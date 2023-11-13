package com.engineersbox.conduit.core.retrieval.ingest.source;

import com.engineersbox.conduit.core.retrieval.ingest.IngestionContext;
import com.engineersbox.conduit.core.retrieval.ingest.connection.Connector;
import com.engineersbox.conduit.core.retrieval.ingest.connection.ConnectorConfiguration;

public abstract class Source<T> {

    public String name() {
        return this.getClass().getName();
    }

    abstract public <E extends ConnectorConfiguration, C extends Connector<T, E>> T invoke(final C connector, final IngestionContext ctx) throws Exception;

    /**
     * Value to return when source invocation times out.
     * Non-overridden default implementation returns null
     * from this method.
     * @return Default value (null when not overridden)
     */
    public T defaultDataValue() {
        return null;
    }

    public static Source<Object> singleConfigurable() {
        return new SingleConfigurableSource();
    }

}
