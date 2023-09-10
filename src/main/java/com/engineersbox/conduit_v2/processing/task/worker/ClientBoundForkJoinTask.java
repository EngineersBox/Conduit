package com.engineersbox.conduit_v2.processing.task.worker;

import com.engineersbox.conduit_v2.processing.pipeline.ProcessingModel;
import com.engineersbox.conduit_v2.processing.task.worker.executor.JobExecutorPool;
import org.jeasy.batch.core.job.Job;
import org.jeasy.batch.core.job.JobExecutor;
import org.jeasy.batch.core.job.JobReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serial;
import java.util.List;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.Future;
import java.util.concurrent.RunnableFuture;

public class ClientBoundForkJoinTask<T, E> extends ForkJoinTask<T> implements RunnableFuture<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientBoundForkJoinTask.class);

    @Serial
    private static final long serialVersionUID = 2988328017776527845L;

    @SuppressWarnings("serial") // Conditionally serializable
    private final ClientBoundWorkerTask<T, E> runnable;
    private final JobExecutorPool<E> jobExecutorPool;
    private T results;

    public ClientBoundForkJoinTask(final ClientBoundWorkerTask<T, E> runnable,
                                   final JobExecutorPool<E> jobExecutorPool) {
        if (runnable == null) throw new NullPointerException();
        this.runnable = runnable;
        this.jobExecutorPool = jobExecutorPool;
        this.results = null;
    }

    @Override
    public T getRawResult() {
        return this.results;
    }

    @Override
    protected void setRawResult(final T newResult) {
        this.results = newResult;
    }

    @Override
    public final boolean exec() {
        final Thread thread;
        if ((thread = Thread.currentThread()) instanceof ClientBoundForkJoinWorkerThead workerThread) {
            final ProcessingModel<T, E> model = runnable.apply(workerThread.getClient());
            try (final JobExecutorPool.ClosableJobExecutor<E> jobExecutor = JobExecutorPool.acquireClosable(this.jobExecutorPool)) {
                setRawResult(model.submitAll(jobExecutor.getJobExecutor()));
            } catch (final Exception e) {
                LOGGER.error("Exception encountered during job submission", e);
                return false;
            }
            return true;
        }
        throw new IllegalThreadStateException("Cannot invoke client bound fork-join task on non-ClientBoundForkJoinWorkerThread instances, got instead: " + thread.getClass().getName());
    }

    @Override
    public final void run() {
        super.invoke();
    }

    @Override
    public String toString() {
        return super.toString() + "[Wrapped task = " + this.runnable + "]";
    }

}
