package com.engineersbox.conduit_v2.processing.task.worker.executor;

import org.jeasy.batch.core.job.JobExecutor;

import java.util.function.Supplier;

public class DirectSupplierJobExecutorPool implements JobExecutorPool {

    private final Supplier<JobExecutor> jobExecutorSupplier;

    public DirectSupplierJobExecutorPool(final Supplier<JobExecutor> jobExecutorSupplier) {
        this.jobExecutorSupplier = jobExecutorSupplier;
    }

    @Override
    public JobExecutor acquire() {
        return this.jobExecutorSupplier.get();
    }

    @Override
    public void release(JobExecutor jobExecutor) {

    }
}
