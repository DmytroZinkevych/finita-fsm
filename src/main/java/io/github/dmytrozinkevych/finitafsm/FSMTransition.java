package io.github.dmytrozinkevych.finitafsm;

public record FSMTransition(FSMState oldState, FSMEvent event, Runnable action, FSMState newState) {}
