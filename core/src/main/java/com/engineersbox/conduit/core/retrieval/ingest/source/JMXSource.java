package com.engineersbox.conduit.core.retrieval.ingest.source;

import com.engineersbox.conduit.core.retrieval.ingest.IngestionContext;
import com.engineersbox.conduit.core.retrieval.ingest.connection.Connector;
import com.engineersbox.conduit.core.retrieval.ingest.connection.ConnectorConfiguration;
import com.engineersbox.conduit.core.schema.metric.Metric;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.internal.EvaluationContext;
import com.jayway.jsonpath.internal.Path;
import com.jayway.jsonpath.internal.path.PathCompiler;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.management.MBeanServerConnection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class JMXSource extends Source<MBeanServerConnection, String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(JMXSource.class);
    private static final ConcurrentMap<String, Path> PATHS = new ConcurrentHashMap<>();

    private final boolean cachePaths;
    private boolean configured;

    public JMXSource(final boolean cachePaths) {
        this.cachePaths = cachePaths;
        this.configured = false;
    }

    public JMXSource() {
        this(true);
    }

    @Override
    public <E extends ConnectorConfiguration, C extends Connector<MBeanServerConnection, E>> String invoke(@Nonnull final C connector,
                                                                                                           @Nullable final Metric currentMetric,
                                                                                                           @Nullable final IngestionContext ctx) throws Exception {
        if (!this.configured) {
            connector.configure();
            this.configured = true;
        }
        if (currentMetric == null) { // TODO: Fix metric null when PollingCondition is not ON_METRIC
            throw new IllegalArgumentException("Cannot query JMX server with null Metric");
        }
        final MBeanServerConnection connection = connector.retrieve();
        final Path jsonPath;
        if (this.cachePaths) {
            jsonPath = PATHS.computeIfAbsent(
                    currentMetric.getPath(),
                    PathCompiler::compile
            );
        } else {
            jsonPath = PathCompiler.compile(currentMetric.getPath());
        }
        final EvaluationContext evalCtx = jsonPath.evaluate(
                connection,
                connection,
                Configuration.defaultConfiguration(),
                false
        );
        final Object value = evalCtx.getValue();
        if (!(value instanceof Pair<?,?> pair)) {
            throw new IllegalStateException("Expected Pair<String,Object> from JMX MBean call");
        }
        final String methodName = (String) pair.getLeft();
        final String previousPath = currentMetric.getPath();
        currentMetric.setPath("$.['" + methodName + "']");
        LOGGER.trace(
                "Updated metric path from {} to {}",
                previousPath,
                currentMetric.getPath()
        );
        final Object result = pair.getRight();
        /* TODO: Refactor to create a queryable data source based on the json_provider
         *       and/or mapping_provider set in the schema. This can be arbitrary format
         *       if there are user defined providers used.
         */
        final String jsonDoc = String.format(
                "{\"%s\":%s}",
                methodName,
                result instanceof String stringResult
                        ? stringResult
                        : String.valueOf(result)
        );
        LOGGER.trace("Constructed JSON doc for method target and value: {}", jsonDoc);
        return jsonDoc;
    }

}
