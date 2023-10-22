package io.github.dmytrozinkevych.finitafsm;

public class FsmTemplate extends AbstractFSM {

    FsmTemplate(FSMState initialState) {
        super(initialState);
    }

    @Override
    protected void beforeEachTransition(FSMState oldState, FSMEvent event, FSMState newState) { }

    @Override
    protected void afterEachTransition(FSMState oldState, FSMEvent event, FSMState newState) { }

    @Override
    protected void onTransitionException(FSMState oldState, FSMEvent event, FSMState newState, Exception cause, FSMTransitionStage transitionStage) { }

    void transitionAction(FSMState oldState, FSMEvent event, FSMState newState) { }

    void onEnterState1(FSMState oldState, FSMEvent event, FSMState newState) { }

    void onExitState1(FSMState oldState, FSMEvent event, FSMState newState) { }

    void onEnterState2(FSMState oldState, FSMEvent event, FSMState newState) { }

    void onExitState2(FSMState oldState, FSMEvent event, FSMState newState) { }
}
