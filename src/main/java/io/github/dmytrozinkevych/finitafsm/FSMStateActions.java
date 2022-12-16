package io.github.dmytrozinkevych.finitafsm;

import io.github.dmytrozinkevych.finitafsm.utils.TriConsumer;

import java.util.Objects;

public record FSMStateActions(
        FSMState state,
        TriConsumer<FSMState, FSMEvent, FSMState> enterStateAction,
        TriConsumer<FSMState, FSMEvent, FSMState> exitStateAction
) {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FSMStateActions that = (FSMStateActions) o;
        return Objects.equals(state, that.state);
    }

    @Override
    public int hashCode() {
        return Objects.hash(state);
    }
}
