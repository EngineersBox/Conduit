package com.engineersbox.conduit.core.processing.pipeline.operation;

import org.jeasy.batch.core.reader.RecordReader;
import org.jeasy.batch.core.record.Record;

import java.util.Queue;
import java.util.function.Function;

public class QueueRecordReader<T, E, Q extends Queue<E>> implements RecordReader<T> {

    protected final Q queue;
    protected final Function<E, Record<T>> elementAdapter;

    public QueueRecordReader(final Q queue,
                             final Function<E, Record<T>> elementAdapter) {
        this.queue = queue;
        this.elementAdapter = elementAdapter;
    }

    @Override
    public Record<T> readRecord() {
        return this.elementAdapter.apply(this.queue.poll());
    }

    public static <T, Q extends Queue<Record<T>>> QueueRecordReader<T,Record<T>,Q> identityReader(final Q queue) {
        return new QueueRecordReader<>(
                queue,
                Function.identity()
        );
    }

}
