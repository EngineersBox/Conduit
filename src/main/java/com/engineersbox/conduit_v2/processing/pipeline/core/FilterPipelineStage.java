package com.engineersbox.conduit_v2.processing.pipeline.core;

import com.engineersbox.conduit_v2.processing.pipeline.PipelineStage;
import com.engineersbox.conduit_v2.processing.pipeline.StageResult;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.impl.collector.Collectors2;
import org.eclipse.collections.impl.factory.Iterables;

import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public abstract class FilterPipelineStage<T> extends PipelineStage<Iterable<T>, Iterable<T>> implements Predicate<T> {


    public FilterPipelineStage(final String name) {
        super(name);
    }

    @Override
    public abstract boolean test(final T element);

    @Override
    public StageResult<Iterable<T>> invoke(final Iterable<T> previousResult) {
        final Iterable<T> result = StreamSupport.stream(previousResult.spliterator(), false)
                .filter(this)
                .collect(Collectors2.toList())
                .asLazy();
        return new StageResult<>(
                StageResult.Type.SINGLE,
                result,
                false
        );
    }
}
