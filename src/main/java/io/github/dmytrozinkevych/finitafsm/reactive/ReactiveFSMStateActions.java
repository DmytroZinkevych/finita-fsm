package io.github.dmytrozinkevych.finitafsm.reactive;

import io.github.dmytrozinkevych.finitafsm.FSMEvent;
import io.github.dmytrozinkevych.finitafsm.FSMState;
import io.github.dmytrozinkevych.finitafsm.reactive.utils.ReactiveTriConsumer;

import java.util.Objects;

public record ReactiveFSMStateActions(
        FSMState state,
        ReactiveTriConsumer<FSMState, FSMEvent, FSMState> enterStateAction,
        ReactiveTriConsumer<FSMState, FSMEvent, FSMState> exitStateAction
) {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReactiveFSMStateActions that = (ReactiveFSMStateActions) o;
        return Objects.equals(state, that.state);
    }

    @Override
    public int hashCode() {
        return Objects.hash(state);
    }
}
