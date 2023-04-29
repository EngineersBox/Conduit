package com.engineersbox.conduit_v2.processing.task.worker;

import io.riemann.riemann.client.RiemannClient;

import java.util.concurrent.ForkJoinWorkerThread;

public class ClientBoundForkJoinWorkerThead extends ForkJoinWorkerThread {

    private final RiemannClient client;

    protected ClientBoundForkJoinWorkerThead(final ClientBoundForkJoinPool pool, final RiemannClient client) {
        super(pool);
        this.client = client;
    }

    public RiemannClient getClient() {
        return this.client;
    }

}
