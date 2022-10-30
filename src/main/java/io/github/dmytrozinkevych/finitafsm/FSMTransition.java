package io.github.dmytrozinkevych.finitafsm;

public record FSMTransition(
        FSMState oldState,
        FSMEvent event,
        TriConsumer<FSMState, FSMEvent, FSMState> action,
        FSMState newState
) { }
