package com.engineersbox.conduit_v2.processing.schema;

import com.engineersbox.conduit.schema.metric.Metric;
import com.engineersbox.conduit_v2.retrieval.ingest.connection.Connector;
import com.jayway.jsonpath.Configuration;
import io.riemann.riemann.Proto;
import org.eclipse.collections.api.RichIterable;

/**
 * Metrics schema definition
 */
public class Schema {

    private Connector<?,?> connector;
    private Configuration jsonPathConfiguration;
    private Proto.Event eventTemplate;
    private String handler;
    private RichIterable<Metric> metrics;

}
