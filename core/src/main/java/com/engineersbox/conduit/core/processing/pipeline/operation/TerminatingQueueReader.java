package com.engineersbox.conduit.core.processing.pipeline.operation;

import org.jeasy.batch.core.record.Record;

import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;

public class TerminatingQueueReader<T, Q extends Queue<Record<T>>, I> extends QueueRecordReader<T, Q> {

    private final I indicator;
    private final BiFunction<Q, I, Record<T>> readOperation;

    public TerminatingQueueReader(final Q queue,
                                  final I indicator,
                                  final BiFunction<Q, I, Record<T>> readOperation) {
        super(queue);
        this.indicator = indicator;
        this.readOperation = readOperation;
    }

    @Override
    public Record<T> readRecord() {
        return this.readOperation.apply(
                super.queue,
                this.indicator
        );
    }

    public static <T, Q extends Queue<Record<T>>> TerminatingQueueReader<T, Q, AtomicBoolean> booleanIndicator(final Q queue,
                                                                                                               final AtomicBoolean indicator) {
        return new TerminatingQueueReader<>(
                queue,
                indicator,
                (final Q queue2, final AtomicBoolean indicator2) -> {
                    Record<T> record;
                    while ((record = queue2.poll()) == null && !indicator2.get());
                    return record;
                }
        );
    }

}
