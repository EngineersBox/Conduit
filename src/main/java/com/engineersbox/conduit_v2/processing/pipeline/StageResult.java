package com.engineersbox.conduit_v2.processing.pipeline;

public record StageResult<T>(Type type,
                             int combineCount,
                             T result,
                             boolean terminate) {

    public StageResult(final Type type,
                       final T result,
                       final boolean terminate) {
        this(
                type,
                0,
                result,
                terminate
        );
    }

    public enum Type {
        SPLIT,
        COMBINE,
        SINGLETON
    }

}
