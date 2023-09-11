package com.engineersbox.conduit_v2.processing.pipeline;

import org.jctools.queues.MessagePassingQueue;
import org.jctools.queues.atomic.MpscAtomicArrayQueue;
import org.jeasy.batch.core.job.JobBuilder;
import org.jeasy.batch.core.job.JobExecutor;
import org.jeasy.batch.core.job.JobReport;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;
import java.util.Spliterators;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.StreamSupport;

public class PipelineProcessingModel implements ProcessingModel<List<JobReport>, JobExecutor> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PipelineProcessingModel.class);

    private final DirectedAcyclicGraph<JobBuilder<?,?>, MessagePassingQueue> graph;

    public PipelineProcessingModel() {
        this(MpscAtomicArrayQueue.class);
    }

    public PipelineProcessingModel(final Class<? extends MessagePassingQueue> edgeClass) {
        this.graph = new DirectedAcyclicGraph<>(edgeClass);
    }

    public <I,O> JobBuilder<I, O> addJob(@Nonnull final String name) {
        if (name == null) {
            throw new NullPointerException("Job must have non-null name");
        }
        final JobBuilder<I, O> builder = new JobBuilder<I, O>().named(name);
        return this.graph.addVertex(builder) ? builder : null;
    }

    public <T, E, Q extends MessagePassingQueue<E>> boolean connectJobs(@Nonnull final JobBuilder<?, T> source,
                                                                        @Nonnull final JobBuilder<T, ?> destination,
                                                                        @Nonnull final Q queue) {
        if (source == null) {
            throw new NullPointerException("Cannot connect null source job");
        } else if (destination == null) {
            throw new NullPointerException("Cannot connect null destination job");
        } else if (queue == null) {
            throw new NullPointerException("Cannot connect job via null queue");
        }
        return this.graph.addEdge(
                source,
                destination,
                queue
        );
    }

    @Override
    public List<JobReport> submitAll(final JobExecutor executor) {
        final TopologicalOrderIterator<JobBuilder<?,?>, MessagePassingQueue> iterator = new TopologicalOrderIterator<>(this.graph);
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, 0), false)
                .map(JobBuilder::build)
                .map(executor::submit)
                .map((final Future<JobReport> future) -> {
                    try {
                        return future.get();
                    } catch (final InterruptedException | ExecutionException e) {
                        LOGGER.error("Failed while waiting for job to complete", e);
                        return null;
                    }
                }).filter(Objects::nonNull)
                .toList();
    }

}
