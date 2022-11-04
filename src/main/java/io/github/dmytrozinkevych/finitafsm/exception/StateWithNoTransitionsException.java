package io.github.dmytrozinkevych.finitafsm.exception;

import io.github.dmytrozinkevych.finitafsm.FSMState;

public class StateWithNoTransitionsException extends RuntimeException {

    public StateWithNoTransitionsException(FSMState state) {
        super("State " + state + " doesn't have any transitions");
    }
}
