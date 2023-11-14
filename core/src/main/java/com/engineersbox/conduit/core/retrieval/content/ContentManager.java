package com.engineersbox.conduit.core.retrieval.content;

import com.engineersbox.conduit.core.processing.PollingCondition;
import com.engineersbox.conduit.core.schema.metric.Metric;
import com.engineersbox.conduit.core.retrieval.ingest.Ingester;
import com.engineersbox.conduit.core.retrieval.ingest.IngestionContext;
import com.engineersbox.conduit.core.retrieval.ingest.connection.Connector;
import com.engineersbox.conduit.core.retrieval.ingest.connection.ConnectorConfiguration;
import com.engineersbox.conduit.core.retrieval.path.PathTraversalHandler;
import com.jayway.jsonpath.TypeRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class ContentManager<T, E extends ConnectorConfiguration, C extends Connector<T, E>> implements RetrievalHandler<Metric> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContentManager.class);

    private final Ingester<T, E, C> ingester;
    private final IngestionContext context;
    private final PathTraversalHandler<T> pathTraversalHandler;
    private final PollingCondition pollingCondition;

    public ContentManager(final Ingester<T, E, C> ingester,
                          final IngestionContext context,
                          final PathTraversalHandler<T> pathTraversalHandler,
                          final PollingCondition pollingCondition) {
        this.ingester = ingester;
        this.context = context;
        this.pathTraversalHandler = pathTraversalHandler;
        this.pollingCondition = pollingCondition;
    }

    public void setCacheKey(final Optional<String> key) {
        this.ingester.setCacheKey(key);
    }

    public void poll() throws Exception {
        poll(null);
    }

    private void poll(final Metric metric) throws Exception {
        /* TODO: Refactor to support polling once to use result for
         *       all metrics lookup with null/no parameters or to
         *       poll for every lookup with the current Metric
         *       instance passed as a parameter. This means we can
         *       perform connector related stuff that is specific
         *       to each metric (JMX call to specific MBean for
         *       example) on each call. This gives the user total
         *       control over how they use a connection to resolve
         *       metrics before it gets passed to a JsonPath eval
         *       with the configuration set in the schema. Thus, the
         *       json_provider and mapping_provider are always
         *       applied to the result of the Source<T> which can,
         *       for example, be a call to a JMX server for a
         *       specific MBean value. This also means that there
         *       is no restriction on what a Connection must
         *       return that is passed to a Source<T>, thus meaning
         *       passing a TCP socket or something like that is
         *       also possible, not just some JSON data retrieved
         *       from the remote server within the Connection
         *       instance.
         */
        LOGGER.trace("Polling content manager");
        this.ingester.clear();
        this.ingester.consumeSource(this.context, metric);
        this.pathTraversalHandler.saturate(this.ingester.getCurrent());
        LOGGER.trace("Saturated path traversal handler with currently ingested context");
    }

    @Override
    public Object lookup(final Metric metric) throws Exception {
        if (this.pollingCondition == PollingCondition.PER_METRIC) {
            poll(metric);
        }
        final String path = metric.getPath();
        final TypeRef<?> type = metric.getStructure().intoConcrete();
        LOGGER.trace("Performing look up for metric path {} into type {}", path, type.getType());
        return this.pathTraversalHandler.read(path, type);
    }



}
