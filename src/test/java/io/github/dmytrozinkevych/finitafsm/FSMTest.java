package io.github.dmytrozinkevych.finitafsm;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class FSMTest {
    @Test
    void testEqualsAndHashCodeMethodsForFSMTransition() {
        var transition1 = new FSMTransition(State.S1, Event.E1, (s1, e, s2) -> { }, State.S2);
        var transition2 = new FSMTransition(State.S1, Event.E1, (s1, e, s2) -> { }, State.S2);
        var transition3 = new FSMTransition(State.S2, Event.E1, (s1, e, s2) -> { }, State.S2);
        var transition4 = new FSMTransition(State.S2, Event.E1, null, State.S2);

        assertEquals(transition1.hashCode(), transition2.hashCode());
        assertEquals(transition1, transition2);

        assertNotEquals(transition1.hashCode(), transition3.hashCode());
        assertNotEquals(transition1, transition3);

        assertEquals(transition3.hashCode(), transition4.hashCode());
        assertEquals(transition3, transition4);
    }

    @Test
    void testEqualsAndHashCodeMethodsForFSMStateActions() {
        var stateActions1 = new FSMStateActions(State.S1, (s1, e, s2) -> { }, (s1, e, s2) -> { });
        var stateActions2 = new FSMStateActions(State.S1, (s1, e, s2) -> { }, (s1, e, s2) -> { });
        var stateActions3 = new FSMStateActions(State.S2, (s1, e, s2) -> { }, (s1, e, s2) -> { });
        var stateActions4 = new FSMStateActions(State.S2, null, null);

        assertEquals(stateActions1.hashCode(), stateActions2.hashCode());
        assertEquals(stateActions1, stateActions2);

        assertNotEquals(stateActions1.hashCode(), stateActions3.hashCode());
        assertNotEquals(stateActions1, stateActions3);

        assertEquals(stateActions3.hashCode(), stateActions4.hashCode());
        assertEquals(stateActions3, stateActions4);
    }

    //TODO: add test cases for FSM exceptions
}
