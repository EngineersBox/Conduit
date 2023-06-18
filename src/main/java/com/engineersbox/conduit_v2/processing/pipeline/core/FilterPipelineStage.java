package com.engineersbox.conduit_v2.processing.pipeline.core;

import com.engineersbox.conduit_v2.processing.pipeline.PipelineStage;
import com.engineersbox.conduit_v2.processing.pipeline.StageResult;

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
    public StageResult<Collection<T>> invoke(final Collection<T> previousResult) {
        final Collection<T> result = previousResult.stream()
                .filter(this)
                .collect(Collectors.toList());
        return new StageResult<>(
                StageResult.Type.SINGLE,
                result,
                false
        );
    }
}
