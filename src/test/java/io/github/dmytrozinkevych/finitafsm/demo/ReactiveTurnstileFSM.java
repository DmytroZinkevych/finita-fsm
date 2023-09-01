package io.github.dmytrozinkevych.finitafsm.demo;

import io.github.dmytrozinkevych.finitafsm.FSMEvent;
import io.github.dmytrozinkevych.finitafsm.FSMState;
import io.github.dmytrozinkevych.finitafsm.FSMTransitionStage;
import io.github.dmytrozinkevych.finitafsm.reactive.AbstractReactiveFSM;
import io.github.dmytrozinkevych.finitafsm.reactive.ReactiveFSMStateActions;
import io.github.dmytrozinkevych.finitafsm.reactive.ReactiveFSMTransition;
import reactor.core.publisher.Mono;

import java.util.Set;

public class ReactiveTurnstileFSM extends AbstractReactiveFSM {
    public enum TurnstileState implements FSMState {
        LOCKED, UNLOCKED
    }

    public enum TurnstileEvent implements FSMEvent {
        PUSH, COIN, QUICK_PASS, ERROR
    }

    public ReactiveTurnstileFSM() {
        super(TurnstileState.LOCKED);
        final var transitions = Set.of(
                new ReactiveFSMTransition(TurnstileState.LOCKED, TurnstileEvent.COIN, TurnstileState.UNLOCKED, this::logTransition),
                new ReactiveFSMTransition(TurnstileState.LOCKED, TurnstileEvent.PUSH, TurnstileState.LOCKED, this::logTransition),
                new ReactiveFSMTransition(TurnstileState.UNLOCKED, TurnstileEvent.COIN, TurnstileState.UNLOCKED, this::logTransition),
                new ReactiveFSMTransition(TurnstileState.UNLOCKED, TurnstileEvent.PUSH, TurnstileState.LOCKED, this::logTransition),
//                new ReactiveFSMTransition(TurnstileState.LOCKED, TurnstileEvent.QUICK_PASS, TurnstileState.UNLOCKED, this::quickPass),
                new ReactiveFSMTransition(TurnstileState.LOCKED, TurnstileEvent.ERROR, TurnstileState.UNLOCKED, this::throwException),
                new ReactiveFSMTransition(TurnstileState.UNLOCKED, TurnstileEvent.ERROR, TurnstileState.LOCKED, this::throwException)    // TODO: remove
        );
        setTransitions(transitions);

        final var stateActions = Set.of(
                new ReactiveFSMStateActions(TurnstileState.LOCKED, this::logEnterState, this::logExitState),
                new ReactiveFSMStateActions(TurnstileState.UNLOCKED, this::logEnterState, this::logExitState)
        );
        setStateActions(stateActions);
    }

//    @Override
//    protected void beforeEachTransition(FSMState oldState, FSMEvent event, FSMState newState) {
//        System.out.printf("Before transition from %s (on %s) to %s%n", oldState, event, newState);
//    }
//
//    @Override
//    protected void afterEachTransition(FSMState oldState, FSMEvent event, FSMState newState) {
//        System.out.printf("After transition from %s (on %s) to %s%n%n", oldState, event, newState);
//    }

    @Override
    protected Mono<Void> onTransitionException(FSMState oldState, FSMEvent event, FSMState newState, Throwable cause, FSMTransitionStage transitionStage) {
        return Mono.fromRunnable(() -> {
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
        });
    }

    // TODO: consider creating reactive action based on a regular one with a special method/constructor
    private Mono<Void> logTransition(FSMState oldState, FSMEvent event, FSMState newState) {
        // TODO: call some "really" reactive action
//        return Mono.create(s -> {
//            System.out.printf("  Transition: %s on %s -> %s%n", oldState, event, newState);
//            s.success();
//        });
        return Mono.fromRunnable(() -> System.out.printf("  Transition: %s on %s -> %s%n", oldState, event, newState));

        //TODO: Mono.fromRunnable() vs blocking code + return Mono.empty()

//        System.out.printf("  Transition: %s on %s -> %s%n", oldState, event, newState);
//        return Mono.empty();
    }

    private Mono<Void> logEnterState(FSMState oldState, FSMEvent event, FSMState newState) {
        return Mono.fromRunnable(() -> System.out.printf(" Entering the state: %s%n", newState));
    }

    private Mono<Void> logExitState(FSMState oldState, FSMEvent event, FSMState newState) {
        return Mono.fromRunnable(() -> System.out.printf(" Exiting from the state: %s%n", oldState));
    }
//
//    private void quickPass(FSMState oldState, FSMEvent event, FSMState newState) {
//        System.out.printf("~ Transition: %s on %s -> %s ~%n", oldState, event, newState);
//        System.out.println("~ Quick pass triggered ~");
//        triggerAfterwards(TurnstileEvent.PUSH);
//    }
//
    private Mono<Void> throwException(FSMState oldState, FSMEvent event, FSMState newState) {
        return Mono.fromRunnable(() -> {
            System.out.printf("* Transition: %s on %s -> %s *%n", oldState, event, newState);
            System.out.println("* Error is going to happen *");
            var n = 12 / 0;
            System.out.println("Result is " + n);
        });
    }
}
