package io.github.dmytrozinkevych.finitafsm.demo;

import org.junit.jupiter.api.Test;

import static io.github.dmytrozinkevych.finitafsm.demo.TurnstileFSM.TurnstileEvent;
import static io.github.dmytrozinkevych.finitafsm.demo.TurnstileFSM.TurnstileState;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TurnstileFSMTest {
    @Test
    void turnstileFSMDemo() {
        var turnstileFSM = new TurnstileFSM();
        assertEquals(TurnstileState.LOCKED, turnstileFSM.getCurrentState());

        System.out.println("\n========== Turnstile FSM Demo START ==========\n");
        System.out.println("Initial state: " + turnstileFSM.getCurrentState() + "\n");
        System.out.println("Transitions:\n");

        turnstileFSM.trigger(TurnstileEvent.PUSH);
        assertEquals(TurnstileState.LOCKED, turnstileFSM.getCurrentState());

        turnstileFSM.trigger(TurnstileEvent.COIN);
        assertEquals(TurnstileState.UNLOCKED, turnstileFSM.getCurrentState());

        turnstileFSM.trigger(TurnstileEvent.PUSH);
        assertEquals(TurnstileState.LOCKED, turnstileFSM.getCurrentState());

        System.out.println("--- Quick pass start ---\n");

        turnstileFSM.trigger(TurnstileEvent.QUICK_PASS);
        assertEquals(TurnstileState.LOCKED, turnstileFSM.getCurrentState());

        System.out.println("--- Quick pass finish ---\n");

        turnstileFSM.trigger(TurnstileEvent.ERROR);
        assertEquals(TurnstileState.LOCKED, turnstileFSM.getCurrentState());

        turnstileFSM.trigger(TurnstileEvent.PUSH);
        assertEquals(TurnstileState.LOCKED, turnstileFSM.getCurrentState());

        System.out.println("\n========== Turnstile FSM Demo FINISH ==========\n");
    }
}
