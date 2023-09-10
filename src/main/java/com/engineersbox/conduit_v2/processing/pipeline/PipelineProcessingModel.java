package com.engineersbox.conduit_v2.processing.pipeline;

import io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueue;
import io.netty.util.internal.shaded.org.jctools.queues.atomic.MpscAtomicArrayQueue;
import org.jeasy.batch.core.job.JobBuilder;
import org.jeasy.batch.core.job.JobExecutor;
import org.jeasy.batch.core.job.JobReport;
import org.jeasy.batch.core.processor.RecordProcessor;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;

import java.util.List;
import java.util.Spliterators;
import java.util.concurrent.Future;
import java.util.stream.StreamSupport;

public class PipelineProcessingModel implements ProcessingModel<List<Future<JobReport>>, JobExecutor> {

    private final DirectedAcyclicGraph<JobBuilder<?,?>, MessagePassingQueue> graph;

    public PipelineProcessingModel() {
        this(MpscAtomicArrayQueue.class);
    }

    public PipelineProcessingModel(final Class<? extends MessagePassingQueue> edgeClass) {
        this.graph = new DirectedAcyclicGraph<>(edgeClass);
    }

    public <I,O> JobBuilder<I, O> addVertex(final String name,
                                            final RecordProcessor<I, O> task) {
        final JobBuilder<I, O> builder = new JobBuilder<I, O>()
                .named(name)
                .processor(task);
        return this.graph.addVertex(builder) ? builder : null;
    }

    public <SO, DI> MessagePassingQueue<?> addEdge(final JobBuilder<?, SO> source,
                                                   final JobBuilder<DI, ?> destination) {
        return this.graph.addEdge(source, destination);
    }

    public <SO, DI, Q extends MessagePassingQueue> boolean addEdge(final JobBuilder<?, SO> source,
                                                                   final JobBuilder<DI, ?> destination,
                                                                   final Q queue) {
        return this.graph.addEdge(
                source,
                destination,
                queue
        );
    }

    @Override
    public List<Future<JobReport>> submitAll(final JobExecutor executor) {
        final TopologicalOrderIterator<JobBuilder<?,?>, MessagePassingQueue> iterator = new TopologicalOrderIterator<>(this.graph);
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, 0), false)
                .map(JobBuilder::build)
                .map(executor::submit)
                .toList();
    }

}
