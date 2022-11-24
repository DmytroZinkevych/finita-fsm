package io.github.dmytrozinkevych.finitafsm.demo;

import io.github.dmytrozinkevych.finitafsm.*;

import java.util.Set;

public class TurnstileFSM extends AbstractFSM {

    public enum TurnstileState implements FSMState {
        LOCKED, UNLOCKED
    }

    public enum TurnstileEvent implements FSMEvent {
        PUSH, COIN, QUICK_PASS
    }

    public TurnstileFSM() {
        super(TurnstileState.LOCKED);
        final var transitions = Set.of(
                new FSMTransition(TurnstileState.LOCKED, TurnstileEvent.COIN, this::logTransition, TurnstileState.UNLOCKED),
                new FSMTransition(TurnstileState.LOCKED, TurnstileEvent.PUSH, this::logTransition, TurnstileState.LOCKED),
                new FSMTransition(TurnstileState.UNLOCKED, TurnstileEvent.COIN, this::logTransition, TurnstileState.UNLOCKED),
                new FSMTransition(TurnstileState.UNLOCKED, TurnstileEvent.PUSH, this::logTransition, TurnstileState.LOCKED),
                new FSMTransition(TurnstileState.LOCKED, TurnstileEvent.QUICK_PASS, this::quickPass, TurnstileState.UNLOCKED)
        );
        setTransitions(transitions);

        final var stateActions = Set.of(
                new FSMStateActions(TurnstileState.LOCKED, this::logEnterState, this::logExitState),
                new FSMStateActions(TurnstileState.UNLOCKED, this::logEnterState, this::logExitState)
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

    private void quickPass(FSMState oldState, FSMEvent event, FSMState newState) {
        System.out.printf("~ Transition: %s on %s -> %s ~%n", oldState, event, newState);
        System.out.println("~ Quick pass triggered ~");
        triggerNext(TurnstileEvent.PUSH);
    }
}
