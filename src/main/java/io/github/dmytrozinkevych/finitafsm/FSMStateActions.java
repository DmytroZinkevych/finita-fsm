package io.github.dmytrozinkevych.finitafsm;

public record FSMStateActions(
        FSMState state,
        TriConsumer<FSMState, FSMEvent, FSMState> onEnterState,
        TriConsumer<FSMState, FSMEvent, FSMState> onExitState
) { }
