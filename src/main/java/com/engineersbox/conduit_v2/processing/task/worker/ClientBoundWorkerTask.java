package com.engineersbox.conduit_v2.processing.task.worker;

import io.riemann.riemann.client.RiemannClient;

import java.util.function.Consumer;

@FunctionalInterface
public interface ClientBoundWorkerTask extends Consumer<RiemannClient> {
    @Override
    void accept(final RiemannClient riemannClient);

}
