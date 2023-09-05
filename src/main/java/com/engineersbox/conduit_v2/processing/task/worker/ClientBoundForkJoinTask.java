package com.engineersbox.conduit_v2.processing.task.worker;

import com.engineersbox.conduit_v2.processing.task.worker.executor.JobExecutorPool;
import org.jeasy.batch.core.job.Job;
import org.jeasy.batch.core.job.JobExecutor;
import org.jeasy.batch.core.job.JobReport;

import java.io.Serial;
import java.util.List;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.Future;
import java.util.concurrent.RunnableFuture;

public class ClientBoundForkJoinTask extends ForkJoinTask<List<Future<JobReport>>> implements RunnableFuture<List<Future<JobReport>>> {

    @Serial
    private static final long serialVersionUID = 2988328017776527845L;

    @SuppressWarnings("serial") // Conditionally serializable
    private final ClientBoundWorkerTask runnable;
    private final JobExecutorPool jobExecutorPool;
    private List<Future<JobReport>> results;

    public ClientBoundForkJoinTask(final ClientBoundWorkerTask runnable,
                                   final JobExecutorPool jobExecutorPool) {
        if (runnable == null) throw new NullPointerException();
        this.runnable = runnable;
        this.jobExecutorPool = jobExecutorPool;
        this.results = null;
    }

    @Override
    public List<Future<JobReport>> getRawResult() {
        return this.results;
    }

    @Override
    protected void setRawResult(final List<Future<JobReport>> newResult) {
        this.results = newResult;
    }

    @Override
    public final boolean exec() {
        final Thread thread;
        if ((thread = Thread.currentThread()) instanceof ClientBoundForkJoinWorkerThead workerThread) {
            final List<Job> jobs = runnable.apply(workerThread.getClient());
            try (final JobExecutorPool.ClosableJobExecutor jobExecutor = JobExecutorPool.acquireClosable(this.jobExecutorPool)) {
                setRawResult(jobExecutor.getJobExecutor().submitAll(jobs));
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
