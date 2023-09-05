package com.engineersbox.conduit_v2.processing.task.worker;

import com.engineersbox.conduit_v2.processing.task.worker.executor.JobExecutorPool;
import org.jeasy.batch.core.job.JobReport;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class ClientBoundForkJoinPool extends ForkJoinPool {

    private final JobExecutorPool jobExecutorPool;

    public ClientBoundForkJoinPool(final int parallelism,
                                   final ForkJoinWorkerThreadFactory factory,
                                   final JobExecutorPool jobExecutorPool,
                                   final Thread.UncaughtExceptionHandler handler,
                                   final boolean asyncMode) {
        super(parallelism, factory, handler, asyncMode);
        this.jobExecutorPool = jobExecutorPool;
    }

    public ClientBoundForkJoinPool(final int parallelism,
                                   final ForkJoinWorkerThreadFactory factory,
                                   final JobExecutorPool jobExecutorPool,
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

    public ForkJoinTask<List<Future<JobReport>>> submit(final ClientBoundWorkerTask task) {
        return super.submit((ForkJoinTask<List<Future<JobReport>>>) new ClientBoundForkJoinTask(task, this.jobExecutorPool));
    }

    public List<? extends ForkJoinTask<List<Future<JobReport>>>> invokeAll(final ClientBoundWorkerTask ...tasks) {
        return Stream.of(tasks)
                .map(this::submit)
                .toList();
    }

    public void invokeAll(final Consumer<? super ForkJoinTask<List<Future<JobReport>>>> appender,
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
