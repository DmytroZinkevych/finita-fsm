package io.github.dmytrozinkevych.finitafsm.reactive;

import io.github.dmytrozinkevych.finitafsm.FSMEvent;
import io.github.dmytrozinkevych.finitafsm.FSMState;
import io.github.dmytrozinkevych.finitafsm.reactive.utils.ReactiveTriConsumer;

import java.util.Objects;

public record ReactiveFSMTransition(
        FSMState oldState,
        FSMEvent event,
        FSMState newState,
        ReactiveTriConsumer<FSMState, FSMEvent, FSMState> action
) {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReactiveFSMTransition that = (ReactiveFSMTransition) o;
        return Objects.equals(oldState, that.oldState)
                && Objects.equals(event, that.event)
                && Objects.equals(newState, that.newState);
    }

    @Override
    public int hashCode() {
        return Objects.hash(oldState, event, newState);
    }
}
