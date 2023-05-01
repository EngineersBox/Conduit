package com.engineersbox.conduit_v2.processing.pipeline.core;

import com.engineersbox.conduit_v2.processing.pipeline.PipelineStage;

import java.util.function.Consumer;

public abstract class TerminatingPipelineStage<T> extends PipelineStage<T, Void> implements Consumer<T> {


    public TerminatingPipelineStage(final String name) {
        super(name);
    }

    @Override
    public abstract void accept(T t);

    @Override
    public Void invoke(final T previousResult) {
        this.accept(previousResult);
        return null;
    }
}
