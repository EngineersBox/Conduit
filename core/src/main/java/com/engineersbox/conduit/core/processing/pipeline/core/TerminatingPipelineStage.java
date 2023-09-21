package com.engineersbox.conduit.core.processing.pipeline.core;

import com.engineersbox.conduit.core.processing.pipeline.StageResult;
import com.engineersbox.conduit.core.processing.pipeline.PipelineStage;

import java.util.function.Consumer;

public abstract class TerminatingPipelineStage<T> extends PipelineStage<T, Void> implements Consumer<T> {


    public TerminatingPipelineStage(final String name) {
        super(name);
    }

    @Override
    public abstract void accept(T t);

    @Override
    public StageResult<Void> invoke(final T previousResult) {
        this.accept(previousResult);
        return new StageResult<>(
                StageResult.Type.SINGLETON,
                null,
                true
        );
    }
}
