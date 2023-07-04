package com.engineersbox.conduit_v2.processing.generation;

import com.engineersbox.conduit.handler.ContextTransformer;
import com.engineersbox.conduit.handler.LuaContextHandler;
import com.engineersbox.conduit.schema.metric.Metric;
import com.engineersbox.conduit_v2.processing.task.MetricProcessingTask;
import com.engineersbox.conduit_v2.retrieval.content.RetrievalHandler;
import io.riemann.riemann.Proto;
import org.eclipse.collections.api.RichIterable;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public abstract class TaskBatchGeneratorFactory {

    private TaskBatchGeneratorFactory() {
        throw new IllegalArgumentException("Factory class");
    }

    public static TaskBatchGenerator defaultGenerator() {
        return (final RichIterable<Metric> metrics,
                final Proto.Event eventTemplate,
                final AtomicReference<RetrievalHandler<Metric>> retrieverReference,
                final LuaContextHandler handler,
                final Consumer<ContextTransformer.Builder> contextInjector) -> new MetricProcessingTask(
                metrics.asLazy(),
                eventTemplate,
                retrieverReference,
                handler,
                contextInjector
        );
    }

}
