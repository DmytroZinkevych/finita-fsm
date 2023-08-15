package io.github.dmytrozinkevych.finitafsm.reactive;

import io.github.dmytrozinkevych.finitafsm.FSMEvent;
import io.github.dmytrozinkevych.finitafsm.FSMState;
import io.github.dmytrozinkevych.finitafsm.FSMTransitionStage;
import io.github.dmytrozinkevych.finitafsm.exceptions.DuplicateFSMEventException;
import io.github.dmytrozinkevych.finitafsm.exceptions.FSMException;
import io.github.dmytrozinkevych.finitafsm.exceptions.FSMHasNoTransitionsSetException;
import io.github.dmytrozinkevych.finitafsm.exceptions.NoSuchTransitionException;
import io.github.dmytrozinkevych.finitafsm.reactive.utils.ReactiveTriConsumer;
import io.github.dmytrozinkevych.finitafsm.utils.Pair;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class AbstractReactiveFSM {

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
    private Map<FSMState, Map<FSMEvent, Pair<FSMState, ReactiveTriConsumer<FSMState, FSMEvent, FSMState>>>> statesWithTransitions;

    private Map<FSMState, Pair<ReactiveTriConsumer<FSMState, FSMEvent, FSMState>, ReactiveTriConsumer<FSMState, FSMEvent, FSMState>>> statesEnterExitActions;

    private FSMState currentState;

//    private FSMEvent nextEvent;

    protected AbstractReactiveFSM(FSMState initialState) {
        currentState = initialState;
    }

    //TODO: make reactive?
    public FSMState getCurrentState() {
        return currentState;
    }

    protected void setTransitions(Set<ReactiveFSMTransition> transitions) {
        statesWithTransitions = new HashMap<>();
        for (var transition : transitions) {
            var oldState = transition.oldState();
            Map<FSMEvent, Pair<FSMState, ReactiveTriConsumer<FSMState, FSMEvent, FSMState>>> eventMap;
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

    protected void setStateActions(Set<ReactiveFSMStateActions> stateActions) {
        if (stateActions == null)
            return;
        statesEnterExitActions = stateActions.stream()
                .filter(fsmStateActions -> fsmStateActions.enterStateAction() != null || fsmStateActions.exitStateAction() != null)
                .collect(
                        Collectors.toMap(
                                ReactiveFSMStateActions::state,
                                fsmStateActions -> new Pair<>(fsmStateActions.enterStateAction(), fsmStateActions.exitStateAction())
                        )
                );
    }

//    protected Mono<Void> beforeEachTransition(FSMState oldState, FSMEvent event, FSMState newState) { }
//
//    protected Mono<Void> afterEachTransition(FSMState oldState, FSMEvent event, FSMState newState) { }
//

    protected Mono<Void> onTransitionException(FSMState oldState, FSMEvent event, FSMState newState, Throwable cause, FSMTransitionStage transitionStage) {
        throw new FSMException(cause);
    }

    private Optional<Pair<ReactiveTriConsumer<FSMState, FSMEvent, FSMState>, ReactiveTriConsumer<FSMState, FSMEvent, FSMState>>> getActionsForState(FSMState state) {
        return Optional.ofNullable(statesEnterExitActions)
                .map(statesActionsMap -> statesActionsMap.get(state));
    }

    protected Optional<ReactiveTriConsumer<FSMState, FSMEvent, FSMState>> getEnterStateAction(FSMState state) {
        return getActionsForState(state)
                .map(Pair::left);
    }

    protected Optional<ReactiveTriConsumer<FSMState, FSMEvent, FSMState>> getExitStateAction(FSMState state) {
        return getActionsForState(state)
                .map(Pair::right);
    }

    private void requireHasTransitions() {
        if (statesWithTransitions == null || statesWithTransitions.isEmpty()) {
            throw new FSMHasNoTransitionsSetException();
        }
    }

    //TODO: return Mono from `this`?
    //TODO: or create a special publisher - single for the whole instance (how to prevent direct calls, outside of the reactive flow)
    public Mono<Void> trigger(FSMEvent event) {
        return Mono.fromSupplier(() -> {
            requireHasTransitions();
            return Optional.of(statesWithTransitions.get(currentState))
                    .map(stateTransitions -> stateTransitions.get(event))
                    .orElseThrow(() -> new NoSuchTransitionException(currentState, event));
        })
                .flatMap(actionNewStatePair -> {
                    var oldState = currentState;
                    var newState = actionNewStatePair.left();
                    var transitionAction = actionNewStatePair.right();


            //        beforeEachTransition(oldState, event, newState)
            //                .doOnError(throwable ->
            //                    onTransitionException(oldState, event, newState, throwable, FSMTransitionStage.BEFORE_TRANSITION)
            //                )

            //        try {
            //            beforeEachTransition(oldState, event, newState);
            //        } catch (Exception ex) {
            //            onTransitionException(oldState, event, newState, ex, FSMTransitionStage.BEFORE_TRANSITION);
            //            return;
            //        }
            //
            //        var exitStateAction = getExitStateAction(oldState);
            //        if (exitStateAction.isPresent()) {
            //            try {
            //                exitStateAction.get().accept(oldState, event, newState);
            //            } catch (Exception ex) {
            //                onTransitionException(oldState, event, newState, ex, FSMTransitionStage.EXIT_OLD_STATE);
            //                return;
            //            }
            //        }

                    //TODO: idea: assign variables to Monos after action (or Mono.then() is actually better?). But need to mark an error -> use doOnError for that?

                    if (transitionAction != null) {
                        return transitionAction.accept(oldState, event, newState)
                                .doOnSuccess(ignore -> currentState = newState)
                                .onErrorResume(ex -> onTransitionException(oldState, event, newState, ex, FSMTransitionStage.TRANSITION_ACTION));
                    } else {
                        return Mono.fromRunnable(() -> currentState = newState);
                    }
                    //
            //        var enterStateAction = getEnterStateAction(newState);
            //        if (enterStateAction.isPresent()) {
            //            try {
            //                enterStateAction.get().accept(oldState, event, newState);
            //            } catch (Exception ex) {
            //                currentState = oldState;
            //                onTransitionException(oldState, event, newState, ex, FSMTransitionStage.ENTER_NEW_STATE);
            //                return;
            //            }
            //        }
            //
            //        try {
            //            afterEachTransition(oldState, event, newState);
            //        } catch (Exception ex) {
            //            currentState = oldState;
            //            onTransitionException(oldState, event, newState, ex, FSMTransitionStage.AFTER_TRANSITION);
            //            return;
            //        }
            //
            //        if (nextEvent != null) {
            //            var newEvent = nextEvent;
            //            nextEvent = null;
            //            trigger(newEvent);
            //        }
                });
    }

//    protected void triggerAfterwards(FSMEvent event) {
//        nextEvent = event;
//    }

    public String generatePlantUmlDiagramCode(FSMState startStane, FSMState endState) {
        requireHasTransitions();
        var transitions = statesWithTransitions.entrySet()
                .stream()
                .flatMap(stateWithTransitionsEntry -> stateWithTransitionsEntry.getValue()
                        .entrySet()
                        .stream()
                        .map(eventNewStateEntry -> new ReactiveFSMTransition(   //TODO: create package-private class/interface BasicTransition which doesn't contain lambda?
                                stateWithTransitionsEntry.getKey(),
                                eventNewStateEntry.getKey(),
                                eventNewStateEntry.getValue().left(),
                                null
                        ))
                )
                .map(transition -> transition.oldState() + " --> " + transition.newState() + " : " + transition.event())
                .collect(Collectors.joining("\n"));
        return STATE_DIAGRAM_TEMPLATE.formatted(startStane, transitions, endState); //TODO: create package-private Constants/Utils class? Put Pair there as well?
    }
}
