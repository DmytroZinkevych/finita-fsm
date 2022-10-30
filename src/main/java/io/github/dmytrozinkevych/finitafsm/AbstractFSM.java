package io.github.dmytrozinkevych.finitafsm;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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

    protected void setCurrentState(FSMState currentState) {
        this.currentState = currentState;
    }

    protected void setTransitions(Collection<FSMTransition> transitions) {
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

    public FSMState trigger(FSMEvent event) {
        var actionNewStatePair = statesWithTransitions.get(currentState).get(event);
        var newState = actionNewStatePair.right();
        actionNewStatePair.left().accept(currentState, event, newState);
        currentState = newState;
        return newState;
    }
}
