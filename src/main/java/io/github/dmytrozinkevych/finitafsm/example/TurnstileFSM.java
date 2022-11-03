package io.github.dmytrozinkevych.finitafsm.example;

import io.github.dmytrozinkevych.finitafsm.AbstractFSM;
import io.github.dmytrozinkevych.finitafsm.FSMEvent;
import io.github.dmytrozinkevych.finitafsm.FSMState;
import io.github.dmytrozinkevych.finitafsm.FSMTransition;

import java.util.Set;

public class TurnstileFSM extends AbstractFSM {

    public enum State implements FSMState {
        LOCKED, UNLOCKED;
    }

    public enum Event implements FSMEvent {
        PUSH, COIN;
    }

    public TurnstileFSM() {
        super(State.LOCKED);
        final var transitions = Set.of(
                new FSMTransition(State.LOCKED, Event.COIN, this::logTransition, State.UNLOCKED),
                new FSMTransition(State.LOCKED, Event.PUSH, this::logTransition, State.LOCKED),
                new FSMTransition(State.UNLOCKED, Event.COIN, this::logTransition, State.UNLOCKED),
                new FSMTransition(State.UNLOCKED, Event.PUSH, this::logTransition, State.LOCKED)
        );
        setTransitions(transitions);
    }

    @Override
    protected void beforeEachTransition(FSMState oldState, FSMEvent event, FSMState newState) {
        System.out.printf("--- Before transition from %s (on %s) to %s ---%n", oldState, event, newState);
    }

    @Override
    protected void afterEachTransition(FSMState oldState, FSMEvent event, FSMState newState) {
        System.out.printf("--- After transition from %s (on %s) to %s ---%n%n", oldState, event, newState);
    }

    private void logTransition(FSMState oldState, FSMEvent event, FSMState newState) {
        System.out.printf("Transition: %s on %s -> %s%n", oldState, event, newState);
    }
}
