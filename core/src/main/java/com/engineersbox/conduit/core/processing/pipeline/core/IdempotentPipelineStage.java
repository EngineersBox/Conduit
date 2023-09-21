package com.engineersbox.conduit.core.processing.pipeline.core;

import com.engineersbox.conduit.core.processing.pipeline.StageResult;
import com.engineersbox.conduit.core.processing.pipeline.PipelineStage;

import java.util.function.Consumer;

public abstract class IdempotentPipelineStage<T> extends PipelineStage<T, T> implements Consumer<T> {

    public IdempotentPipelineStage(String name) {
        super(name);
    }

    @Override
    public abstract void accept(final T element);

    @Override
    public StageResult<T> invoke(final T previousResult) {
        accept(previousResult);
        return new StageResult<>(
                StageResult.Type.SINGLETON,
                previousResult,
                false
        );
    }
}
