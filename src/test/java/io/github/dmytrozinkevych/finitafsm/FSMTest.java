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

import static io.github.dmytrozinkevych.finitafsm.Event.*;
import static io.github.dmytrozinkevych.finitafsm.State.*;
import static io.github.dmytrozinkevych.finitafsm.TestUtils.allowNeutralInteractions;
import static io.github.dmytrozinkevych.finitafsm.TestUtils.throwArithmeticException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FSMTest {

    @Test
    void testEqualsAndHashCodeMethodsForFSMTransition() {
        var transition1 = new FSMTransition(STATE_1, EVENT_1, STATE_2, TestUtils::emptyAction);
        var transition2 = new FSMTransition(STATE_1, EVENT_1, STATE_2, TestUtils::emptyAction);
        var transition3 = new FSMTransition(STATE_2, EVENT_1, STATE_2, TestUtils::emptyAction);
        var transition4 = new FSMTransition(STATE_2, EVENT_1, STATE_2, null);

        assertEquals(transition1.hashCode(), transition2.hashCode());
        assertEquals(transition1, transition2);

        assertNotEquals(transition1.hashCode(), transition3.hashCode());
        assertNotEquals(transition1, transition3);

        assertEquals(transition3.hashCode(), transition4.hashCode());
        assertEquals(transition3, transition4);
    }

    @Test
    void testEqualsAndHashCodeMethodsForFSMStateActions() {
        var stateActions1 = new FSMStateActions(STATE_1, TestUtils::emptyAction, TestUtils::emptyAction);
        var stateActions2 = new FSMStateActions(STATE_1, TestUtils::emptyAction, TestUtils::emptyAction);
        var stateActions3 = new FSMStateActions(STATE_2, TestUtils::emptyAction, TestUtils::emptyAction);
        var stateActions4 = new FSMStateActions(STATE_2, null, null);

        assertEquals(stateActions1.hashCode(), stateActions2.hashCode());
        assertEquals(stateActions1, stateActions2);

        assertNotEquals(stateActions1.hashCode(), stateActions3.hashCode());
        assertNotEquals(stateActions1, stateActions3);

        assertEquals(stateActions3.hashCode(), stateActions4.hashCode());
        assertEquals(stateActions3, stateActions4);
    }

    @Test
    void testTriggeringFSMWithNoTransitionsSetThrowsException() {
        var fsm = new AbstractFSM(STATE_1) { };

        assertThrows(FSMHasNoTransitionsSetException.class, () -> fsm.trigger(EVENT_1));
    }

    @Test
    void testDuplicatingOfFSMEventThrowsException() {
        var transitions = Set.of(
                new FSMTransition(STATE_1, EVENT_1, STATE_2, TestUtils::emptyAction),
                new FSMTransition(STATE_1, EVENT_2, STATE_2, TestUtils::emptyAction),
                new FSMTransition(STATE_1, EVENT_2, STATE_1, TestUtils::emptyAction)
        );
        var fsm = new AbstractFSM(STATE_1) { };

        assertThrows(DuplicateFSMEventException.class, () -> fsm.setTransitions(transitions));
    }

    @Test
    void testTriggeringEventWhichIsNotSetForCurrentStateThrowsException() {
        var transitions = Set.of(
                new FSMTransition(STATE_1, EVENT_1, STATE_2, TestUtils::emptyAction),
                new FSMTransition(STATE_2, EVENT_2, STATE_1, TestUtils::emptyAction)
        );
        var fsm = new AbstractFSM(STATE_1) { };
        fsm.setTransitions(transitions);

        assertThrows(NoSuchTransitionException.class, () -> fsm.trigger(EVENT_2));
        assertEquals(STATE_1, fsm.getCurrentState());
    }

    @SuppressWarnings("unchecked")
    @Test
    void testTriggeringEventRunsEventAction() {
        TriConsumer<FSMState, FSMEvent, FSMState> action = mock(TriConsumer.class);

        var transitions = Set.of(
                new FSMTransition(STATE_1, EVENT_1, STATE_2, action),
                new FSMTransition(STATE_2, EVENT_2, STATE_1, TestUtils::emptyAction)
        );
        var fsm = new AbstractFSM(STATE_1) { };
        fsm.setTransitions(transitions);

        fsm.trigger(EVENT_1);
        verify(action).accept(STATE_1, EVENT_1, STATE_2);
    }

    @Test
    void testTriggeringEventWithActionSetAsNull() {
        var transitions = Set.of(
                new FSMTransition(STATE_1, EVENT_1, STATE_2, null)
        );
        var fsm = new AbstractFSM(STATE_1) { };
        fsm.setTransitions(transitions);

        assertDoesNotThrow(() -> fsm.trigger(EVENT_1));
        assertEquals(STATE_2, fsm.getCurrentState());
    }

    @SuppressWarnings("unchecked")
    @Test
    void testRunningActionsOnEnterAndExitState() {
        TriConsumer<FSMState, FSMEvent, FSMState> onEnterState1 = mock(TriConsumer.class);
        TriConsumer<FSMState, FSMEvent, FSMState> onEnterState2 = mock(TriConsumer.class);
        TriConsumer<FSMState, FSMEvent, FSMState> onExitState1 = mock(TriConsumer.class);

        var transitions = Set.of(
                new FSMTransition(STATE_1, EVENT_1, STATE_2, TestUtils::emptyAction),
                new FSMTransition(STATE_2, EVENT_2, STATE_1, TestUtils::emptyAction),
                new FSMTransition(STATE_3, EVENT_1, STATE_1, TestUtils::emptyAction)
        );
        var stateActions = Set.of(
                new FSMStateActions(STATE_1, onEnterState1, onExitState1),
                new FSMStateActions(STATE_2, onEnterState2, null)
        );
        var fsm = new AbstractFSM(STATE_3) { };
        fsm.setTransitions(transitions);
        fsm.setStateActions(stateActions);

        fsm.trigger(EVENT_1);
        verify(onEnterState1).accept(STATE_3, EVENT_1, STATE_1);

        assertDoesNotThrow(() -> fsm.trigger(EVENT_1));
        var inOrder = Mockito.inOrder(onExitState1, onEnterState2);
        inOrder.verify(onExitState1).accept(STATE_1, EVENT_1, STATE_2);
        inOrder.verify(onEnterState2).accept(STATE_1, EVENT_1, STATE_2);
    }

    @Test
    void testIfActionsOnEnterAndExitStateAreNullNoExceptionIsThrown() {
        var transitions = Set.of(
                new FSMTransition(STATE_1, EVENT_1, STATE_2, TestUtils::emptyAction),
                new FSMTransition(STATE_2, EVENT_2, STATE_3, TestUtils::emptyAction)
        );
        var stateActions = Set.of(
                new FSMStateActions(STATE_1, null, null),
                new FSMStateActions(STATE_2, null, null)
        );
        var fsm = new AbstractFSM(STATE_1) { };
        fsm.setTransitions(transitions);
        fsm.setStateActions(stateActions);

        assertDoesNotThrow(() -> fsm.trigger(EVENT_1));
    }

    @Test
    void testTransitionExceptionWhenOnTransitionExceptionIsNotOverridden() {
        var transitions = Set.of(
                new FSMTransition(STATE_1, EVENT_1, STATE_2, TestUtils::throwArithmeticException)
        );
        var fsm = new AbstractFSM(STATE_1) { };
        fsm.setTransitions(transitions);
        
        assertThrows(FSMException.class, () -> fsm.trigger(EVENT_1));
        assertEquals(STATE_1, fsm.getCurrentState());
        
        try {
            fsm.trigger(EVENT_1);
        } catch (FSMException ex) {
            assertEquals(ex.getCause().getClass(), ArithmeticException.class);
            assertEquals(STATE_1, fsm.getCurrentState());
        }
    }

    @Test
    void testBeforeTransitionExceptionHandling() {
        class TransitionExceptionFsm extends FsmTemplate {
            public TransitionExceptionFsm() {
                super(STATE_1);
                var transitions = Set.of(
                        new FSMTransition(STATE_1, EVENT_1, STATE_2, this::transitionAction)
                );
                setTransitions(transitions);

                var stateActions = Set.of(
                        new FSMStateActions(STATE_1, this::onEnterState1, this::onExitState1),
                        new FSMStateActions(STATE_2, this::onEnterState2, this::onExitState2)
                );
                setStateActions(stateActions);
            }

            @Override
            protected void beforeEachTransition(FSMState oldState, FSMEvent event, FSMState newState) {
                throwArithmeticException();
            }
        }
        var fsm = spy(new TransitionExceptionFsm());

        fsm.trigger(EVENT_1);

        var inOrder = Mockito.inOrder(fsm);

        inOrder.verify(fsm).trigger(EVENT_1);
        inOrder.verify(fsm).beforeEachTransition(STATE_1, EVENT_1, STATE_2);
        inOrder.verify(fsm).onTransitionException(eq(STATE_1), eq(EVENT_1), eq(STATE_2), isA(ArithmeticException.class), eq(FSMTransitionStage.BEFORE_TRANSITION));

        verifyNoMoreInteractions(fsm);

        assertEquals(STATE_1, fsm.getCurrentState());
    }

    @Test
    void testExitStateExceptionHandling() {
        var transitionExceptionWasHandled = new AtomicBoolean(false);

        var transitions = Set.of(
                new FSMTransition(STATE_1, EVENT_1, STATE_2, TestUtils::emptyAction)
        );
        var stateActions = Set.of(
                new FSMStateActions(STATE_1, TestUtils::emptyAction, TestUtils::throwArithmeticException)
        );
        var fsm = new AbstractFSM(STATE_1) {
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

        fsm.trigger(EVENT_1);

        assertTrue(transitionExceptionWasHandled.get());
        assertEquals(STATE_1, fsm.getCurrentState());
    }

    @Test
    void testTransitionActionExceptionHandling() {
        var transitionExceptionWasHandled = new AtomicBoolean(false);

        var transitions = Set.of(
                new FSMTransition(STATE_1, EVENT_1, STATE_2, TestUtils::throwArithmeticException)
        );
        var fsm = new AbstractFSM(STATE_1) {
            @Override
            protected void onTransitionException(FSMState oldState, FSMEvent event, FSMState newState, Exception cause, FSMTransitionStage transitionStage) {
                if (transitionStage == FSMTransitionStage.TRANSITION_ACTION) {
                    transitionExceptionWasHandled.set(true);
                    assertEquals(cause.getClass(), ArithmeticException.class);
                }
            }
        };
        fsm.setTransitions(transitions);

        fsm.trigger(EVENT_1);

        assertTrue(transitionExceptionWasHandled.get());
        assertEquals(STATE_1, fsm.getCurrentState());
    }

    @Test
    void testEnterStateExceptionHandling() {
        var transitionExceptionWasHandled = new AtomicBoolean(false);

        var transitions = Set.of(
                new FSMTransition(STATE_1, EVENT_1, STATE_2, TestUtils::emptyAction)
        );
        var stateActions = Set.of(
                new FSMStateActions(STATE_2, TestUtils::throwArithmeticException, TestUtils::emptyAction)
        );
        var fsm = new AbstractFSM(STATE_1) {
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

        fsm.trigger(EVENT_1);

        assertTrue(transitionExceptionWasHandled.get());
        assertEquals(STATE_1, fsm.getCurrentState());
    }

    @Test
    void testAfterTransitionExceptionHandling() {
        var transitionExceptionWasHandled = new AtomicBoolean(false);

        var transitions = Set.of(
                new FSMTransition(STATE_1, EVENT_1, STATE_2, TestUtils::emptyAction)
        );
        var fsm = new AbstractFSM(STATE_1) {
            @Override
            protected void afterEachTransition(FSMState oldState, FSMEvent event, FSMState newState) {
                throwArithmeticException();
            }

            @Override
            protected void onTransitionException(FSMState oldState, FSMEvent event, FSMState newState, Exception cause, FSMTransitionStage transitionStage) {
                transitionExceptionWasHandled.set(true);
                assertEquals(FSMTransitionStage.AFTER_TRANSITION, transitionStage);
                assertEquals(ArithmeticException.class, cause.getClass());
            }
        };
        fsm.setTransitions(transitions);

        fsm.trigger(EVENT_1);

        assertTrue(transitionExceptionWasHandled.get());
        assertEquals(STATE_1, fsm.getCurrentState());
    }

    @Test
    void testOrderOfRunningOfAllActions() {
        var fsm = spy(TestOrderOfActionsFsm.class);
        fsm.trigger(EVENT_1);

        var inOrder = Mockito.inOrder(fsm);

        inOrder.verify(fsm).beforeEachTransition(STATE_1, EVENT_1, STATE_2);
        inOrder.verify(fsm).onExitState1(STATE_1, EVENT_1, STATE_2);
        inOrder.verify(fsm).transitionAction(STATE_1, EVENT_1, STATE_2);
        inOrder.verify(fsm).onEnterState2(STATE_1, EVENT_1, STATE_2);
        inOrder.verify(fsm).afterEachTransition(STATE_1, EVENT_1, STATE_2);

        verify(fsm, never()).onEnterState1(any(), any(), any());
        verify(fsm, never()).onExitState2(any(), any(), any());
    }

    @Test
    void testTriggerAfterwards() {
        var fsm = spy(TestTriggerAfterwardsFsm.class);
        fsm.trigger(EVENT_1);

        var inOrder = Mockito.inOrder(fsm);

        inOrder.verify(fsm).trigger(EVENT_1);
        inOrder.verify(fsm).beforeEachTransition(STATE_1, EVENT_1, STATE_2);
        inOrder.verify(fsm).onExitState1(STATE_1, EVENT_1, STATE_2);
        inOrder.verify(fsm).transitionActionWithTriggerAfterwards(STATE_1, EVENT_1, STATE_2);
        inOrder.verify(fsm).triggerAfterwards(EVENT_2);
        inOrder.verify(fsm).onEnterState2(STATE_1, EVENT_1, STATE_2);
        inOrder.verify(fsm).afterEachTransition(STATE_1, EVENT_1, STATE_2);

        inOrder.verify(fsm).trigger(EVENT_2);
        inOrder.verify(fsm).beforeEachTransition(STATE_2, EVENT_2, STATE_3);
        inOrder.verify(fsm).onExitState2(STATE_2, EVENT_2, STATE_3);
        inOrder.verify(fsm).regularTransitionAction(STATE_2, EVENT_2, STATE_3);
        inOrder.verify(fsm).onEnterState3(STATE_2, EVENT_2, STATE_3);
        inOrder.verify(fsm).afterEachTransition(STATE_2, EVENT_2, STATE_3);

        allowNeutralInteractions(fsm);
        verifyNoMoreInteractions(fsm);

        assertEquals(STATE_3, fsm.getCurrentState());
    }

    @Test
    void testNotRunningAnyOtherActionsAfterExceptionOccurred() {
        var fsm = spy(TestExceptionFsm.class);
        fsm.trigger(EVENT_1);

        var inOrder = Mockito.inOrder(fsm);

        inOrder.verify(fsm).trigger(EVENT_1);
        inOrder.verify(fsm).beforeEachTransition(STATE_1, EVENT_1, STATE_2);
        inOrder.verify(fsm).onTransitionException(eq(STATE_1), eq(EVENT_1), eq(STATE_2), isA(ArithmeticException.class), eq(FSMTransitionStage.BEFORE_TRANSITION));

        verifyNoMoreInteractions(fsm);

        assertEquals(STATE_1, fsm.getCurrentState());
    }

    @Test
    void testResettingNextEventAfterExceptionOccurred() {
        var fsm = spy(TestExceptionBeforeTriggeringNextEventFsm.class);

        fsm.trigger(EVENT_1);
        assertEquals(STATE_1, fsm.getCurrentState());

        var inOrder = Mockito.inOrder(fsm);

        inOrder.verify(fsm).actionWithTrigger(STATE_1, EVENT_1, STATE_2);
        inOrder.verify(fsm).onTransitionException(eq(STATE_1), eq(EVENT_1), eq(STATE_2), isA(ArithmeticException.class), eq(FSMTransitionStage.TRANSITION_ACTION));

        fsm.trigger(EVENT_3);
        inOrder.verify(fsm).regularAction(STATE_1, EVENT_3, STATE_2);

        assertEquals(STATE_2, fsm.getCurrentState());
    }

    @SuppressWarnings("unchecked")
    @Test
    void testGettingActionsForState() {
        TriConsumer<FSMState, FSMEvent, FSMState> enterStateAction = mock(TriConsumer.class);
        TriConsumer<FSMState, FSMEvent, FSMState> exitStateAction = mock(TriConsumer.class);

        var stateActions = Set.of(
                new FSMStateActions(STATE_1, enterStateAction, exitStateAction)
        );
        var fsm = new AbstractFSM(STATE_1) { };
        fsm.setStateActions(stateActions);

        assertTrue(fsm.getEnterStateAction(STATE_1).isPresent());
        fsm.getEnterStateAction(STATE_1).get().accept(null, null, null);
        verify(enterStateAction).accept(any(), any(), any());

        assertTrue(fsm.getExitStateAction(STATE_1).isPresent());
        fsm.getExitStateAction(STATE_1).get().accept(null, null, null);
        verify(exitStateAction).accept(any(), any(), any());
    }

    @Test
    void testGeneratingPlantUmlStateDiagramCode() {
        var transitions = Set.of(
                new FSMTransition(STATE_1, EVENT_1, STATE_2, TestUtils::emptyAction),
                new FSMTransition(STATE_2, EVENT_1, STATE_1, TestUtils::emptyAction),
                new FSMTransition(STATE_2, EVENT_2, STATE_1, TestUtils::emptyAction)
        );
        var fsm = new AbstractFSM(STATE_1) { };
        fsm.setTransitions(transitions);

        var diagramCode = fsm.generatePlantUmlDiagramCode(STATE_1, STATE_2);

        assertTrue(diagramCode.startsWith(
                """
                @startuml
                !pragma layout smetana
                hide empty description
                                
                [*] --> STATE_1
                """
        ));
        assertTrue(diagramCode.endsWith(
                """
                STATE_2 --> [*]
                            
                @enduml
                """
        ));
        assertTrue(diagramCode.endsWith("\n"));

        var expectedTransitions = Set.of(
                "STATE_1 --> STATE_2 : EVENT_1",
                "STATE_2 --> STATE_1 : EVENT_1",
                "STATE_2 --> STATE_1 : EVENT_2"
        );
        var actualTransitions = Arrays.stream(diagramCode.split("\n"))
                .filter(line -> line.contains(":"))
                .collect(Collectors.toSet());
        assertEquals(expectedTransitions, actualTransitions);
    }

    @Test
    void testGeneratingDiagramForFSMWithNoTransitionsSetThrowsException() {
        var fsm = new AbstractFSM(STATE_1) { };

        assertThrows(FSMHasNoTransitionsSetException.class, () -> fsm.generatePlantUmlDiagramCode(STATE_1, STATE_2));
    }
}
