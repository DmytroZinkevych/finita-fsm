package io.github.dmytrozinkevych.finitafsm.example;

import io.github.dmytrozinkevych.finitafsm.AbstractStateMachine;
import io.github.dmytrozinkevych.finitafsm.FSMEvent;
import io.github.dmytrozinkevych.finitafsm.FSMState;
import io.github.dmytrozinkevych.finitafsm.FSMTransition;

import java.util.List;

public class TurnstileFSM extends AbstractStateMachine {

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

    private void logTransition() {
        // TODO: add info about transition as arguments
        System.err.println("Transition");
    }
}
