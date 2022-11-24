package io.github.dmytrozinkevych.finitafsm.demo;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TurnstileFSMTest {
    @Test
    void turnstileFSMDemo() {
        var turnstileFSM = new TurnstileFSM();
        Assertions.assertEquals(TurnstileFSM.TurnstileState.LOCKED, turnstileFSM.getCurrentState());

        System.out.println("\n========== Turnstile FSM Demo START ==========\n");
        System.out.println("Initial state: " + turnstileFSM.getCurrentState() + "\n");
        System.out.println("Transitions:\n");

        turnstileFSM.trigger(TurnstileFSM.TurnstileEvent.PUSH);
        assertEquals(TurnstileFSM.TurnstileState.LOCKED, turnstileFSM.getCurrentState());

        turnstileFSM.trigger(TurnstileFSM.TurnstileEvent.COIN);
        assertEquals(TurnstileFSM.TurnstileState.UNLOCKED, turnstileFSM.getCurrentState());

        turnstileFSM.trigger(TurnstileFSM.TurnstileEvent.PUSH);
        assertEquals(TurnstileFSM.TurnstileState.LOCKED, turnstileFSM.getCurrentState());

        System.out.println("--- Quick pass start ---\n");

        turnstileFSM.trigger(TurnstileFSM.TurnstileEvent.QUICK_PASS);
        assertEquals(TurnstileFSM.TurnstileState.LOCKED, turnstileFSM.getCurrentState());

        System.out.println("--- Quick pass finish ---\n");

        turnstileFSM.trigger(TurnstileFSM.TurnstileEvent.PUSH);
        assertEquals(TurnstileFSM.TurnstileState.LOCKED, turnstileFSM.getCurrentState());

        System.out.println("\n========== Turnstile FSM Demo FINISH ==========\n");
    }
}
