package io.github.dmytrozinkevych;

import io.github.dmytrozinkevych.finitafsm.FSMStateActions;
import io.github.dmytrozinkevych.finitafsm.FSMTransition;
import org.junit.jupiter.api.Test;

import static io.github.dmytrozinkevych.TurnstileFSM.Event;
import static io.github.dmytrozinkevych.TurnstileFSM.State;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

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

    @Test
    void testEqualsAndHashCodeMethodsForFSMTransition() {
        var transition1 = new FSMTransition(State.LOCKED, Event.COIN, (s1, e, s2) -> { }, State.UNLOCKED);
        var transition2 = new FSMTransition(State.LOCKED, Event.COIN, (s1, e, s2) -> { }, State.UNLOCKED);
        var transition3 = new FSMTransition(State.UNLOCKED, Event.COIN, (s1, e, s2) -> { }, State.UNLOCKED);

        assertEquals(transition1.hashCode(), transition2.hashCode());
        assertEquals(transition1, transition2);

        assertNotEquals(transition1.hashCode(), transition3.hashCode());
        assertNotEquals(transition1, transition3);
    }

    @Test
    void testEqualsAndHashCodeMethodsForFSMStateActions() {
        var stateActions1 = new FSMStateActions(State.LOCKED, (s1, e, s2) -> { }, (s1, e, s2) -> { });
        var stateActions2 = new FSMStateActions(State.LOCKED, (s1, e, s2) -> { }, (s1, e, s2) -> { });
        var stateActions3 = new FSMStateActions(State.UNLOCKED, (s1, e, s2) -> { }, (s1, e, s2) -> { });

        assertEquals(stateActions1.hashCode(), stateActions2.hashCode());
        assertEquals(stateActions1, stateActions2);

        assertNotEquals(stateActions1.hashCode(), stateActions3.hashCode());
        assertNotEquals(stateActions1, stateActions3);
    }

    //TODO: add test cases for FSM exceptions
}
