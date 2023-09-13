package com.engineersbox.conduit.processing.generation;

import com.engineersbox.conduit.schema.extension.handler.ContextTransformer;
import com.engineersbox.conduit.schema.metric.Metric;
import com.engineersbox.conduit.processing.task.worker.ClientBoundWorkerTask;
import com.engineersbox.conduit.retrieval.content.RetrievalHandler;
import io.riemann.riemann.Proto;
import org.eclipse.collections.api.RichIterable;
import org.eclipse.collections.api.map.ImmutableMap;

import java.util.function.Consumer;

@FunctionalInterface
public interface TaskBatchGenerator<T, E> {

    ClientBoundWorkerTask<T, E> generate(final RichIterable<Metric> metrics,
                                         final Proto.Event eventTemplate,
                                         final RetrievalHandler<Metric> retrieverReference,
                                         final ImmutableMap<String, Object> extensions,
                                         final Consumer<ContextTransformer.Builder> contextInjector);

}
