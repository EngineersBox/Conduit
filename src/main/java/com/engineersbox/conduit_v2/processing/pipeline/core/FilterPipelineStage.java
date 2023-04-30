package com.engineersbox.conduit_v2.processing.pipeline.core;

import com.engineersbox.conduit_v2.processing.pipeline.PipelineStage;
import com.engineersbox.conduit_v2.processing.pipeline.StageType;

import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class FilterPipelineStage<T> extends PipelineStage<Collection<T>, Collection<T>> {

    private final Predicate<T> filterPredicate;

    public FilterPipelineStage(final String name,
                               final Predicate<T> filterPredicate) {
        super(name);
        this.filterPredicate = filterPredicate;
    }

    @Override
    public Collection<T> apply(final Collection<T> previousResult) {
        return previousResult.stream()
                .filter(this.filterPredicate)
                .collect(Collectors.toList());
    }
}
