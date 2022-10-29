package io.github.dmytrozinkevych.finitafsm.example;

import io.github.dmytrozinkevych.finitafsm.FSMState;

public class Main {
    public static void main(String[] args) {
        System.out.println("Turnstile FSM states:");
        var turnstileFSM = new TurnstileFSM();
        FSMState currentState = TurnstileFSM.State.LOCKED;
        System.out.println(currentState);
        currentState = turnstileFSM.trigger(currentState, TurnstileFSM.Event.PUSH);
        System.out.println(currentState);
        currentState = turnstileFSM.trigger(currentState, TurnstileFSM.Event.COIN);
        System.out.println(currentState);
        currentState = turnstileFSM.trigger(currentState, TurnstileFSM.Event.PUSH);
        System.out.println(currentState);
    }
}
