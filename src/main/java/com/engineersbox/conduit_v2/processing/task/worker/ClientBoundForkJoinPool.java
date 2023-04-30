package com.engineersbox.conduit_v2.processing.task.worker;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

public class ClientBoundForkJoinPool extends ForkJoinPool {

    public ClientBoundForkJoinPool() {}

    public ClientBoundForkJoinPool(final int parallelism) {
        super(parallelism);
    }

    public ClientBoundForkJoinPool(final int parallelism,
                                   final ForkJoinWorkerThreadFactory factory,
                                   final Thread.UncaughtExceptionHandler handler,
                                   final boolean asyncMode) {
        super(parallelism, factory, handler, asyncMode);
    }

    public ClientBoundForkJoinPool(final int parallelism,
                                   final ForkJoinWorkerThreadFactory factory,
                                   final Thread.UncaughtExceptionHandler handler,
                                   final boolean asyncMode,
                                   final int corePoolSize,
                                   final int maximumPoolSize,
                                   final int minimumRunnable,
                                   final Predicate<? super ForkJoinPool> saturate,
                                   final long keepAliveTime,
                                   final TimeUnit unit) {
        super(parallelism, factory, handler, asyncMode, corePoolSize, maximumPoolSize, minimumRunnable, saturate, keepAliveTime, unit);
    }

    public ForkJoinTask<?> submit(final ClientBoundWorkerTask runnable) {
        return super.submit((ForkJoinTask<Void>) new ClientBoundForkJoinTask(runnable));
    }

}
