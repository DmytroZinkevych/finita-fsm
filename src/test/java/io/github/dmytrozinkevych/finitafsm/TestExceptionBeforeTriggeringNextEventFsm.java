package io.github.dmytrozinkevych.finitafsm;

import java.util.Set;

import static io.github.dmytrozinkevych.finitafsm.Event.*;
import static io.github.dmytrozinkevych.finitafsm.State.*;
import static io.github.dmytrozinkevych.finitafsm.TestUtils.throwArithmeticException;

public class TestExceptionBeforeTriggeringNextEventFsm extends AbstractFSM {

    TestExceptionBeforeTriggeringNextEventFsm() {
        super(STATE_1);
        var transitions = Set.of(
                new FSMTransition(STATE_1, EVENT_1, STATE_2, this::actionWithTrigger),
                new FSMTransition(STATE_2, EVENT_2, STATE_3, this::forbiddenAction),
                new FSMTransition(STATE_1, EVENT_3, STATE_2, this::regularAction)
        );
        setTransitions(transitions);
    }

    @Override
    protected void onTransitionException(FSMState oldState, FSMEvent event, FSMState newState, Exception cause, FSMTransitionStage transitionStage) { }

    void actionWithTrigger(FSMState oldState, FSMEvent event, FSMState newState) {
        triggerAfterwards(EVENT_2);
        throwArithmeticException();
    }

    void regularAction(FSMState oldState, FSMEvent event, FSMState newState) { }

    void forbiddenAction(FSMState oldState, FSMEvent event, FSMState newState) { }
}
