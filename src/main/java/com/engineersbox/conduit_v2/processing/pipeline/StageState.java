package com.engineersbox.conduit_v2.processing.pipeline;

public enum StageState {
    PENDING,
    EXECUTING,
    FINISHED,
    EVICTED;

    public StageState next() {
        return isLast() ? null : StageState.values()[this.ordinal() + 1];
    }

    public boolean isLast() {
        return this.ordinal() == StageState.values().length - 2;
    }

}
