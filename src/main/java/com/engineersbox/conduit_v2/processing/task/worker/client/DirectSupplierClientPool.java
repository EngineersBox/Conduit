package com.engineersbox.conduit_v2.processing.task.worker.client;

import io.riemann.riemann.client.IRiemannClient;

import java.util.function.Supplier;

public class DirectSupplierClientPool implements ClientPool {

    private final Supplier<IRiemannClient> clientProvider;

    public DirectSupplierClientPool(final Supplier<IRiemannClient> clientProvider) {
        this.clientProvider = clientProvider;
    }

    @Override
    public IRiemannClient acquire() {
        return this.clientProvider.get();
    }

    @Override
    public void release(final IRiemannClient client) {
        // Nothing to release
    }
}
