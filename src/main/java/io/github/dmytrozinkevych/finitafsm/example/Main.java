package io.github.dmytrozinkevych.finitafsm.example;

public class Main {
    public static void main(String[] args) {
        var turnstileFSM = new TurnstileFSM();
        System.out.println("\n========== Turnstile FSM ==========\n");
        System.out.println("Initial state: " + turnstileFSM.getCurrentState() + "\n");
        System.out.println("Transitions:\n");
        turnstileFSM.trigger(TurnstileFSM.Event.PUSH);
        turnstileFSM.trigger(TurnstileFSM.Event.COIN);
        turnstileFSM.trigger(TurnstileFSM.Event.PUSH);
        System.out.println("--- Turnstile test start ---\n");
        turnstileFSM.trigger(TurnstileFSM.Event.TEST_WORK);
        System.out.println("--- Turnstile test finish ---\n");
        turnstileFSM.trigger(TurnstileFSM.Event.PUSH);
    }
}
