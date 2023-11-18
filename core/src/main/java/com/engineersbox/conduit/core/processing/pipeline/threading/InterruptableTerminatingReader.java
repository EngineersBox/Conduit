package com.engineersbox.conduit.core.processing.pipeline.threading;

import com.engineersbox.conduit.core.util.Functional;
import org.jeasy.batch.core.reader.RecordReader;
import org.jeasy.batch.core.record.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InterruptableTerminatingReader<T> implements RecordReader<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(InterruptingTerminationNotifier.class);

    private final Functional.ThrowsSupplier<Record<T>> readOperation;
    private final Indicator indicator;

    public InterruptableTerminatingReader(final Functional.ThrowsSupplier<Record<T>> readOperation,
                                          final Indicator indicator) {
        this.readOperation = readOperation;
        this.indicator = indicator;
    }

    @Override
    public Record<T> readRecord() throws Exception {
        this.indicator.ref.compareAndSet(null, Thread.currentThread());
        Record<T> record = null;
        try {
            record = this.readOperation.get();
        } catch (final InterruptedException ie) {
            LOGGER.trace("Encountered interrupt during read with indicator state {} and record {}", this.indicator.end.get(), record);
            // Intended
            record = this.readOperation.get();
        }
        return this.indicator.end.get() ? record : null;
    }
}
