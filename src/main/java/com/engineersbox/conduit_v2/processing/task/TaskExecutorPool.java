package com.engineersbox.conduit_v2.processing.task;

import com.engineersbox.conduit_v2.processing.task.worker.ClientBoundForkJoinPool;
import com.engineersbox.conduit_v2.processing.task.worker.ClientBoundWorkerTask;
import com.engineersbox.conduit_v2.processing.task.worker.QueuedForkJoinWorkerThreadFactory;
import com.engineersbox.conduit_v2.processing.task.worker.client.ClientPool;
import com.engineersbox.conduit_v2.processing.task.worker.executor.JobExecutorPool;
import org.jeasy.batch.core.job.JobReport;

import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.Future;
import java.util.function.Consumer;

public class TaskExecutorPool<T, E> {

    private final ClientBoundForkJoinPool<T, E> executorService;

    public TaskExecutorPool(final ClientPool clientProvider,
                            final JobExecutorPool<E> jobExecutorPool,
                            final int parallelism) {
        this.executorService = newWorkStealingPool(
                parallelism,
                jobExecutorPool,
                new QueuedForkJoinWorkerThreadFactory(clientProvider)
        );
    }

    private ClientBoundForkJoinPool<T, E> newWorkStealingPool(final int parallelism,
                                                              final JobExecutorPool<E> jobExecutorPool,
                                                              final ForkJoinPool.ForkJoinWorkerThreadFactory workerThreadFactory) {
        return new ClientBoundForkJoinPool<>(
                parallelism > 0 ? parallelism : Runtime.getRuntime().availableProcessors(),
                workerThreadFactory,
                jobExecutorPool,
                null,
                true
        );
    }

    public ForkJoinTask<T> submit(final ClientBoundWorkerTask<T, E> task) {
        return this.executorService.submit(task);
    }

    public List<? extends ForkJoinTask<T>> invokeAll(final ClientBoundWorkerTask<T, E> ...tasks) {
        return this.executorService.invokeAll(tasks);
    }

    public void invokeAll(final Consumer<? super ForkJoinTask<T>> appender,
                          final ClientBoundWorkerTask<T, E> ...tasks) {
        this.executorService.invokeAll(appender, tasks);
    }

    public void waitAll(final ClientBoundWorkerTask<T, E> ...tasks) {
        this.executorService.waitAll(tasks);
    }

}
