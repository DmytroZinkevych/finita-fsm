package io.github.dmytrozinkevych.finitafsm.demo;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import static io.github.dmytrozinkevych.finitafsm.demo.TurnstileFSM.TurnstileEvent;
import static io.github.dmytrozinkevych.finitafsm.demo.TurnstileFSM.TurnstileState;

//TODO: create a benchmark comparison of reactive and non-reactive FSM
class ReactiveTurnstileFSMTest {
    @Test
    void reactiveTurnstileFSMDemo() {
        //TODO: somehow prevent reusing of the Reactive FSM instance. Maybe return Mono<AbstractReactiveFSM> after each trigger() call?
        var reactiveTurnstileFSM = new ReactiveTurnstileFSM();
//        assertEquals(TurnstileState.LOCKED, reactiveTurnstileFSM.getCurrentState());

        System.out.println("\n========== Reactive Turnstile FSM Demo START ==========\n");
//        System.out.println("Initial state: " + reactiveTurnstileFSM.getCurrentState() + "\n");
        System.out.println("Transitions:\n");

        //TODO: use StepVerifier to test if new states after transitions were correct?
        Flux.just(
                TurnstileEvent.PUSH,
                TurnstileEvent.COIN,
                TurnstileEvent.COIN,
//                TurnstileEvent.QUICK_PASS,
                TurnstileEvent.ERROR,
                TurnstileEvent.PUSH
        )
                .flatMap(reactiveTurnstileFSM::trigger)
                .doOnComplete(() -> {
                    System.out.println("\n\n--- State diagram: ---\n");
                    System.out.println(reactiveTurnstileFSM.generatePlantUmlDiagramCode(TurnstileState.LOCKED, TurnstileState.UNLOCKED));

                    System.out.println("\n========== Reactive Turnstile FSM Demo FINISH ==========\n");
                })
                .blockLast();
        // TODO: add unit tests
    }
}
