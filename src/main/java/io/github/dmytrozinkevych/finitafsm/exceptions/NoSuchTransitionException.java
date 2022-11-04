package io.github.dmytrozinkevych.finitafsm.exceptions;

import io.github.dmytrozinkevych.finitafsm.FSMEvent;
import io.github.dmytrozinkevych.finitafsm.FSMState;

public class NoSuchTransitionException extends RuntimeException {

    public NoSuchTransitionException(FSMState state, FSMEvent event) {
        super("State '%s' does not have transition for event '%s'".formatted(state, event));
    }
}