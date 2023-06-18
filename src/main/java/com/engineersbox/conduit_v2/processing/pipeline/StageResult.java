package com.engineersbox.conduit_v2.processing.pipeline;

public record StageResult<T>(Type type,
                          T result,
                          boolean terminate) {

    public enum Type {
        SPLIT,
        COMBINE,
        SINGLE
    }

}
