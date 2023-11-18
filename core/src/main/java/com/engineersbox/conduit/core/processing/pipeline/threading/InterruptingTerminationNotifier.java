package com.engineersbox.conduit.core.processing.pipeline.threading;

import org.jeasy.batch.core.reader.IterableRecordReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InterruptingTerminationNotifier<T> extends IterableRecordReader<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(InterruptingTerminationNotifier.class);

    private final Indicator indicator;

    public InterruptingTerminationNotifier(final Iterable<T> iterable,
                                           final Indicator indicator) {
        super(iterable);
        this.indicator = indicator;
    }

    @Override
    public void close() throws Exception {
        Thread thread;
        while ((thread = this.indicator.ref.get()) == null);
        this.indicator.end.set(true);
        thread.interrupt();
        LOGGER.trace("Interrupted dependent job {} and set termination indicator to true", thread);
        super.close();
    }

}
