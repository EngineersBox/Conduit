package com.engineersbox.conduit_v2.processing.pipeline.core;

import com.engineersbox.conduit_v2.processing.pipeline.PipelineStage;
import com.engineersbox.conduit_v2.processing.pipeline.StageResult;

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
                StageResult.Type.SINGLE,
                previousResult,
                false
        );
    }
}
