package com.engineersbox.conduit_v2.processing.task;

import com.engineersbox.conduit_v2.processing.task.worker.ClientBoundForkJoinPool;
import com.engineersbox.conduit_v2.processing.task.worker.ClientBoundWorkerTask;
import com.engineersbox.conduit_v2.processing.task.worker.QueuedForkJoinWorkerThreadFactory;
import com.engineersbox.conduit_v2.processing.task.worker.client.ClientPool;

import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.function.Consumer;

public class TaskExecutorPool {

    private final ClientBoundForkJoinPool executorService;

    public TaskExecutorPool(final ClientPool clientProvider,
                            final int parallelism) {
        this.executorService = newWorkStealingPool(
                parallelism,
                new QueuedForkJoinWorkerThreadFactory(clientProvider)
        );
    }

    private ClientBoundForkJoinPool newWorkStealingPool(final int parallelism,
                                                        final ForkJoinPool.ForkJoinWorkerThreadFactory workerThreadFactory) {
        return new ClientBoundForkJoinPool(
                parallelism > 0 ? parallelism : Runtime.getRuntime().availableProcessors(),
                workerThreadFactory,
                null,
                true
        );
    }

    public ForkJoinTask<?> submit(final ClientBoundWorkerTask task) {
        return this.executorService.submit(task);
    }

    public List<? extends ForkJoinTask<?>> invokeAll(final ClientBoundWorkerTask ...tasks) {
        return this.executorService.invokeAll(tasks);
    }

    public void invokeAll(final Consumer<? super ForkJoinTask<?>> appender,
                          final ClientBoundWorkerTask ...tasks) {
        this.executorService.invokeAll(appender, tasks);
    }

    public void waitAll(final ClientBoundWorkerTask ...tasks) {
        this.executorService.waitAll(tasks);
    }

}
