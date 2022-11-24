package io.github.dmytrozinkevych.finitafsm.exceptions;

public class FSMException extends RuntimeException {

    public FSMException() {
        super();
    }

    public FSMException(String message) {
        super(message);
    }
}
