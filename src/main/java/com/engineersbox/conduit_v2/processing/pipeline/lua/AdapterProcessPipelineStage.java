package com.engineersbox.conduit_v2.processing.pipeline.lua;

import com.engineersbox.conduit.handler.ContextTransformer;
import com.engineersbox.conduit.handler.LuaContextHandler;
import com.engineersbox.conduit_v2.processing.event.EventSerialiser;
import com.engineersbox.conduit_v2.processing.event.EventsDeserialiser;
import com.engineersbox.conduit_v2.processing.pipeline.core.ProcessPipelineStage;
import com.engineersbox.conduit_v2.processing.task.MetricProcessingTask;
import io.riemann.riemann.Proto;

public class AdapterProcessPipelineStage extends ProcessPipelineStage<Proto.Event[], Proto.Event[]> {

    private final ContextTransformer.Builder contextBuilder;
    private final LuaContextHandler contextHandler;
    private final ContextTransformer transformer;
    private final Proto.Event eventTemplate;

    public AdapterProcessPipelineStage(final ContextTransformer.Builder contextBuilder,
                                       final LuaContextHandler contextHandler,
                                       final ContextTransformer transformer,
                                       final Proto.Event eventTemplate) {
        super("Adapter Lua handler");
        this.contextBuilder = contextBuilder;
        this.contextHandler = contextHandler;
        this.transformer = transformer;
        this.eventTemplate = eventTemplate;
    }

    @Override
    public Proto.Event[] apply(final Proto.Event[] events) {
        final Object handlerObj = getContextAttribute(HandlerSaturationPipelineStage.LUA_HANDLER_PREFIX + "adapter");
        if (!(handlerObj instanceof String handler)) {
            return events;
        }
        this.contextBuilder.withReadOnly(
                "events",
                events,
                EventSerialiser.class
        );
        this.contextHandler.invoke(
                handler,
                this.transformer.transform()
        );
        return this.contextHandler.getFromResult(
                new String[]{
                        "events"
                },
                new EventsDeserialiser(this.eventTemplate)
        );
    }

}
