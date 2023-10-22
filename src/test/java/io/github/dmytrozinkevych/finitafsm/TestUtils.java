package io.github.dmytrozinkevych.finitafsm;

public class TestUtils {
    public static void throwArithmeticException(FSMState oldState, FSMEvent event, FSMState newState) {
        throwArithmeticException();
    }

    public static void throwArithmeticException() {
        var n = 12 / 0;
    }

    public static void emptyAction(FSMState oldState, FSMEvent event, FSMState newState) { }
}
