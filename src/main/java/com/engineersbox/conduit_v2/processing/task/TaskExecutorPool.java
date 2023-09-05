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

public class TaskExecutorPool {

    private final ClientBoundForkJoinPool executorService;

    public TaskExecutorPool(final ClientPool clientProvider,
                            final JobExecutorPool jobExecutorPool,
                            final int parallelism) {
        this.executorService = newWorkStealingPool(
                parallelism,
                jobExecutorPool,
                new QueuedForkJoinWorkerThreadFactory(clientProvider)
        );
    }

    private ClientBoundForkJoinPool newWorkStealingPool(final int parallelism,
                                                        final JobExecutorPool jobExecutorPool,
                                                        final ForkJoinPool.ForkJoinWorkerThreadFactory workerThreadFactory) {
        return new ClientBoundForkJoinPool(
                parallelism > 0 ? parallelism : Runtime.getRuntime().availableProcessors(),
                workerThreadFactory,
                jobExecutorPool,
                null,
                true
        );
    }

    public ForkJoinTask<List<Future<JobReport>>> submit(final ClientBoundWorkerTask task) {
        return this.executorService.submit(task);
    }

    public List<? extends ForkJoinTask<List<Future<JobReport>>>> invokeAll(final ClientBoundWorkerTask ...tasks) {
        return this.executorService.invokeAll(tasks);
    }

    public void invokeAll(final Consumer<? super ForkJoinTask<List<Future<JobReport>>>> appender,
                          final ClientBoundWorkerTask ...tasks) {
        this.executorService.invokeAll(appender, tasks);
    }

    public void waitAll(final ClientBoundWorkerTask ...tasks) {
        this.executorService.waitAll(tasks);
    }

}
