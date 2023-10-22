package io.github.dmytrozinkevych.finitafsm;

import java.util.Set;

import static io.github.dmytrozinkevych.finitafsm.Event.EVENT_1;
import static io.github.dmytrozinkevych.finitafsm.Event.EVENT_2;
import static io.github.dmytrozinkevych.finitafsm.State.*;

public class TestTriggerAfterwardsFsm extends AbstractFSM {

    TestTriggerAfterwardsFsm() {
        super(STATE_1);
        final var transitions = Set.of(
                new FSMTransition(STATE_1, EVENT_1, STATE_2, this::transitionActionWithTriggerAfterwards),
                new FSMTransition(STATE_2, EVENT_2, STATE_3, this::regularTransitionAction)
        );
        setTransitions(transitions);

        var stateActions = Set.of(
                new FSMStateActions(STATE_1, this::onEnterState1, this::onExitState1),
                new FSMStateActions(STATE_2, this::onEnterState2, this::onExitState2),
                new FSMStateActions(STATE_3, this::onEnterState3, this::onExitState3)
        );
        setStateActions(stateActions);
    }

    void transitionActionWithTriggerAfterwards(FSMState oldState, FSMEvent event, FSMState newState) {
        triggerAfterwards(EVENT_2);
    }

    void regularTransitionAction(FSMState oldState, FSMEvent event, FSMState newState) { }

    void onEnterState1(FSMState oldState, FSMEvent event, FSMState newState) { }

    void onExitState1(FSMState oldState, FSMEvent event, FSMState newState) { }

    void onEnterState2(FSMState oldState, FSMEvent event, FSMState newState) { }

    void onExitState2(FSMState oldState, FSMEvent event, FSMState newState) { }

    void onEnterState3(FSMState oldState, FSMEvent event, FSMState newState) { }

    void onExitState3(FSMState oldState, FSMEvent event, FSMState newState) { }
}
