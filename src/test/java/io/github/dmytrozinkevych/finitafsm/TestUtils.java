package io.github.dmytrozinkevych.finitafsm;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;

public class TestUtils {

    public static void throwArithmeticException(FSMState oldState, FSMEvent event, FSMState newState) {
        throwArithmeticException();
    }

    public static void throwArithmeticException() {
        var n = 12 / 0;
    }

    public static void emptyAction(FSMState oldState, FSMEvent event, FSMState newState) { }

    public static void allowNeutralInteractions(AbstractFSM fsm) {
        verify(fsm, atLeast(0)).getEnterStateAction(any());
        verify(fsm, atLeast(0)).getExitStateAction(any());
    }
}
