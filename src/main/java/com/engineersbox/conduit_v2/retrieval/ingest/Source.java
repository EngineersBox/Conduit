package com.engineersbox.conduit_v2.retrieval.ingest;

import com.engineersbox.conduit_v2.retrieval.ingest.connection.Connector;
import com.engineersbox.conduit_v2.retrieval.ingest.connection.ConnectorConfiguration;

public abstract class Source<T> {

    abstract public <E extends ConnectorConfiguration, C extends Connector<T, E>> T invoke(final C connector, final IngestionContext ctx) throws Exception;

}
