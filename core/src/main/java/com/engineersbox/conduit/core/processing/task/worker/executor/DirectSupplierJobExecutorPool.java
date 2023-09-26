package com.engineersbox.conduit.core.processing.task.worker.executor;

import javax.annotation.concurrent.ThreadSafe;
import java.util.function.Supplier;

@ThreadSafe
public class DirectSupplierJobExecutorPool<E> implements JobExecutorPool<E> {

    private final Supplier<E> jobExecutorSupplier;

    public DirectSupplierJobExecutorPool(final Supplier<E> jobExecutorSupplier) {
        this.jobExecutorSupplier = jobExecutorSupplier;
    }

    @Override
    public E acquire() {
        return this.jobExecutorSupplier.get();
    }

    @Override
    public void release(final E jobExecutor) {

    }
}
