package com.engineersbox.conduit_v2.processing.pipeline.core;

import com.engineersbox.conduit_v2.processing.pipeline.PipelineStage;

import java.util.Collection;
import java.util.function.Function;

public abstract class ProcessPipelineStage<T, R> extends PipelineStage<Collection<T>, Collection<R>> implements Function<T, R> {


    public ProcessPipelineStage(final String name) {
        super(name);
    }

    @Override
    public abstract R apply(final T element);

    @Override
    public Collection<R> invoke(final Collection<T> previousResult) {
        return previousResult.stream()
                .map(this)
                .toList();
    }
}
