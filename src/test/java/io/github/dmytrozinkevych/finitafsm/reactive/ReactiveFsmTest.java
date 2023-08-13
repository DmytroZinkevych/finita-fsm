package io.github.dmytrozinkevych.finitafsm.reactive;

import io.github.dmytrozinkevych.finitafsm.Event;
import io.github.dmytrozinkevych.finitafsm.FSMEvent;
import io.github.dmytrozinkevych.finitafsm.FSMState;
import io.github.dmytrozinkevych.finitafsm.State;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class ReactiveFsmTest {

    private Mono<Void> emptyAction(FSMState oldState, FSMEvent event, FSMState newState) {
        return Mono.empty();
    }

    private Mono<Void> throwArithmeticException(FSMState oldState, FSMEvent event, FSMState newState) {
        var n = 12 / 0;
        return Mono.empty();
    }

    @Test
    void testEqualsAndHashCodeMethodsForFSMTransition() {
        var transition1 = new ReactiveFSMTransition(State.S1, Event.E1, State.S2, this::emptyAction);
        var transition2 = new ReactiveFSMTransition(State.S1, Event.E1, State.S2, this::emptyAction);
        var transition3 = new ReactiveFSMTransition(State.S2, Event.E1, State.S2, this::emptyAction);
        var transition4 = new ReactiveFSMTransition(State.S2, Event.E1, State.S2, null);

        assertEquals(transition1.hashCode(), transition2.hashCode());
        assertEquals(transition1, transition2);

        assertNotEquals(transition1.hashCode(), transition3.hashCode());
        assertNotEquals(transition1, transition3);

        assertEquals(transition3.hashCode(), transition4.hashCode());
        assertEquals(transition3, transition4);
    }

    @Test
    void testEqualsAndHashCodeMethodsForFSMStateActions() {
        var stateActions1 = new ReactiveFSMStateActions(State.S1, this::emptyAction, this::emptyAction);
        var stateActions2 = new ReactiveFSMStateActions(State.S1, this::emptyAction, this::emptyAction);
        var stateActions3 = new ReactiveFSMStateActions(State.S2, this::emptyAction, this::emptyAction);
        var stateActions4 = new ReactiveFSMStateActions(State.S2, null, null);

        assertEquals(stateActions1.hashCode(), stateActions2.hashCode());
        assertEquals(stateActions1, stateActions2);

        assertNotEquals(stateActions1.hashCode(), stateActions3.hashCode());
        assertNotEquals(stateActions1, stateActions3);

        assertEquals(stateActions3.hashCode(), stateActions4.hashCode());
        assertEquals(stateActions3, stateActions4);
    }
}
