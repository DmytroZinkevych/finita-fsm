package io.github.dmytrozinkevych.finitafsm;

import java.util.Set;

import static io.github.dmytrozinkevych.finitafsm.TestUtils.throwArithmeticException;

public class TestExceptionBeforeTriggeringNextEventFSM extends AbstractFSM {

    protected TestExceptionBeforeTriggeringNextEventFSM() {
        super(State.S1);
        var transitions = Set.of(
                new FSMTransition(State.S1, Event.E1, State.S2, this::actionWithTrigger),
                new FSMTransition(State.S2, Event.E2, State.S3, this::forbiddenAction),
                new FSMTransition(State.S1, Event.E3, State.S2, this::regularAction)
        );
        setTransitions(transitions);
    }

    @Override
    protected void onTransitionException(FSMState oldState, FSMEvent event, FSMState newState, Exception cause, FSMTransitionStage transitionStage) { }

    void actionWithTrigger(FSMState oldState, FSMEvent event, FSMState newState) {
        triggerAfterwards(Event.E2);
        throwArithmeticException(oldState, event, newState);
    }

    void regularAction(FSMState oldState, FSMEvent event, FSMState newState) { }

    void forbiddenAction(FSMState oldState, FSMEvent event, FSMState newState) { }
}
