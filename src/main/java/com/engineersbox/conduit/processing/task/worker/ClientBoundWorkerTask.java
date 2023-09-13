package com.engineersbox.conduit.processing.task.worker;

import com.engineersbox.conduit.processing.pipeline.ProcessingModel;
import io.riemann.riemann.client.IRiemannClient;

import java.util.function.Function;

@FunctionalInterface
public interface ClientBoundWorkerTask<T, E> extends Function<IRiemannClient, ProcessingModel<T, E>> {
    @Override
    ProcessingModel<T, E> apply(final IRiemannClient riemannClient);

}