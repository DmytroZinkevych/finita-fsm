package io.github.dmytrozinkevych.finitafsm;

import io.github.dmytrozinkevych.finitafsm.exceptions.DuplicateFSMEventException;
import io.github.dmytrozinkevych.finitafsm.exceptions.FSMHasNoTransitionsSetException;
import io.github.dmytrozinkevych.finitafsm.exceptions.NoSuchTransitionException;
import io.github.dmytrozinkevych.finitafsm.utils.Pair;
import io.github.dmytrozinkevych.finitafsm.utils.TriConsumer;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public abstract class AbstractFSM {

    //TODO: ensure proper work in concurrent environment
    private Map<FSMState, Map<FSMEvent, Pair<TriConsumer<FSMState, FSMEvent, FSMState>, FSMState>>> statesWithTransitions;

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
            Map<FSMEvent, Pair<TriConsumer<FSMState, FSMEvent, FSMState>, FSMState>> eventMap;
            if (statesWithTransitions.containsKey(oldState)) {
                eventMap = statesWithTransitions.get(oldState);
            } else {
                eventMap = new HashMap<>();
                statesWithTransitions.put(oldState, eventMap);
            }
            if (eventMap.containsKey(transition.event())) {
                throw new DuplicateFSMEventException();
            }
            eventMap.put(transition.event(), new Pair<>(transition.action(), transition.newState()));
        }
    }

    protected void setStateActions(Set<FSMStateActions> stateActions) {
        statesEnterExitActions = new HashMap<>();
        for (var stateAction : stateActions) {
            statesEnterExitActions.put(
                    stateAction.state(),
                    new Pair<>(stateAction.onEnterState(), stateAction.onExitState())
            );
        }
    }

    protected void beforeEachTransition(FSMState oldState, FSMEvent event, FSMState newState) { }

    protected void afterEachTransition(FSMState oldState, FSMEvent event, FSMState newState) { }

    public FSMState trigger(FSMEvent event) {
        if (statesWithTransitions == null || statesWithTransitions.isEmpty()) {
            throw new FSMHasNoTransitionsSetException();
        }
        var actionNewStatePair = Optional.ofNullable(statesWithTransitions.get(currentState))
                .map(stateTransitions -> stateTransitions.get(event))
                .orElseThrow(() -> new NoSuchTransitionException(currentState, event));

        var oldState = currentState;
        var transitionAction = actionNewStatePair.left();
        var newState = actionNewStatePair.right();

        beforeEachTransition(oldState, event, newState);
        if (statesEnterExitActions != null && statesEnterExitActions.containsKey(oldState)) {
            var stateExitAction = statesEnterExitActions.get(oldState).right();
            if (stateExitAction != null) {
                stateExitAction.accept(oldState, event, newState);
            }
        }
        if (transitionAction != null) {
            transitionAction.accept(oldState, event, newState);
        }
        currentState = newState;
        if (statesEnterExitActions != null && statesEnterExitActions.containsKey(newState)) {
            var stateEnterAction = statesEnterExitActions.get(newState).left();
            if (stateEnterAction != null) {
                stateEnterAction.accept(oldState, event, newState);
            }
        }
        afterEachTransition(oldState, event, newState);

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
}
