package com.engineersbox.conduit_v2.processing.task.worker;

import io.riemann.riemann.client.IRiemannClient;
import io.riemann.riemann.client.RiemannClient;

import java.util.concurrent.ForkJoinWorkerThread;

public class ClientBoundForkJoinWorkerThead extends ForkJoinWorkerThread {

    private final IRiemannClient client;

    protected ClientBoundForkJoinWorkerThead(final ClientBoundForkJoinPool pool, final IRiemannClient client) {
        super(pool);
        this.client = client;
    }

    public IRiemannClient getClient() {
        return this.client;
    }

}
