package io.github.dmytrozinkevych.finitafsm;

import io.github.dmytrozinkevych.finitafsm.exceptions.DuplicateFSMEventException;
import io.github.dmytrozinkevych.finitafsm.exceptions.FSMException;
import io.github.dmytrozinkevych.finitafsm.exceptions.FSMHasNoTransitionsSetException;
import io.github.dmytrozinkevych.finitafsm.exceptions.NoSuchTransitionException;
import io.github.dmytrozinkevych.finitafsm.utils.TriConsumer;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class FSMTest {

    private void emptyAction(FSMState oldState, FSMEvent event, FSMState newState) { }

    private void throwArithmeticException(FSMState oldState, FSMEvent event, FSMState newState) {
        var n = 12 / 0;
    }

    @Test
    void testEqualsAndHashCodeMethodsForFSMTransition() {
        var transition1 = new FSMTransition(State.S1, Event.E1, State.S2, this::emptyAction);
        var transition2 = new FSMTransition(State.S1, Event.E1, State.S2, this::emptyAction);
        var transition3 = new FSMTransition(State.S2, Event.E1, State.S2, this::emptyAction);
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
        var stateActions1 = new FSMStateActions(State.S1, this::emptyAction, this::emptyAction);
        var stateActions2 = new FSMStateActions(State.S1, this::emptyAction, this::emptyAction);
        var stateActions3 = new FSMStateActions(State.S2, this::emptyAction, this::emptyAction);
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
        assertEquals(State.S1, fsm.getCurrentState());
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

    @SuppressWarnings("unchecked")
    @Test
    void testRunningActionsOnEnterAndExitState() {
        TriConsumer<FSMState, FSMEvent, FSMState> onEnterState1 = mock(TriConsumer.class);
        TriConsumer<FSMState, FSMEvent, FSMState> onEnterState2 = mock(TriConsumer.class);
        TriConsumer<FSMState, FSMEvent, FSMState> onExitState1 = mock(TriConsumer.class);

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
        verify(onEnterState1).accept(any(), any(), any());

        assertDoesNotThrow(() -> fsm.trigger(Event.E1));
        var inOrder = Mockito.inOrder(onExitState1, onEnterState2);
        inOrder.verify(onExitState1).accept(any(), any(), any());
        inOrder.verify(onEnterState2).accept(any(), any(), any());
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

    @Test
    void testTransitionExceptionWhenOnTransitionExceptionIsNotOverridden() {
        var transitions = Set.of(
                new FSMTransition(State.S1, Event.E1, State.S2, this::throwArithmeticException)
        );
        var fsm = new AbstractFSM(State.S1) { };
        fsm.setTransitions(transitions);
        
        assertThrows(FSMException.class, () -> fsm.trigger(Event.E1));
        assertEquals(State.S1, fsm.getCurrentState());
        
        try {
            fsm.trigger(Event.E1);
        } catch (FSMException ex) {
            assertEquals(ex.getCause().getClass(), ArithmeticException.class);
            assertEquals(State.S1, fsm.getCurrentState());
        }
    }

    @Test
    void testBeforeTransitionExceptionHandling() {
        var transitionExceptionWasHandled = new AtomicBoolean(false);

        var fsm = new AbstractFSM(State.S1) {
            @Override
            protected void beforeEachTransition(FSMState oldState, FSMEvent event, FSMState newState) {
                throwArithmeticException(oldState, event, newState);
            }

            @Override
            protected void onTransitionException(FSMState oldState, FSMEvent event, FSMState newState, Exception cause, FSMTransitionStage transitionStage) {
                if (transitionStage == FSMTransitionStage.BEFORE_TRANSITION) {
                    transitionExceptionWasHandled.set(true);
                    assertEquals(cause.getClass(), ArithmeticException.class);
                }
            }
        };
        fsm.setTransitions(
                Set.of(
                        new FSMTransition(State.S1, Event.E1, State.S2, this::emptyAction)
                )
        );

        fsm.trigger(Event.E1);

        assertTrue(transitionExceptionWasHandled.get());
        assertEquals(State.S1, fsm.getCurrentState());
    }

    @Test
    void testExitStateExceptionHandling() {
        var transitionExceptionWasHandled = new AtomicBoolean(false);

        var fsm = new AbstractFSM(State.S1) {
            @Override
            protected void onTransitionException(FSMState oldState, FSMEvent event, FSMState newState, Exception cause, FSMTransitionStage transitionStage) {
                if (transitionStage == FSMTransitionStage.EXIT_OLD_STATE) {
                    transitionExceptionWasHandled.set(true);
                    assertEquals(cause.getClass(), ArithmeticException.class);
                }
            }
        };
        fsm.setTransitions(
                Set.of(
                        new FSMTransition(State.S1, Event.E1, State.S2, this::emptyAction)
                )
        );
        fsm.setStateActions(
                Set.of(
                        new FSMStateActions(State.S1, this::emptyAction, this::throwArithmeticException)
                )
        );

        fsm.trigger(Event.E1);

        assertTrue(transitionExceptionWasHandled.get());
        assertEquals(State.S1, fsm.getCurrentState());
    }

    @Test
    void testTransitionActionExceptionHandling() {
        var transitionExceptionWasHandled = new AtomicBoolean(false);

        var fsm = new AbstractFSM(State.S1) {
            @Override
            protected void onTransitionException(FSMState oldState, FSMEvent event, FSMState newState, Exception cause, FSMTransitionStage transitionStage) {
                if (transitionStage == FSMTransitionStage.TRANSITION_ACTION) {
                    transitionExceptionWasHandled.set(true);
                    assertEquals(cause.getClass(), ArithmeticException.class);
                }
            }
        };
        fsm.setTransitions(
                Set.of(
                        new FSMTransition(State.S1, Event.E1, State.S2, this::throwArithmeticException)
                )
        );

        fsm.trigger(Event.E1);

        assertTrue(transitionExceptionWasHandled.get());
        assertEquals(State.S1, fsm.getCurrentState());
    }

    @Test
    void testEnterStateExceptionHandling() {
        var transitionExceptionWasHandled = new AtomicBoolean(false);

        var fsm = new AbstractFSM(State.S1) {
            @Override
            protected void onTransitionException(FSMState oldState, FSMEvent event, FSMState newState, Exception cause, FSMTransitionStage transitionStage) {
                if (transitionStage == FSMTransitionStage.ENTER_NEW_STATE) {
                    transitionExceptionWasHandled.set(true);
                    assertEquals(cause.getClass(), ArithmeticException.class);
                }
            }
        };
        fsm.setTransitions(
                Set.of(
                        new FSMTransition(State.S1, Event.E1, State.S2, this::emptyAction)
                )
        );
        fsm.setStateActions(
                Set.of(
                        new FSMStateActions(State.S2, this::throwArithmeticException, this::emptyAction)
                )
        );

        fsm.trigger(Event.E1);

        assertTrue(transitionExceptionWasHandled.get());
        assertEquals(State.S1, fsm.getCurrentState());
    }

    @Test
    void testAfterTransitionExceptionHandling() {
        var transitionExceptionWasHandled = new AtomicBoolean(false);

        var fsm = new AbstractFSM(State.S1) {
            @Override
            protected void afterEachTransition(FSMState oldState, FSMEvent event, FSMState newState) {
                throwArithmeticException(oldState, event, newState);
            }

            @Override
            protected void onTransitionException(FSMState oldState, FSMEvent event, FSMState newState, Exception cause, FSMTransitionStage transitionStage) {
                if (transitionStage == FSMTransitionStage.AFTER_TRANSITION) {
                    transitionExceptionWasHandled.set(true);
                    assertEquals(cause.getClass(), ArithmeticException.class);
                }
            }
        };
        fsm.setTransitions(
                Set.of(
                        new FSMTransition(State.S1, Event.E1, State.S2, this::emptyAction)
                )
        );

        fsm.trigger(Event.E1);

        assertTrue(transitionExceptionWasHandled.get());
        assertEquals(State.S1, fsm.getCurrentState());
    }
}
