package com.engineersbox.conduit.core.retrieval.ingest.source.provider;

import com.engineersbox.conduit.core.retrieval.ingest.source.Source;

import java.util.function.Supplier;

public class ThreadSourceProvider<T, R> implements SourceProvider<T, R> {

    private final ThreadLocal<Source<T, R>> source;
    private final Supplier<Source<T, R>> sourceSupplier;

    public ThreadSourceProvider(final Supplier<Source<T, R>> sourceSupplier) {
        this.sourceSupplier = sourceSupplier;
        this.source = new ThreadLocal<>();
    }

    @Override
    public Source<T, R> apply(final Long threadId) {
        Source<T, R> value = this.source.get();
        if (value == null) {
            value = this.sourceSupplier.get();
            this.source.set(value);
        }
        return value;
    }
}
