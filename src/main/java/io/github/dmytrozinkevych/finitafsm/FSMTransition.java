package io.github.dmytrozinkevych.finitafsm;

import io.github.dmytrozinkevych.finitafsm.utils.TriConsumer;

import java.util.Objects;

public record FSMTransition(
        FSMState oldState,
        FSMEvent event,
        FSMState newState,
        TriConsumer<FSMState, FSMEvent, FSMState> action
) {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FSMTransition that = (FSMTransition) o;
        return Objects.equals(oldState, that.oldState)
                && Objects.equals(event, that.event)
                && Objects.equals(newState, that.newState);
    }

    @Override
    public int hashCode() {
        return Objects.hash(oldState, event, newState);
    }
}
