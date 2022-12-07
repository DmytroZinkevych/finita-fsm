package io.github.dmytrozinkevych.finitafsm.demo;

import io.github.dmytrozinkevych.finitafsm.*;

import java.util.Set;

public class TurnstileFSM extends AbstractFSM {

    public enum TurnstileState implements FSMState {
        LOCKED, UNLOCKED
    }

    public enum TurnstileEvent implements FSMEvent {
        PUSH, COIN, QUICK_PASS, ERROR
    }

    public TurnstileFSM() {
        super(TurnstileState.LOCKED);
        final var transitions = Set.of(
                new FSMTransition(TurnstileState.LOCKED, TurnstileEvent.COIN, TurnstileState.UNLOCKED, this::logTransition),
                new FSMTransition(TurnstileState.LOCKED, TurnstileEvent.PUSH, TurnstileState.LOCKED, this::logTransition),
                new FSMTransition(TurnstileState.UNLOCKED, TurnstileEvent.COIN, TurnstileState.UNLOCKED, this::logTransition),
                new FSMTransition(TurnstileState.UNLOCKED, TurnstileEvent.PUSH, TurnstileState.LOCKED, this::logTransition),
                new FSMTransition(TurnstileState.LOCKED, TurnstileEvent.QUICK_PASS, TurnstileState.UNLOCKED, this::quickPass),
                new FSMTransition(TurnstileState.LOCKED, TurnstileEvent.ERROR, TurnstileState.UNLOCKED, this::throwException)
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

    @Override
    protected void onTransitionException(FSMState oldState, FSMEvent event, FSMState newState, Exception cause, FSMTransitionStage transitionStage) {
        System.out.println("** Error happened: running the error handler **");
        if (transitionStage == FSMTransitionStage.TRANSITION_ACTION) {
            System.out.printf("** Error happened during the transition action execution. Cause: %s **%n", cause.getClass().getSimpleName());
            System.out.printf("** Automatically going back to an old state: %s **%n", oldState);
            getEnterStateAction(oldState)
                    .ifPresent(action -> {
                        System.out.println("** Explicitly calling enter state action **");
                        action.accept(oldState, null, oldState);
                    });
        }
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
        triggerAfterwards(TurnstileEvent.PUSH);
    }

    private void throwException(FSMState oldState, FSMEvent event, FSMState newState) {
        System.out.printf("* Transition: %s on %s -> %s *%n", oldState, event, newState);
        System.out.println("* Error is going to happen *");
        var n = 12 / 0;
        System.out.println("Result is " + n);
    }
}
