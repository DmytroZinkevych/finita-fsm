package io.github.dmytrozinkevych.finitafsm;

import io.github.dmytrozinkevych.finitafsm.exceptions.DuplicateFSMEventException;
import io.github.dmytrozinkevych.finitafsm.exceptions.FSMException;
import io.github.dmytrozinkevych.finitafsm.exceptions.FSMHasNoTransitionsSetException;
import io.github.dmytrozinkevych.finitafsm.exceptions.NoSuchTransitionException;
import io.github.dmytrozinkevych.finitafsm.utils.Pair;
import io.github.dmytrozinkevych.finitafsm.utils.TriConsumer;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class AbstractFSM {

    private static final String STATE_DIAGRAM_TEMPLATE =
            """
            @startuml
            !pragma layout smetana
            hide empty description
                            
            [*] --> %s
            
            %s
                            
            %s --> [*]
                            
            @enduml
            """;

    //TODO: ensure proper work in concurrent environment
    private Map<FSMState, Map<FSMEvent, Pair<FSMState, TriConsumer<FSMState, FSMEvent, FSMState>>>> statesWithTransitions;

    private Map<FSMState, Pair<TriConsumer<FSMState, FSMEvent, FSMState>, TriConsumer<FSMState, FSMEvent, FSMState>>> statesEnterExitActions;

    private FSMState currentState;

    private FSMEvent nextEvent;

    protected AbstractFSM(FSMState initialState) {
        currentState = initialState;
    }

    public FSMState getCurrentState() {
        return currentState;
    }

    protected void setTransitions(Set<FSMTransition> transitions) {
        statesWithTransitions = new HashMap<>();
        for (var transition : transitions) {
            var oldState = transition.oldState();
            Map<FSMEvent, Pair<FSMState, TriConsumer<FSMState, FSMEvent, FSMState>>> eventMap;
            if (statesWithTransitions.containsKey(oldState)) {
                eventMap = statesWithTransitions.get(oldState);
            } else {
                eventMap = new HashMap<>();
                statesWithTransitions.put(oldState, eventMap);
            }
            if (eventMap.containsKey(transition.event())) {
                throw new DuplicateFSMEventException();
            }
            eventMap.put(transition.event(), new Pair<>(transition.newState(), transition.action()));
        }
    }

    protected void setStateActions(Set<FSMStateActions> stateActions) {
        if (stateActions == null)
            return;
        statesEnterExitActions = stateActions.stream()
                .filter(fsmStateActions -> fsmStateActions.enterStateAction() != null || fsmStateActions.exitStateAction() != null)
                .collect(
                        Collectors.toMap(
                                FSMStateActions::state,
                                fsmStateActions -> new Pair<>(fsmStateActions.enterStateAction(), fsmStateActions.exitStateAction())
                        )
                );
    }

    protected void beforeEachTransition(FSMState oldState, FSMEvent event, FSMState newState) { }

    protected void afterEachTransition(FSMState oldState, FSMEvent event, FSMState newState) { }

    protected void onTransitionException(FSMState oldState, FSMEvent event, FSMState newState, Exception cause, FSMTransitionStage transitionStage) {
        throw new FSMException(cause);
    }

    private Optional<Pair<TriConsumer<FSMState, FSMEvent, FSMState>, TriConsumer<FSMState, FSMEvent, FSMState>>> getActionsForState(FSMState state) {
        return Optional.ofNullable(statesEnterExitActions)
                .map(statesActionsMap -> statesActionsMap.get(state));
    }

    protected Optional<TriConsumer<FSMState, FSMEvent, FSMState>> getEnterStateAction(FSMState state) {
        return getActionsForState(state)
                .map(Pair::left);
    }

    protected Optional<TriConsumer<FSMState, FSMEvent, FSMState>> getExitStateAction(FSMState state) {
        return getActionsForState(state)
                .map(Pair::right);
    }

    private void requireHasTransitions() {
        if (statesWithTransitions == null || statesWithTransitions.isEmpty()) {
            throw new FSMHasNoTransitionsSetException();
        }
    }

    public FSMState trigger(FSMEvent event) {
        requireHasTransitions();
        var actionNewStatePair = Optional.of(statesWithTransitions.get(currentState))
                .map(stateTransitions -> stateTransitions.get(event))
                .orElseThrow(() -> new NoSuchTransitionException(currentState, event));

        var oldState = currentState;
        var newState = actionNewStatePair.left();
        var transitionAction = actionNewStatePair.right();

        try {
            beforeEachTransition(oldState, event, newState);
        } catch (Exception ex) {
            onTransitionException(oldState, event, newState, ex, FSMTransitionStage.BEFORE_TRANSITION);
            return oldState;
        }

        var exitStateAction = getExitStateAction(oldState);
        if (exitStateAction.isPresent()) {
            try {
                exitStateAction.get().accept(oldState, event, newState);
            } catch (Exception ex) {
                onTransitionException(oldState, event, newState, ex, FSMTransitionStage.EXIT_OLD_STATE);
                return oldState;
            }
        }

        try {
            if (transitionAction != null) {
                transitionAction.accept(oldState, event, newState);
            }
            currentState = newState;
        } catch (Exception ex) {
            onTransitionException(oldState, event, newState, ex, FSMTransitionStage.TRANSITION_ACTION);
            return oldState;
        }

        var enterStateAction = getEnterStateAction(newState);
        if (enterStateAction.isPresent()) {
            try {
                enterStateAction.get().accept(oldState, event, newState);
            } catch (Exception ex) {
                currentState = oldState;
                onTransitionException(oldState, event, newState, ex, FSMTransitionStage.ENTER_NEW_STATE);
                return oldState;
            }
        }

        try {
            afterEachTransition(oldState, event, newState);
        } catch (Exception ex) {
            currentState = oldState;
            onTransitionException(oldState, event, newState, ex, FSMTransitionStage.AFTER_TRANSITION);
            return oldState;
        }

        if (nextEvent != null) {
            var newEvent = nextEvent;
            nextEvent = null;
            return trigger(newEvent);
        }

        return newState;
    }

    protected void triggerAfterwards(FSMEvent event) {
        nextEvent = event;
    }

    public String generatePlantUmlDiagramCode(FSMState startStane, FSMState endState) {
        requireHasTransitions();
        var transitions = statesWithTransitions.entrySet()
                .stream()
                .flatMap(stateWithTransitionsEntry -> stateWithTransitionsEntry.getValue()
                        .entrySet()
                        .stream()
                        .map(eventNewStateEntry -> new FSMTransition(
                                stateWithTransitionsEntry.getKey(),
                                eventNewStateEntry.getKey(),
                                eventNewStateEntry.getValue().left(),
                                null
                        ))
                )
                .map(transition -> transition.oldState() + " --> " + transition.newState() + " : " + transition.event())
                .collect(Collectors.joining("\n"));
        return STATE_DIAGRAM_TEMPLATE.formatted(startStane, transitions, endState);
    }
}
