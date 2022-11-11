package io.github.dmytrozinkevych.finitafsm.example;

import io.github.dmytrozinkevych.finitafsm.*;

import java.util.Set;

public class TurnstileFSM extends AbstractFSM {

    public enum State implements FSMState {
        LOCKED, UNLOCKED
    }

    public enum Event implements FSMEvent {
        PUSH, COIN, TEST_WORK
    }

    public TurnstileFSM() {
        super(State.LOCKED);
        final var transitions = Set.of(
                new FSMTransition(State.LOCKED, Event.COIN, this::logTransition, State.UNLOCKED),
                new FSMTransition(State.LOCKED, Event.PUSH, this::logTransition, State.LOCKED),
                new FSMTransition(State.UNLOCKED, Event.COIN, this::logTransition, State.UNLOCKED),
                new FSMTransition(State.UNLOCKED, Event.PUSH, this::logTransition, State.LOCKED),
                new FSMTransition(State.LOCKED, Event.TEST_WORK, this::testWork, State.UNLOCKED)
        );
        setTransitions(transitions);

        final var stateActions = Set.of(
                new FSMStateActions(State.LOCKED, this::logEnterState, this::logExitState),
                new FSMStateActions(State.UNLOCKED, this::logEnterState, this::logExitState)
        );
        setStateActions(stateActions);
    }

    @Override
    protected void beforeEachTransition(FSMState oldState, FSMEvent event, FSMState newState) {
        System.out.printf("Before transition from %s (on %s) to %s%n", oldState, event, newState);
    }

    @Override
    protected void afterEachTransition(FSMState oldState, FSMEvent event, FSMState newState) {
        System.out.printf("After transition from %s (on %s) to %s%n%n", oldState, event, newState);
    }

    private void logTransition(FSMState oldState, FSMEvent event, FSMState newState) {
        System.out.printf("  Transition: %s on %s -> %s%n", oldState, event, newState);
    }

    private void logEnterState(FSMState oldState, FSMEvent event, FSMState newState) {
        System.out.printf(" Entering the state: %s%n", newState);
    }

    private void logExitState(FSMState oldState, FSMEvent event, FSMState newState) {
        System.out.printf(" Exiting from the state: %s%n", oldState);
    }

    private void testWork(FSMState oldState, FSMEvent event, FSMState newState) {
        System.out.printf("~ Transition: %s on %s -> %s ~%n", oldState, event, newState);
        System.out.println("~ Testing the work of the turnstile ~");
        triggerNext(Event.PUSH);
    }
}
