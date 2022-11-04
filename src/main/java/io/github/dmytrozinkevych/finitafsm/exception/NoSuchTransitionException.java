package io.github.dmytrozinkevych.finitafsm.exception;

import io.github.dmytrozinkevych.finitafsm.FSMEvent;
import io.github.dmytrozinkevych.finitafsm.FSMState;

public class NoSuchTransitionException extends RuntimeException {

    public NoSuchTransitionException(FSMState state, FSMEvent event) {
        super("State " + state + " does not have transition for event " + event);
    }
}
