package com.engineersbox.conduit_v2.processing.task.worker;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

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

    public ForkJoinTask<?> submit(final ClientBoundWorkerTask task) {
        return super.submit((ForkJoinTask<Void>) new ClientBoundForkJoinTask(task));
    }

    public List<? extends ForkJoinTask<?>> invokeAll(final ClientBoundWorkerTask ...tasks) {
        return Stream.of(tasks)
                .map(this::submit)
                .toList();
    }

    public void invokeAll(final Consumer<? super ForkJoinTask<?>> appender,
                          final ClientBoundWorkerTask ...tasks) {
        Stream.of(tasks)
                .map(this::submit)
                .forEach(appender);
    }

    public void waitAll(final ClientBoundWorkerTask ...tasks) {
        final List<? extends ForkJoinTask<?>> submittedTasks = invokeAll(tasks);
        submittedTasks.forEach(ForkJoinTask::quietlyJoin);
    }

}
