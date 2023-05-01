package com.engineersbox.conduit_v2.processing.pipeline.core;

import com.engineersbox.conduit_v2.processing.pipeline.PipelineStage;

import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public abstract class FilterPipelineStage<T> extends PipelineStage<Collection<T>, Collection<T>> implements Predicate<T> {


    public FilterPipelineStage(final String name) {
        super(name);
    }

    @Override
    public abstract boolean test(final T element);

    @Override
    public Collection<T> invoke(final Collection<T> previousResult) {
        return previousResult.stream()
                .filter(this)
                .collect(Collectors.toList());
    }
}
