package com.engineersbox.conduit.core.processing.pipeline.operation;

import org.jeasy.batch.core.job.JobReport;
import org.jeasy.batch.core.listener.JobListener;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class TerminationNotificationListener<I> implements JobListener {

    private final I indicator;
    private final Consumer<I> setIndicatorOperation;

    public TerminationNotificationListener(final I indicator,
                                           final Consumer<I> setIndicatorOperation) {
        this.indicator = indicator;
        this.setIndicatorOperation = setIndicatorOperation;
    }

    public I getIndicator() {
        return this.indicator;
    }

    @Override
    public void afterJob(final JobReport jobReport) {
        this.setIndicatorOperation.accept(this.indicator);
        JobListener.super.afterJob(jobReport);
    }

    public static TerminationNotificationListener<AtomicBoolean> booleanIndicator() {
        return new TerminationNotificationListener<>(
                new AtomicBoolean(false),
                (final AtomicBoolean indicator) -> indicator.set(true)
        );
    }

    public static TerminationNotificationListener<AtomicBoolean> booleanIndicator(final AtomicBoolean indicator) {
        return new TerminationNotificationListener<>(
                indicator,
                (final AtomicBoolean indicator2) -> indicator2.set(true)
        );
    }

}
