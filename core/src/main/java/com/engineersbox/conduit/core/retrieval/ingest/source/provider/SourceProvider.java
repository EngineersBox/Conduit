package com.engineersbox.conduit.core.retrieval.ingest.source.provider;

import com.engineersbox.conduit.core.retrieval.ingest.source.Source;

import java.util.function.Function;
import java.util.function.Supplier;

public interface SourceProvider<T, R> extends Function<Long, Source<T, R>> {
    @Override
    Source<T, R> apply(final Long threadId);

    static <T, R> SourceProvider<T, R> perInvocation(final Supplier<Source<T, R>> constructor) {
        return new ConstructingSourceProvider<>(constructor);
    }

    static <T, R> SourceProvider<T, R> universal(final Source<T, R> instance) {
        return new UniversalSourceProvider<>(instance);
    }

    static <T, R> SourceProvider<T, R> threaded(final Supplier<Source<T, R>> threadInstanceSupplier) {
        return new ThreadSourceProvider<>(threadInstanceSupplier);
    }

}
