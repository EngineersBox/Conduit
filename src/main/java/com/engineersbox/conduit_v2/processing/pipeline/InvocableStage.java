package com.engineersbox.conduit_v2.processing.pipeline;

@FunctionalInterface
public interface InvocableStage<T, R> {

    StageResult<R> invoke(final T t);

}
