package com.engineersbox.conduit.core.processing.pipeline.operation;

import org.jeasy.batch.core.record.Batch;
import org.jeasy.batch.core.record.Record;
import org.jeasy.batch.core.writer.RecordWriter;

import java.util.Queue;
import java.util.function.Function;

public class QueueRecordWriter<T, E, Q extends Queue<E>> implements RecordWriter<T> {

    private final Q queue;
    private final Function<Record<T>, E> elementAdapter;

    public QueueRecordWriter(final Q queue,
                             final Function<Record<T>,E> elementAdapter) {
        this.queue = queue;
        this.elementAdapter = elementAdapter;
    }

    @Override
    public void writeRecords(final Batch<T> batch) {
        for (final Record<T> record : batch) {
            this.queue.offer(this.elementAdapter.apply(record));
        }
    }
}
