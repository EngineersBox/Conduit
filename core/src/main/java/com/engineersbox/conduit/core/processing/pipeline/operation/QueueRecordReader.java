package com.engineersbox.conduit.core.processing.pipeline.operation;

import org.jeasy.batch.core.reader.RecordReader;
import org.jeasy.batch.core.record.Record;

import java.util.Queue;

public class QueueRecordReader<T, Q extends Queue<Record<T>>> implements RecordReader<T> {

    protected final Q queue;

    public QueueRecordReader(final Q queue) {
        this.queue = queue;
    }

    @Override
    public Record<T> readRecord() {
        return this.queue.poll();
    }
}
