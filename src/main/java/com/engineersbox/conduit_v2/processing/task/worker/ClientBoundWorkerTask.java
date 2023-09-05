package com.engineersbox.conduit_v2.processing.task.worker;

import io.riemann.riemann.client.IRiemannClient;
import org.jeasy.batch.core.job.Job;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

@FunctionalInterface
public interface ClientBoundWorkerTask extends Function<IRiemannClient, List<Job>> {
    @Override
    List<Job> apply(final IRiemannClient riemannClient);

}
