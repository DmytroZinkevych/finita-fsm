package io.github.dmytrozinkevych.finitafsm;

import java.util.Objects;

public record FSMTransition(
        FSMState oldState,
        FSMEvent event,
        TriConsumer<FSMState, FSMEvent, FSMState> action,
        FSMState newState
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
