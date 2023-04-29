package com.engineersbox.conduit_v2.processing.task;

import com.engineersbox.conduit_v2.processing.task.worker.ClientBoundForkJoinPool;
import com.engineersbox.conduit_v2.processing.task.worker.QueuedForkJoinWorkerThreadFactory;
import io.riemann.riemann.client.RiemannClient;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.function.Supplier;

public class TaskExecutorPool {

    // TODO: Support configurable pool sizes
    private final ClientBoundForkJoinPool executorService;

    public TaskExecutorPool(final Supplier<RiemannClient> clientProvider) {
        this.executorService = newWorkStealingPool(
                3, // TODO: Support configurable pool sizes
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

    public ForkJoinTask<?> submit(final MetricProcessingTask task) {
        return this.executorService.submit(task);
    }

}
