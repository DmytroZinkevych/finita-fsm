package io.github.dmytrozinkevych.finitafsm.reactive;

import io.github.dmytrozinkevych.finitafsm.*;
import io.github.dmytrozinkevych.finitafsm.exceptions.DuplicateFSMEventException;
import io.github.dmytrozinkevych.finitafsm.exceptions.FSMException;
import io.github.dmytrozinkevych.finitafsm.exceptions.FSMHasNoTransitionsSetException;
import io.github.dmytrozinkevych.finitafsm.exceptions.NoSuchTransitionException;
import io.github.dmytrozinkevych.finitafsm.reactive.utils.ReactiveTriConsumer;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

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

    @Test
    void testTriggeringEventWithActionSetAsNull() {
        var transitions = Set.of(
                new ReactiveFSMTransition(State.S1, Event.E1, State.S2, null)
        );
        var fsm = new AbstractReactiveFSM(State.S1) { };
        fsm.setTransitions(transitions);

        assertDoesNotThrow(() -> fsm.trigger(Event.E1).block());
        assertEquals(State.S2, fsm.getCurrentState());
    }

//    @Test
//    void testRunningActionsOnEnterAndExitState() {
//
//    }
//
//    @Test
//    void testIfActionsOnEnterAndExitStateAreNullNoExceptionIsThrown() {
//
//    }

    @Test
    void testTransitionExceptionWhenOnTransitionExceptionIsNotOverridden() {
        var transitions = Set.of(
                new ReactiveFSMTransition(State.S1, Event.E1, State.S2, this::throwArithmeticException)
        );
        var fsm = new AbstractReactiveFSM(State.S1) { };
        fsm.setTransitions(transitions);

        assertThrows(FSMException.class, () -> fsm.trigger(Event.E1).block());
        assertEquals(State.S1, fsm.getCurrentState());

        try {
            fsm.trigger(Event.E1).block();
        } catch (FSMException ex) {
            assertEquals(ex.getCause().getClass(), ArithmeticException.class);
            assertEquals(State.S1, fsm.getCurrentState());
        }
    }

//    @Test
//    void testBeforeTransitionExceptionHandling() {
//
//    }
//
//    @Test
//    void testExitStateExceptionHandling() {
//
//    }

    @Test
    void testTransitionActionExceptionHandling() {
        var transitionExceptionWasHandled = new AtomicBoolean(false);

        var transitions = Set.of(
                new ReactiveFSMTransition(State.S1, Event.E1, State.S2, this::throwArithmeticException)
        );
        var fsm = new AbstractReactiveFSM(State.S1) {
            @Override
            protected Mono<Void> onTransitionException(FSMState oldState, FSMEvent event, FSMState newState, Throwable cause, FSMTransitionStage transitionStage) {
                return Mono.fromRunnable(() -> {
                    if (transitionStage == FSMTransitionStage.TRANSITION_ACTION) {
                        transitionExceptionWasHandled.set(true);
                        assertEquals(cause.getClass(), ArithmeticException.class);
                    }
                });
            }
        };
        fsm.setTransitions(transitions);

        fsm.trigger(Event.E1).block();

        assertTrue(transitionExceptionWasHandled.get());
        assertEquals(State.S1, fsm.getCurrentState());
    }

//    @Test
//    void testEnterStateExceptionHandling() {
//
//    }
//
//    @Test
//    void testAfterTransitionExceptionHandling() {
//
//    }

    @SuppressWarnings("unchecked")
    @Test
    void testGettingActionsForState() {
        ReactiveTriConsumer<FSMState, FSMEvent, FSMState> enterStateAction = mock(ReactiveTriConsumer.class);
        ReactiveTriConsumer<FSMState, FSMEvent, FSMState> exitStateAction = mock(ReactiveTriConsumer.class);

        var stateActions = Set.of(
                new ReactiveFSMStateActions(State.S1, enterStateAction, exitStateAction)
        );
        var fsm = new AbstractReactiveFSM(State.S1) { };
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
                new ReactiveFSMTransition(State.S1, Event.E1, State.S2, this::emptyAction),
                new ReactiveFSMTransition(State.S2, Event.E1, State.S1, this::emptyAction),
                new ReactiveFSMTransition(State.S2, Event.E2, State.S1, this::emptyAction)
        );
        var fsm = new AbstractReactiveFSM(State.S1) { };
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
