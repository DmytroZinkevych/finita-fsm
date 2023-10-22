package io.github.dmytrozinkevych.finitafsm;

import java.util.Set;

import static io.github.dmytrozinkevych.finitafsm.Event.EVENT_1;
import static io.github.dmytrozinkevych.finitafsm.State.STATE_1;
import static io.github.dmytrozinkevych.finitafsm.State.STATE_2;
import static io.github.dmytrozinkevych.finitafsm.TestUtils.throwArithmeticException;

public class TestEnterStateExceptionFsm extends FsmTemplate {

    public TestEnterStateExceptionFsm() {
        super(STATE_1);
        var transitions = Set.of(
                new FSMTransition(STATE_1, EVENT_1, STATE_2, this::transitionAction)
        );
        setTransitions(transitions);

        var stateActions = Set.of(
                new FSMStateActions(STATE_1, this::onEnterState1, this::onExitState1),
                new FSMStateActions(STATE_2, this::onEnterState2, this::onExitState2)
        );
        setStateActions(stateActions);
    }

    @Override
    void onEnterState2(FSMState oldState, FSMEvent event, FSMState newState) {
        throwArithmeticException();
    }
}
