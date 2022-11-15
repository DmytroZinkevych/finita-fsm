package io.github.dmytrozinkevych.demo;

import org.junit.jupiter.api.Test;

import static io.github.dmytrozinkevych.demo.TurnstileFSM.TurnstileEvent;
import static io.github.dmytrozinkevych.demo.TurnstileFSM.TurnstileState;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TurnstileFSMTest {
    @Test
    void turnstileFSMDemo() {
        var turnstileFSM = new TurnstileFSM();
        assertEquals(TurnstileState.LOCKED, turnstileFSM.getCurrentState());

        System.out.println("\n========== Turnstile FSM ==========\n");
        System.out.println("Initial state: " + turnstileFSM.getCurrentState() + "\n");
        System.out.println("Transitions:\n");

        turnstileFSM.trigger(TurnstileEvent.PUSH);
        assertEquals(TurnstileState.LOCKED, turnstileFSM.getCurrentState());

        turnstileFSM.trigger(TurnstileEvent.COIN);
        assertEquals(TurnstileState.UNLOCKED, turnstileFSM.getCurrentState());

        turnstileFSM.trigger(TurnstileEvent.PUSH);
        assertEquals(TurnstileState.LOCKED, turnstileFSM.getCurrentState());

        System.out.println("--- Turnstile test start ---\n");

        turnstileFSM.trigger(TurnstileEvent.TEST_WORK);
        assertEquals(TurnstileState.LOCKED, turnstileFSM.getCurrentState());

        System.out.println("--- Turnstile test finish ---\n");

        turnstileFSM.trigger(TurnstileEvent.PUSH);
        assertEquals(TurnstileState.LOCKED, turnstileFSM.getCurrentState());
    }
}
