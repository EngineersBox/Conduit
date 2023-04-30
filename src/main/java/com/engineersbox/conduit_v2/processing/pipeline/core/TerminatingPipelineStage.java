package com.engineersbox.conduit_v2.processing.pipeline.core;

import com.engineersbox.conduit_v2.processing.pipeline.PipelineStage;

import java.util.function.Consumer;

public class TerminatingPipelineStage<T> extends PipelineStage<T, Void> {

    private final Consumer<T> consumer;

    public TerminatingPipelineStage(final String name,
                                    final Consumer<T> consumer) {
        super(name);
        this.consumer = consumer;
    }

    @Override
    public Void apply(final T previousResult) {
        this.consumer.accept(previousResult);
        return null;
    }
}
