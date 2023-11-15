package com.engineersbox.conduit.core.retrieval.ingest.source.provider;

import com.engineersbox.conduit.core.retrieval.ingest.source.Source;

import java.util.function.Supplier;

public class ConstructingSourceProvider<T, R> implements SourceProvider<T, R> {

    private final Supplier<Source<T, R>> constructor;

    public ConstructingSourceProvider(final Supplier<Source<T, R>> constructor) {
        this.constructor = constructor;
    }

    @Override
    public Source<T, R> apply(final Long threadId) {
        return this.constructor.get();
    }
}
