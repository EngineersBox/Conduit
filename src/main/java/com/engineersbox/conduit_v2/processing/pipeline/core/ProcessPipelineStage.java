package com.engineersbox.conduit_v2.processing.pipeline.core;

import com.engineersbox.conduit_v2.processing.pipeline.PipelineStage;
import com.engineersbox.conduit_v2.processing.pipeline.StageType;

import java.util.Collection;
import java.util.function.Function;

public class ProcessPipelineStage<T, R> extends PipelineStage<Collection<T>, Collection<R>> {

    private final Function<T, R> processFunction;

    public ProcessPipelineStage(final String name,
                                final Function<T, R> processFunction) {
        super(name);
        this.processFunction = processFunction;
    }

    @Override
    public Collection<R> apply(final Collection<T> previousResult) {
        return previousResult.stream()
                .map(this.processFunction)
                .toList();
    }
}
