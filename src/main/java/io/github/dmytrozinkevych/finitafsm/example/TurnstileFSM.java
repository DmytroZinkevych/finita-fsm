package io.github.dmytrozinkevych.finitafsm.example;

import io.github.dmytrozinkevych.finitafsm.AbstractFSM;
import io.github.dmytrozinkevych.finitafsm.FSMEvent;
import io.github.dmytrozinkevych.finitafsm.FSMState;
import io.github.dmytrozinkevych.finitafsm.FSMTransition;

import java.util.List;

public class TurnstileFSM extends AbstractFSM {

    public enum State implements FSMState {
        LOCKED, UNLOCKED;
    }

    public enum Event implements FSMEvent {
        PUSH, COIN;
    }

    public TurnstileFSM() {
        super();
        final List<FSMTransition> transitions = List.of(
                new FSMTransition(State.LOCKED, Event.COIN, this::logTransition, State.UNLOCKED),
                new FSMTransition(State.LOCKED, Event.PUSH, this::logTransition, State.LOCKED),
                new FSMTransition(State.UNLOCKED, Event.COIN, this::logTransition, State.UNLOCKED),
                new FSMTransition(State.UNLOCKED, Event.PUSH, this::logTransition, State.LOCKED)
        );
        setTransitions(transitions);
    }

    private void logTransition(FSMState currentState, FSMEvent event, FSMState newState) {
        System.out.printf("Transition: %s on %s -> %s%n", currentState, event, newState);
    }
}
