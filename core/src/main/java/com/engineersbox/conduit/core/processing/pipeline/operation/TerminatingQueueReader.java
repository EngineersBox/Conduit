package com.engineersbox.conduit.core.processing.pipeline.operation;

import org.jeasy.batch.core.record.Record;

import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

public class TerminatingQueueReader<T, E, Q extends Queue<E>, I> extends QueueRecordReader<T, E, Q> {

    protected final I indicator;
    protected final Function<TerminatingQueueReader<T,E,Q,I>, Record<T>> readOperation;

    public TerminatingQueueReader(final Q queue,
                                  final Function<E, Record<T>> elementAdapter,
                                  final I indicator,
                                  final Function<TerminatingQueueReader<T,E,Q,I>, Record<T>> readOperation) {
        super(queue, elementAdapter);
        this.indicator = indicator;
        this.readOperation = readOperation;
    }

    @Override
    public Record<T> readRecord() {
        return this.readOperation.apply(this);
    }

    public static <T, E, Q extends Queue<E>> TerminatingQueueReader<T, E, Q, AtomicBoolean> booleanIndicator(final Q queue,
                                                                                                             final Function<E, Record<T>> elementAdapter,
                                                                                                             final AtomicBoolean indicator) {
        return new TerminatingQueueReader<>(
                queue,
                elementAdapter,
                indicator,
                (final TerminatingQueueReader<T,E,Q, AtomicBoolean> _this) -> {
                    Record<T> record;
                    while (
                            (record = _this.elementAdapter.apply(_this.queue.poll())) == null
                            && !_this.indicator.get()
                    );
                    return record;
                }
        );
    }

}
