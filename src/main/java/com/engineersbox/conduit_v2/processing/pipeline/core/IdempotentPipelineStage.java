package com.engineersbox.conduit_v2.processing.pipeline.core;

import com.engineersbox.conduit_v2.processing.pipeline.PipelineStage;

import java.util.function.Consumer;

public abstract class IdempotentPipelineStage<T> extends PipelineStage<T, T> implements Consumer<T> {

    public IdempotentPipelineStage(String name) {
        super(name);
    }

    @Override
    public abstract void accept(final T element);

    @Override
    public T invoke(final T previousResult) {
        accept(previousResult);
        return previousResult;
    }
}
