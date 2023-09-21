package com.engineersbox.conduit.core.processing.pipeline;

import org.jctools.queues.MessagePassingQueue;
import org.jctools.queues.atomic.MpscAtomicArrayQueue;
import org.jeasy.batch.core.job.JobBuilder;
import org.jeasy.batch.core.job.JobExecutor;
import org.jeasy.batch.core.job.JobReport;
import org.jeasy.batch.core.reader.RecordReader;
import org.jeasy.batch.core.writer.RecordWriter;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Spliterators;
import java.util.concurrent.Future;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class PipelineProcessingModel implements ProcessingModel<List<Future<JobReport>>, JobExecutor> {

    // NOTE: Usage guide for EasyBatch: https://github.com/j-easy/easy-batch/tree/master/easy-batch-tutorials/src/main/java/org/jeasy/batch/tutorials/advanced/parallel

    private static final Logger LOGGER = LoggerFactory.getLogger(PipelineProcessingModel.class);

    private final DirectedAcyclicGraph<JobBuilder<?,?>, MessagePassingQueue> graph;
    private final boolean blockOnFutures;

    public PipelineProcessingModel(final boolean blockOnFutures) {
        this(MpscAtomicArrayQueue.class, blockOnFutures);
    }

    public PipelineProcessingModel(final Class<? extends MessagePassingQueue> edgeClass,
                                   final boolean blockOnFutures) {
        this.graph = new DirectedAcyclicGraph<>(edgeClass);
        this.blockOnFutures = blockOnFutures;
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

    public <T, E, Q extends MessagePassingQueue<E>> boolean connectJobs(@Nonnull final JobBuilder<?, T> source,
                                                                        final RecordWriter<T> sourceQueueWriter,
                                                                        @Nonnull final JobBuilder<T, ?> destination,
                                                                        final RecordReader<T> destinationQueueReader,
                                                                        @Nonnull final Q queue) {
        if (!connectJobs(source, destination, queue)) {
            return false;
        }
        if (sourceQueueWriter == null) {
            LOGGER.trace("Source queue writer was null, skipping writer binding to job {}", source);
        } else {
            source.writer(sourceQueueWriter);
        }
        if (destinationQueueReader == null) {
            LOGGER.trace("Destination queue reader was null, skipping reader binding to job {}", destination);
        } else {
            destination.reader(destinationQueueReader);
        }
        return true;
    }

    @Override
    public List<Future<JobReport>> submitAll(final JobExecutor executor) {
        final TopologicalOrderIterator<JobBuilder<?,?>, MessagePassingQueue> iterator = new TopologicalOrderIterator<>(this.graph);
        Stream<Future<JobReport>> stream = StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, 0), false)
                .map(JobBuilder::build)
                .map(executor::submit);
        if (this.blockOnFutures) {
            stream = stream.peek((final Future<JobReport> future) -> {
                while (!future.isDone() && !future.isCancelled()) ;
            });
        }
        return stream.toList();
    }

}