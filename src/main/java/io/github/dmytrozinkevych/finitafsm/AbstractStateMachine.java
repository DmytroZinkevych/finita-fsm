package io.github.dmytrozinkevych.finitafsm;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractStateMachine {

    //TODO: ensure proper work in concurrent environment
    private final Map<FSMState, Map<FSMEvent, Pair<Runnable, FSMState>>> statesWithTransitions;

    protected AbstractStateMachine(Collection<FSMTransition> transitions) {
        statesWithTransitions = new HashMap<>();
        for (var transition : transitions) {
            var oldState = transition.oldState();
            Map<FSMEvent, Pair<Runnable, FSMState>> eventMap;
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

    public FSMState trigger(FSMState currentState, FSMEvent event) {
        var actionNewStatePair = statesWithTransitions.get(currentState).get(event);
        actionNewStatePair.left().run();
        return actionNewStatePair.right();
        //TODO: save current state in machine
    }
}
