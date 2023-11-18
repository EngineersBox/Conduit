package com.engineersbox.conduit.core.processing.pipeline.operation;

import org.jeasy.batch.core.record.Batch;
import org.jeasy.batch.core.record.Record;
import org.jeasy.batch.core.writer.RecordWriter;

import java.util.Queue;

public class QueueRecordWriter<T, Q extends Queue<Record<T>>> implements RecordWriter<T> {

    private final Q queue;

    public QueueRecordWriter(final Q queue) {
        this.queue = queue;
    }

    @Override
    public void writeRecords(final Batch<T> batch) {
        for (final Record<T> record : batch) {
            this.queue.offer(record);
        }
    }
}
