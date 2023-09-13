package com.engineersbox.conduit.processing.task;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class TaskBatcher<T> implements Spliterator<List<T>> {

    private final List<T> source;
    private final int maxChunks;
    private int chunks;
    private int chunkSize;
    private int remaining;
    private int consumed;

    private TaskBatcher(final Collection<T> source,
                        final int batchSize) {
        this.source = source.stream().toList();
        this.chunks = batchSize;
        this.maxChunks = batchSize;
        this.chunkSize = (int) Math.ceil(((double) this.source.size()) / batchSize);
        this.remaining = this.source.size();
    }

    public static <T> Stream<List<T>> partitioned(final Collection<T> source,
                                                  final int batchSize,
                                                  final boolean parallel) {
        final int size = source.size();
        if (size == batchSize) {
            return asSingletonListStream(source);
        } else if (size == 0 || batchSize == 0) {
            return Stream.empty();
        } else if (batchSize == 1) {
            return Stream.of(source.stream().toList());
        } else {
            return StreamSupport.stream(new TaskBatcher<>(source, batchSize), parallel);
        }
    }

    private static <T> Stream<List<T>> asSingletonListStream(final Collection<T> list) {
        final Stream.Builder<List<T>> accumulator = Stream.builder();
        for (final T t : list) {
            accumulator.add(Collections.singletonList(t));
        }
        return accumulator.build();
    }

    @Override
    public boolean tryAdvance(final Consumer<? super List<T>> action) {
        if (this.consumed < this.source.size() && this.chunks != 0) {
            final List<T> batch = this.source.subList(this.consumed, this.consumed + this.chunkSize);
            this.consumed = this.consumed + this.chunkSize;
            this.remaining = this.remaining - this.chunkSize;
            this.chunkSize = (int) Math.ceil(((double) this.remaining) / --this.chunks);
            action.accept(batch);
            return true;
        }
        return false;
    }

    @Override
    public Spliterator<List<T>> trySplit() {
        return null;
    }

    @Override
    public long estimateSize() {
        return maxChunks;
    }

    @Override
    public int characteristics() {
        return ORDERED | SIZED;
    }

}
