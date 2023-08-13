package io.github.dmytrozinkevych.finitafsm.reactive;

import io.github.dmytrozinkevych.finitafsm.Event;
import io.github.dmytrozinkevych.finitafsm.FSMEvent;
import io.github.dmytrozinkevych.finitafsm.FSMState;
import io.github.dmytrozinkevych.finitafsm.State;
import io.github.dmytrozinkevych.finitafsm.exceptions.DuplicateFSMEventException;
import io.github.dmytrozinkevych.finitafsm.exceptions.FSMHasNoTransitionsSetException;
import io.github.dmytrozinkevych.finitafsm.exceptions.NoSuchTransitionException;
import io.github.dmytrozinkevych.finitafsm.reactive.utils.ReactiveTriConsumer;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReactiveFsmTest {

    private Mono<Void> emptyAction(FSMState oldState, FSMEvent event, FSMState newState) {
        return Mono.empty();
    }

    private Mono<Void> throwArithmeticException(FSMState oldState, FSMEvent event, FSMState newState) {
        return Mono.fromRunnable(() -> {
            var n = 12 / 0;
        });
    }

    @Test
    void testEqualsAndHashCodeMethodsForReactiveFSMTransition() {
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
    void testEqualsAndHashCodeMethodsForReactiveFSMStateActions() {
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

    @Test
    void testTriggeringFSMWithNoTransitionsSetThrowsException() {
        var fsm = new AbstractReactiveFSM(State.S1) { };

        assertThrows(FSMHasNoTransitionsSetException.class, () -> fsm.trigger(Event.E1).block());
    }

    @Test
    void testDuplicatingOfFSMEventThrowsException() {
        var transitions = Set.of(
                new ReactiveFSMTransition(State.S1, Event.E1, State.S2, this::emptyAction),
                new ReactiveFSMTransition(State.S1, Event.E2, State.S2, this::emptyAction),
                new ReactiveFSMTransition(State.S1, Event.E2, State.S1, this::emptyAction)
        );
        var fsm = new AbstractReactiveFSM(State.S1) { };

        assertThrows(DuplicateFSMEventException.class, () -> fsm.setTransitions(transitions));
    }

    @Test
    void testTriggeringEventWhichIsNotSetForCurrentStateThrowsException() {
        var transitions = Set.of(
                new ReactiveFSMTransition(State.S1, Event.E1, State.S2, this::emptyAction),
                new ReactiveFSMTransition(State.S2, Event.E2, State.S1, this::emptyAction)
        );
        var fsm = new AbstractReactiveFSM(State.S1) { };
        fsm.setTransitions(transitions);

        assertThrows(NoSuchTransitionException.class, () -> fsm.trigger(Event.E2).block());
        assertEquals(State.S1, fsm.getCurrentState());
    }

    @SuppressWarnings("unchecked")
    @Test
    void testTriggeringEventRunsEventAction() {
        ReactiveTriConsumer<FSMState, FSMEvent, FSMState> action = mock(ReactiveTriConsumer.class);

        when(action.accept(any(), any(), any())).thenReturn(Mono.empty());

        var transitions = Set.of(
                new ReactiveFSMTransition(State.S1, Event.E1, State.S2, action),
                new ReactiveFSMTransition(State.S2, Event.E2, State.S1, this::emptyAction)
        );
        var fsm = new AbstractReactiveFSM(State.S1) { };
        fsm.setTransitions(transitions);

        fsm.trigger(Event.E1).block();
        verify(action).accept(State.S1, Event.E1, State.S2);
        assertEquals(State.S2, fsm.getCurrentState());
    }
}
