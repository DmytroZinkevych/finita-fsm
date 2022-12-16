package io.github.dmytrozinkevych.finitafsm;

public enum FSMTransitionStage {
    BEFORE_TRANSITION, EXIT_OLD_STATE, TRANSITION_ACTION, ENTER_NEW_STATE, AFTER_TRANSITION
}
