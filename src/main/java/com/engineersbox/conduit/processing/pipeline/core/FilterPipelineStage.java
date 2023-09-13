package com.engineersbox.conduit.processing.pipeline.core;

import com.engineersbox.conduit.processing.pipeline.PipelineStage;
import com.engineersbox.conduit.processing.pipeline.StageResult;
import org.eclipse.collections.api.LazyIterable;
import org.eclipse.collections.impl.collector.Collectors2;

import java.util.function.Predicate;
import java.util.stream.StreamSupport;

public abstract class FilterPipelineStage<T> extends PipelineStage<Iterable<T>, Iterable<T>> implements Predicate<T> {

    protected int filteredCount;

    public FilterPipelineStage(final String name) {
        super(name);
        this.filteredCount = 0;
    }

    @Override
    public abstract boolean test(final T element);

    @Override
    public StageResult<Iterable<T>> invoke(final Iterable<T> previousResult) {
        final LazyIterable<T> result = StreamSupport.stream(previousResult.spliterator(), false)
                .filter(this)
                .collect(Collectors2.toList())
                .asLazy();
        this.filteredCount = result.size();
        return new StageResult<>(
                StageResult.Type.SINGLETON,
                result,
                false
        );
    }
}
