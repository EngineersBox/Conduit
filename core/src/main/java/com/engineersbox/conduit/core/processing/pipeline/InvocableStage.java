package com.engineersbox.conduit.core.processing.pipeline;

@FunctionalInterface
public interface InvocableStage<T, R> {

    StageResult<R> invoke(final T t);

}
