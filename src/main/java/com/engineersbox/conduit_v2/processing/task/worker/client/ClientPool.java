package com.engineersbox.conduit_v2.processing.task.worker.client;

import io.riemann.riemann.client.IRiemannClient;

public interface ClientPool {

    IRiemannClient acquire();

    void release(final IRiemannClient client);

}
