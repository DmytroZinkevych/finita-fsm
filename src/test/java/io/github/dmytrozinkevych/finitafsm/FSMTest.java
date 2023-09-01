package io.github.dmytrozinkevych.finitafsm;

import io.github.dmytrozinkevych.finitafsm.exceptions.DuplicateFSMEventException;
import io.github.dmytrozinkevych.finitafsm.exceptions.FSMException;
import io.github.dmytrozinkevych.finitafsm.exceptions.FSMHasNoTransitionsSetException;
import io.github.dmytrozinkevych.finitafsm.exceptions.NoSuchTransitionException;
import io.github.dmytrozinkevych.finitafsm.utils.TriConsumer;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FSMTest {

    private static class TestFsm extends AbstractFSM {
        TestFsm() {
            super(State.S1);
            final var transitions = Set.of(
                    new FSMTransition(State.S1, Event.E1, State.S2, this::transitionAction)
            );
            setTransitions(transitions);

            var stateActions = Set.of(
                    new FSMStateActions(State.S1, this::onEnterState1, this::onExitState1),
                    new FSMStateActions(State.S2, this::onEnterState2, this::onExitState2)
            );
            setStateActions(stateActions);
        }

        void transitionAction(FSMState oldState, FSMEvent event, FSMState newState) { }

        void onEnterState1(FSMState oldState, FSMEvent event, FSMState newState) { }

        void onExitState1(FSMState oldState, FSMEvent event, FSMState newState) { }

        void onEnterState2(FSMState oldState, FSMEvent event, FSMState newState) { }

        void onExitState2(FSMState oldState, FSMEvent event, FSMState newState) { }
    }

    private static class TestTriggerAfterwardsFsm extends AbstractFSM {
        TestTriggerAfterwardsFsm() {
            super(State.S1);
            final var transitions = Set.of(
                    new FSMTransition(State.S1, Event.E1, State.S2, this::transitionActionWithTriggerAfterwards),
                    new FSMTransition(State.S2, Event.E2, State.S1, this::regularTransitionAction)
            );
            setTransitions(transitions);

            var stateActions = Set.of(
                    new FSMStateActions(State.S1, this::onEnterState1, this::onExitState1),
                    new FSMStateActions(State.S2, this::onEnterState2, this::onExitState2)
            );
            setStateActions(stateActions);
        }

        void transitionActionWithTriggerAfterwards(FSMState oldState, FSMEvent event, FSMState newState) {
            triggerAfterwards(Event.E2);
        }

        void regularTransitionAction(FSMState oldState, FSMEvent event, FSMState newState) { }

        void onEnterState1(FSMState oldState, FSMEvent event, FSMState newState) { }

        void onExitState1(FSMState oldState, FSMEvent event, FSMState newState) { }

        void onEnterState2(FSMState oldState, FSMEvent event, FSMState newState) { }

        void onExitState2(FSMState oldState, FSMEvent event, FSMState newState) { }
    }

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

    @SuppressWarnings("unchecked")
    @Test
    void testTriggeringEventRunsEventAction() {
        TriConsumer<FSMState, FSMEvent, FSMState> action = mock(TriConsumer.class);

        var transitions = Set.of(
                new FSMTransition(State.S1, Event.E1, State.S2, action),
                new FSMTransition(State.S2, Event.E2, State.S1, this::emptyAction)
        );
        var fsm = new AbstractFSM(State.S1) { };
        fsm.setTransitions(transitions);

        fsm.trigger(Event.E1);
        verify(action).accept(State.S1, Event.E1, State.S2);
    }

    @Test
    void testTriggeringEventWithActionSetAsNull() {
        var transitions = Set.of(
                new FSMTransition(State.S1, Event.E1, State.S2, null)
        );
        var fsm = new AbstractFSM(State.S1) { };
        fsm.setTransitions(transitions);

        assertDoesNotThrow(() -> fsm.trigger(Event.E1));
        assertEquals(State.S2, fsm.getCurrentState());
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
        verify(onEnterState1).accept(State.S3, Event.E1, State.S1);

        assertDoesNotThrow(() -> fsm.trigger(Event.E1));
        var inOrder = Mockito.inOrder(onExitState1, onEnterState2);
        inOrder.verify(onExitState1).accept(State.S1, Event.E1, State.S2);
        inOrder.verify(onEnterState2).accept(State.S1, Event.E1, State.S2);
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

        var transitions = Set.of(
                new FSMTransition(State.S1, Event.E1, State.S2, this::emptyAction)
        );
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
        fsm.setTransitions(transitions);

        fsm.trigger(Event.E1);

        assertTrue(transitionExceptionWasHandled.get());
        assertEquals(State.S1, fsm.getCurrentState());
    }

    @Test
    void testExitStateExceptionHandling() {
        var transitionExceptionWasHandled = new AtomicBoolean(false);

        var transitions = Set.of(
                new FSMTransition(State.S1, Event.E1, State.S2, this::emptyAction)
        );
        var stateActions = Set.of(
                new FSMStateActions(State.S1, this::emptyAction, this::throwArithmeticException)
        );
        var fsm = new AbstractFSM(State.S1) {
            @Override
            protected void onTransitionException(FSMState oldState, FSMEvent event, FSMState newState, Exception cause, FSMTransitionStage transitionStage) {
                if (transitionStage == FSMTransitionStage.EXIT_OLD_STATE) {
                    transitionExceptionWasHandled.set(true);
                    assertEquals(cause.getClass(), ArithmeticException.class);
                }
            }
        };
        fsm.setTransitions(transitions);
        fsm.setStateActions(stateActions);

        fsm.trigger(Event.E1);

        assertTrue(transitionExceptionWasHandled.get());
        assertEquals(State.S1, fsm.getCurrentState());
    }

    @Test
    void testTransitionActionExceptionHandling() {
        var transitionExceptionWasHandled = new AtomicBoolean(false);

        var transitions = Set.of(
                new FSMTransition(State.S1, Event.E1, State.S2, this::throwArithmeticException)
        );
        var fsm = new AbstractFSM(State.S1) {
            @Override
            protected void onTransitionException(FSMState oldState, FSMEvent event, FSMState newState, Exception cause, FSMTransitionStage transitionStage) {
                if (transitionStage == FSMTransitionStage.TRANSITION_ACTION) {
                    transitionExceptionWasHandled.set(true);
                    assertEquals(cause.getClass(), ArithmeticException.class);
                }
            }
        };
        fsm.setTransitions(transitions);

        fsm.trigger(Event.E1);

        assertTrue(transitionExceptionWasHandled.get());
        assertEquals(State.S1, fsm.getCurrentState());
    }

    @Test
    void testEnterStateExceptionHandling() {
        var transitionExceptionWasHandled = new AtomicBoolean(false);

        var transitions = Set.of(
                new FSMTransition(State.S1, Event.E1, State.S2, this::emptyAction)
        );
        var stateActions = Set.of(
                new FSMStateActions(State.S2, this::throwArithmeticException, this::emptyAction)
        );
        var fsm = new AbstractFSM(State.S1) {
            @Override
            protected void onTransitionException(FSMState oldState, FSMEvent event, FSMState newState, Exception cause, FSMTransitionStage transitionStage) {
                if (transitionStage == FSMTransitionStage.ENTER_NEW_STATE) {
                    transitionExceptionWasHandled.set(true);
                    assertEquals(cause.getClass(), ArithmeticException.class);
                }
            }
        };
        fsm.setTransitions(transitions);
        fsm.setStateActions(stateActions);

        fsm.trigger(Event.E1);

        assertTrue(transitionExceptionWasHandled.get());
        assertEquals(State.S1, fsm.getCurrentState());
    }

    @Test
    void testAfterTransitionExceptionHandling() {
        var transitionExceptionWasHandled = new AtomicBoolean(false);

        var transitions = Set.of(
                new FSMTransition(State.S1, Event.E1, State.S2, this::emptyAction)
        );
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
        fsm.setTransitions(transitions);

        fsm.trigger(Event.E1);

        assertTrue(transitionExceptionWasHandled.get());
        assertEquals(State.S1, fsm.getCurrentState());
    }

    @Test
    void testOrderOfRunningOfAllActions() {
        var fsm = spy(TestFsm.class);
        fsm.trigger(Event.E1);

        var inOrder = Mockito.inOrder(fsm);
        inOrder.verify(fsm).beforeEachTransition(State.S1, Event.E1, State.S2);
        inOrder.verify(fsm).onExitState1(State.S1, Event.E1, State.S2);
        inOrder.verify(fsm).transitionAction(State.S1, Event.E1, State.S2);
        inOrder.verify(fsm).onEnterState2(State.S1, Event.E1, State.S2);
        inOrder.verify(fsm).afterEachTransition(State.S1, Event.E1, State.S2);

        verify(fsm, times(0)).onEnterState1(any(), any(), any());
        verify(fsm, times(0)).onExitState2(any(), any(), any());
    }

    @Test
    void testTriggerAfterwards() {
        var fsm = spy(TestTriggerAfterwardsFsm.class);
        fsm.trigger(Event.E1);

        var inOrder = Mockito.inOrder(fsm);
        inOrder.verify(fsm, times(1)).beforeEachTransition(State.S1, Event.E1, State.S2);
        inOrder.verify(fsm, times(1)).onExitState1(State.S1, Event.E1, State.S2);
        inOrder.verify(fsm, times(1)).transitionActionWithTriggerAfterwards(State.S1, Event.E1, State.S2);
        inOrder.verify(fsm, times(1)).triggerAfterwards(Event.E2);
        inOrder.verify(fsm, times(1)).onEnterState2(State.S1, Event.E1, State.S2);
        inOrder.verify(fsm, times(1)).afterEachTransition(State.S1, Event.E1, State.S2);

        inOrder.verify(fsm, times(1)).beforeEachTransition(State.S2, Event.E2, State.S1);
        inOrder.verify(fsm, times(1)).onExitState2(State.S2, Event.E2, State.S1);
        inOrder.verify(fsm, times(1)).regularTransitionAction(State.S2, Event.E2, State.S1);
        inOrder.verify(fsm, times(1)).onEnterState1(State.S2, Event.E2, State.S1);
        inOrder.verify(fsm, times(1)).afterEachTransition(State.S2, Event.E2, State.S1);

        verify(fsm, never()).triggerAfterwards(Event.E1);
    }

    @SuppressWarnings("unchecked")
    @Test
    void testGettingActionsForState() {
        TriConsumer<FSMState, FSMEvent, FSMState> enterStateAction = mock(TriConsumer.class);
        TriConsumer<FSMState, FSMEvent, FSMState> exitStateAction = mock(TriConsumer.class);

        var stateActions = Set.of(
                new FSMStateActions(State.S1, enterStateAction, exitStateAction)
        );
        var fsm = new AbstractFSM(State.S1) { };
        fsm.setStateActions(stateActions);

        assertTrue(fsm.getEnterStateAction(State.S1).isPresent());
        fsm.getEnterStateAction(State.S1).get().accept(null, null, null);
        verify(enterStateAction).accept(any(), any(), any());

        assertTrue(fsm.getExitStateAction(State.S1).isPresent());
        fsm.getExitStateAction(State.S1).get().accept(null, null, null);
        verify(exitStateAction).accept(any(), any(), any());
    }

    @Test
    void testGeneratingPlantUmlStateDiagramCode() {
        var transitions = Set.of(
                new FSMTransition(State.S1, Event.E1, State.S2, this::emptyAction),
                new FSMTransition(State.S2, Event.E1, State.S1, this::emptyAction),
                new FSMTransition(State.S2, Event.E2, State.S1, this::emptyAction)
        );
        var fsm = new AbstractFSM(State.S1) { };
        fsm.setTransitions(transitions);

        var diagramCode = fsm.generatePlantUmlDiagramCode(State.S1, State.S2);

        assertTrue(diagramCode.startsWith(
                """
                @startuml
                !pragma layout smetana
                hide empty description
                                
                [*] --> S1
                """
        ));
        assertTrue(diagramCode.endsWith(
                """
                S2 --> [*]
                            
                @enduml
                """
        ));
        assertTrue(diagramCode.endsWith("\n"));

        var expectedTransitions = Set.of(
                "S1 --> S2 : E1",
                "S2 --> S1 : E1",
                "S2 --> S1 : E2"
        );
        var actualTransitions = Arrays.stream(diagramCode.split("\n"))
                .filter(line -> line.contains(":"))
                .collect(Collectors.toSet());
        assertEquals(expectedTransitions, actualTransitions);
    }

    @Test
    void testGeneratingDiagramForFSMWithNoTransitionsSetThrowsException() {
        var fsm = new AbstractFSM(State.S1) { };

        assertThrows(FSMHasNoTransitionsSetException.class, () -> fsm.generatePlantUmlDiagramCode(State.S1, State.S2));
    }
}
