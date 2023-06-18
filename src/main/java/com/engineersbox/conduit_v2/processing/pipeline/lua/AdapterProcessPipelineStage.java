package com.engineersbox.conduit_v2.processing.pipeline.lua;

import com.engineersbox.conduit.handler.ContextTransformer;
import com.engineersbox.conduit.handler.LuaContextHandler;
import com.engineersbox.conduit_v2.processing.event.EventSerialiser;
import com.engineersbox.conduit_v2.processing.event.EventsDeserialiser;
import com.engineersbox.conduit_v2.processing.pipeline.PipelineStage;
import com.engineersbox.conduit_v2.processing.pipeline.StageResult;
import com.engineersbox.conduit_v2.processing.pipeline.core.ProcessPipelineStage;
import com.engineersbox.conduit_v2.processing.task.MetricProcessingTask;
import io.riemann.riemann.Proto;
import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.collector.Collectors2;
import org.eclipse.collections.impl.utility.Iterate;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class AdapterProcessPipelineStage extends PipelineStage<Proto.Event[][], Proto.Event[]> {

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
    public StageResult<Proto.Event[]> invoke(final Proto.Event[][] events) {
        final Object handlerObj = getContextAttribute(HandlerSaturationPipelineStage.LUA_HANDLER_PREFIX + "adapter");
        final Proto.Event[] eventsList = Arrays.stream(events)
                .map(Arrays::stream)
                .reduce(Stream::concat)
                .get()
                .toArray(Proto.Event[]::new);
        if (!(handlerObj instanceof String handler)) {
            return new StageResult<>(
                    StageResult.Type.SINGLE,
                    eventsList,
                    false
            );
        }
        this.contextBuilder.withReadOnly(
                "events",
                eventsList,
                EventSerialiser.class
        );
        this.contextHandler.invoke(
                handler,
                this.transformer.transform()
        );
        final Proto.Event[] finalEvents = this.contextHandler.getFromResult(
                new String[]{
                        "events"
                },
                new EventsDeserialiser(this.eventTemplate)
        );
        return new StageResult<>(
                StageResult.Type.SINGLE,
                finalEvents,
                false
        );
    }

}
