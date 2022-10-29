package io.github.dmytrozinkevych.finitafsm.example;

import io.github.dmytrozinkevych.finitafsm.AbstractStateMachine;
import io.github.dmytrozinkevych.finitafsm.FSMEvent;
import io.github.dmytrozinkevych.finitafsm.FSMState;
import io.github.dmytrozinkevych.finitafsm.FSMTransition;

import java.util.Set;

public class TurnstileFSM extends AbstractStateMachine {

    public enum State implements FSMState {
        LOCKED, UNLOCKED;

        @Override
        public String getName() {
            return this.toString();
        }

    }

    public enum Event implements FSMEvent {
        PUSH, COIN;

        @Override
        public String getName() {
            return this.toString();
        }
    }

    private static final Set<FSMTransition> transitions = Set.of(
            new FSMTransition(State.LOCKED, Event.COIN, TurnstileFSM::logTransition, State.UNLOCKED),
            new FSMTransition(State.LOCKED, Event.PUSH, TurnstileFSM::logTransition, State.LOCKED),
            new FSMTransition(State.UNLOCKED, Event.COIN, TurnstileFSM::logTransition, State.UNLOCKED),
            new FSMTransition(State.UNLOCKED, Event.PUSH, TurnstileFSM::logTransition, State.LOCKED)
    );

    public TurnstileFSM() {
        super(transitions);
    }

    private static void logTransition() {
        // TODO: make non-static, add info about transition as arguments
        System.err.println("Transition");
    }
}
