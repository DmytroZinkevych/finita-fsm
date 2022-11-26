package io.github.dmytrozinkevych.finitafsm;

import io.github.dmytrozinkevych.finitafsm.exceptions.DuplicateFSMEventException;
import io.github.dmytrozinkevych.finitafsm.exceptions.FSMHasNoTransitionsSetException;
import io.github.dmytrozinkevych.finitafsm.exceptions.NoSuchTransitionException;
import io.github.dmytrozinkevych.finitafsm.utils.TriConsumer;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class FSMTest {

    private void emptyAction(FSMState oldState, FSMEvent event, FSMState newState) { }

    @Test
    void testEqualsAndHashCodeMethodsForFSMTransition() {
        var transition1 = new FSMTransition(State.S1, Event.E1, State.S2, (s1, e, s2) -> { });
        var transition2 = new FSMTransition(State.S1, Event.E1, State.S2, (s1, e, s2) -> { });
        var transition3 = new FSMTransition(State.S2, Event.E1, State.S2, (s1, e, s2) -> { });
        var transition4 = new FSMTransition(State.S2, Event.E1, State.S2, null);

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

    @Test
    void testTriggeringFSMWithNoTransitionsSetThrowsException() {
        var fsm = new AbstractFSM(State.S1) { };

        assertThrows(FSMHasNoTransitionsSetException.class, () -> fsm.trigger(Event.E1));
    }

    @Test
    void testDuplicatingOfFSMEventThrowsException() {
        var transitions = Set.of(
                new FSMTransition(State.S1, Event.E1, State.S2, this::emptyAction),
                new FSMTransition(State.S1, Event.E2, State.S2, this::emptyAction),
                new FSMTransition(State.S1, Event.E2, State.S1, this::emptyAction)
        );
        var fsm = new AbstractFSM(State.S1) { };

        assertThrows(DuplicateFSMEventException.class, () -> fsm.setTransitions(transitions));
    }

    @Test
    void testTriggeringEventWhichIsNotSetForCurrentStateThrowsException() {
        var transitions = Set.of(
                new FSMTransition(State.S1, Event.E1, State.S2, this::emptyAction),
                new FSMTransition(State.S2, Event.E2, State.S1, this::emptyAction)
        );
        var fsm = new AbstractFSM(State.S1) { };
        fsm.setTransitions(transitions);

        assertThrows(NoSuchTransitionException.class, () -> fsm.trigger(Event.E2));
    }

    @Test
    void testTriggeringEventWithActionSetAsNull() {
        var transitions = Set.of(
                new FSMTransition(State.S1, Event.E1, State.S2, null)
        );
        var fsm = new AbstractFSM(State.S1) { };
        fsm.setTransitions(transitions);

        assertDoesNotThrow(() -> fsm.trigger(Event.E1));
    }

    @Test
    void testRunningActionsOnEnterAndExitState() {
        var onEnterState1Called = new AtomicBoolean(false);
        var onEnterState2Called = new AtomicBoolean(false);
        var onExitState1Called = new AtomicBoolean(false);

        TriConsumer<FSMState, FSMEvent, FSMState> onEnterState1 = (s1, e, s2) -> onEnterState1Called.set(true);
        TriConsumer<FSMState, FSMEvent, FSMState> onEnterState2 = (s1, e, s2) -> onEnterState2Called.set(true);
        TriConsumer<FSMState, FSMEvent, FSMState> onExitState1 = (s1, e, s2) -> onExitState1Called.set(true);

        var transitions = Set.of(
                new FSMTransition(State.S1, Event.E1, State.S2, this::emptyAction),
                new FSMTransition(State.S2, Event.E2, State.S1, this::emptyAction),
                new FSMTransition(State.S3, Event.E1, State.S1, this::emptyAction)
        );
        var stateActions = Set.of(
                new FSMStateActions(State.S1, onEnterState1, onExitState1),
                new FSMStateActions(State.S2, onEnterState2, null)
        );
        var fsm = new AbstractFSM(State.S3) { };
        fsm.setTransitions(transitions);
        fsm.setStateActions(stateActions);

        fsm.trigger(Event.E1);
        assertTrue(onEnterState1Called.get());

        assertDoesNotThrow(() -> fsm.trigger(Event.E1));
        assertTrue(onExitState1Called.get());
        assertTrue(onEnterState2Called.get());
    }

    @Test
    void testIfActionsOnEnterAndExitStateAreNullNoExceptionIsThrown() {
        var transitions = Set.of(
                new FSMTransition(State.S1, Event.E1, State.S2, this::emptyAction),
                new FSMTransition(State.S2, Event.E2, State.S3, this::emptyAction)
        );
        var stateActions = Set.of(
                new FSMStateActions(State.S1, null, null),
                new FSMStateActions(State.S2, null, null)
        );
        var fsm = new AbstractFSM(State.S1) { };
        fsm.setTransitions(transitions);
        fsm.setStateActions(stateActions);

        assertDoesNotThrow(() -> fsm.trigger(Event.E1));
    }
}
