package io.github.dmytrozinkevych.finitafsm;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public abstract class AbstractFSM {

    //TODO: ensure proper work in concurrent environment
    private Map<FSMState, Map<FSMEvent, Pair<TriConsumer<FSMState, FSMEvent, FSMState>, FSMState>>> statesWithTransitions;

    private FSMState currentState;

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
            // TODO: throw exception if event already exists
            eventMap.put(transition.event(), new Pair<>(transition.action(), transition.newState()));
        }
    }

    protected void beforeEachTransition(FSMState oldState, FSMEvent event, FSMState newState) { }

    protected void afterEachTransition(FSMState oldState, FSMEvent event, FSMState newState) { }

    public FSMState trigger(FSMEvent event) {
        var actionNewStatePair = statesWithTransitions.get(currentState).get(event);
        var action = actionNewStatePair.left();
        var oldState = currentState;
        var newState = actionNewStatePair.right();

        beforeEachTransition(oldState, event, newState);
        action.accept(oldState, event, newState);
        currentState = newState;
        afterEachTransition(oldState, event, newState);

        return currentState;
    }
}
