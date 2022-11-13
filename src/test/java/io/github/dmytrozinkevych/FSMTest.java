package io.github.dmytrozinkevych;

import org.junit.jupiter.api.Test;

import static io.github.dmytrozinkevych.TurnstileFSM.Event;
import static io.github.dmytrozinkevych.TurnstileFSM.State;
import static org.junit.jupiter.api.Assertions.assertEquals;

class FSMTest {
    @Test
    void turnstileFSMDemo() {
        var turnstileFSM = new TurnstileFSM();
        assertEquals(State.LOCKED, turnstileFSM.getCurrentState());

        System.out.println("\n========== Turnstile FSM ==========\n");
        System.out.println("Initial state: " + turnstileFSM.getCurrentState() + "\n");
        System.out.println("Transitions:\n");

        turnstileFSM.trigger(Event.PUSH);
        assertEquals(State.LOCKED, turnstileFSM.getCurrentState());

        turnstileFSM.trigger(Event.COIN);
        assertEquals(State.UNLOCKED, turnstileFSM.getCurrentState());

        turnstileFSM.trigger(Event.PUSH);
        assertEquals(State.LOCKED, turnstileFSM.getCurrentState());

        System.out.println("--- Turnstile test start ---\n");

        turnstileFSM.trigger(Event.TEST_WORK);
        assertEquals(State.LOCKED, turnstileFSM.getCurrentState());

        System.out.println("--- Turnstile test finish ---\n");

        turnstileFSM.trigger(Event.PUSH);
        assertEquals(State.LOCKED, turnstileFSM.getCurrentState());
    }

    //TODO: add test cases for FSM exceptions
}
