package com.engineersbox.conduit_v2.processing.task.worker;

import com.engineersbox.conduit_v2.processing.pipeline.ProcessingModel;
import io.riemann.riemann.client.IRiemannClient;
import org.jeasy.batch.core.job.Job;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

@FunctionalInterface
public interface ClientBoundWorkerTask<T, E> extends Function<IRiemannClient, ProcessingModel<T, E>> {
    @Override
    ProcessingModel<T, E> apply(final IRiemannClient riemannClient);

}
