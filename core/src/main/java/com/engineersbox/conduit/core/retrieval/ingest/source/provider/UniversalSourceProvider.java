package com.engineersbox.conduit.core.retrieval.ingest.source.provider;

import com.engineersbox.conduit.core.retrieval.ingest.source.Source;

public class UniversalSourceProvider<T, R> implements SourceProvider<T, R> {

    private final Source<T, R> source;

    public UniversalSourceProvider(final Source<T, R> source) {
        this.source = source;
    }

    @Override
    public Source<T, R> apply(final Long threadId) {
        return this.source;
    }
}
