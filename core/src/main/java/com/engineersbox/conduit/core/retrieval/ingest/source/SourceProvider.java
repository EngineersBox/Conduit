package com.engineersbox.conduit.core.retrieval.ingest.source;

import java.util.function.Function;
import java.util.function.Supplier;

public interface SourceProvider<T> extends Function<Long, Source<T>> {
    @Override
    Source<T> apply(final Long threadId);

    static <T> SourceProvider<T> perInvocation(final Supplier<Source<T>> constructor) {
        return new ConstructingSourceProvider<>(constructor);
    }

    static <T> SourceProvider<T> universal(final Source<T> instance) {
        return new UniversalSourceProvider<>(instance);
    }

    class ConstructingSourceProvider<T> implements SourceProvider<T> {

        private final Supplier<Source<T>> constructor;

        public ConstructingSourceProvider(final Supplier<Source<T>> constructor) {
            this.constructor = constructor;
        }

        @Override
        public Source<T> apply(final Long threadId) {
            return this.constructor.get();
        }
    }

    class UniversalSourceProvider<T> implements SourceProvider<T> {

        private final Source<T> source;

        public UniversalSourceProvider(final Source<T> source) {
            this.source = source;
        }

        @Override
        public Source<T> apply(final Long threadId) {
            return this.source;
        }
    }

}
