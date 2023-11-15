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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.management.MBeanServerConnection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class JMXSource extends Source<MBeanServerConnection, String> {

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
        final Path path;
        if (this.cachePaths) {
            path = PATHS.computeIfAbsent(
                    currentMetric.getPath(),
                    PathCompiler::compile
            );
        } else {
            path = PathCompiler.compile(currentMetric.getPath());
        }
        // TODO: Figure out what the params to this arg is from GitHub for JsonPath
        //       and verify if we can supply the connection for the jmxMBean function
        final EvaluationContext evalCtx = path.evaluate(
                connection,
                connection,
                Configuration.defaultConfiguration(),
                false
        );
        final Object value = evalCtx.getValue();
        return value instanceof String stringValue ? stringValue : (String) value;
    }

}
