package com.engineersbox.conduit_v2.processing.task.worker;

import io.riemann.riemann.client.IRiemannClient;

import java.util.function.Consumer;

@FunctionalInterface
public interface ClientBoundWorkerTask extends Consumer<IRiemannClient> {
    @Override
    void accept(final IRiemannClient riemannClient);

}
