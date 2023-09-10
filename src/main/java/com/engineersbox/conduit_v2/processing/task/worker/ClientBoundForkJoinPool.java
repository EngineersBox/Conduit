package com.engineersbox.conduit_v2.processing.task.worker;

import com.engineersbox.conduit_v2.processing.task.worker.executor.JobExecutorPool;
import org.jeasy.batch.core.job.JobReport;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class ClientBoundForkJoinPool<T, E> extends ForkJoinPool {

    private final JobExecutorPool<E> jobExecutorPool;

    public ClientBoundForkJoinPool(final int parallelism,
                                   final ForkJoinWorkerThreadFactory factory,
                                   final JobExecutorPool<E> jobExecutorPool,
                                   final Thread.UncaughtExceptionHandler handler,
                                   final boolean asyncMode) {
        super(parallelism, factory, handler, asyncMode);
        this.jobExecutorPool = jobExecutorPool;
    }

    public ClientBoundForkJoinPool(final int parallelism,
                                   final ForkJoinWorkerThreadFactory factory,
                                   final JobExecutorPool<E> jobExecutorPool,
                                   final Thread.UncaughtExceptionHandler handler,
                                   final boolean asyncMode,
                                   final int corePoolSize,
                                   final int maximumPoolSize,
                                   final int minimumRunnable,
                                   final Predicate<? super ForkJoinPool> saturate,
                                   final long keepAliveTime,
                                   final TimeUnit unit) {
        super(parallelism, factory, handler, asyncMode, corePoolSize, maximumPoolSize, minimumRunnable, saturate, keepAliveTime, unit);
        this.jobExecutorPool = jobExecutorPool;
    }

    public ForkJoinTask<T> submit(final ClientBoundWorkerTask<T, E> task) {
        return super.submit((ForkJoinTask<T>) new ClientBoundForkJoinTask<>(task, this.jobExecutorPool));
    }

    public List<? extends ForkJoinTask<T>> invokeAll(final ClientBoundWorkerTask<T, E> ...tasks) {
        return Stream.of(tasks)
                .map(this::submit)
                .toList();
    }

    public void invokeAll(final Consumer<? super ForkJoinTask<T>> appender,
                          final ClientBoundWorkerTask<T, E> ...tasks) {
        Stream.of(tasks)
                .map(this::submit)
                .forEach(appender);
    }

    public void waitAll(final ClientBoundWorkerTask<T, E> ...tasks) {
        final List<? extends ForkJoinTask<?>> submittedTasks = invokeAll(tasks);
        submittedTasks.forEach(ForkJoinTask::quietlyJoin);
    }

}
