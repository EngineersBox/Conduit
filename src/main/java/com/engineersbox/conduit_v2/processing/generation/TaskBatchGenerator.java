package com.engineersbox.conduit_v2.processing.generation;

import com.engineersbox.conduit.handler.ContextTransformer;
import com.engineersbox.conduit.handler.LuaContextHandler;
import com.engineersbox.conduit_v2.processing.schema.metric.Metric;
import com.engineersbox.conduit_v2.processing.task.worker.ClientBoundWorkerTask;
import com.engineersbox.conduit_v2.retrieval.content.RetrievalHandler;
import io.riemann.riemann.Proto;
import org.eclipse.collections.api.RichIterable;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

@FunctionalInterface
public interface TaskBatchGenerator {

    ClientBoundWorkerTask generate(final RichIterable<Metric> metrics,
                                   final Proto.Event eventTemplate,
                                   final RetrievalHandler<Metric> retrieverReference,
                                   final LuaContextHandler handler,
                                   final Consumer<ContextTransformer.Builder> contextInjector);

}
