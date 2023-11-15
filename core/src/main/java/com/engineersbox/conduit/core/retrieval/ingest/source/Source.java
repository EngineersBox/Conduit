package com.engineersbox.conduit.core.retrieval.ingest.source;

import com.engineersbox.conduit.core.retrieval.ingest.IngestionContext;
import com.engineersbox.conduit.core.retrieval.ingest.connection.Connector;
import com.engineersbox.conduit.core.retrieval.ingest.connection.ConnectorConfiguration;
import com.engineersbox.conduit.core.schema.metric.Metric;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class Source<T, R> {

    public String name() {
        return this.getClass().getName();
    }

    abstract public <E extends ConnectorConfiguration, C extends Connector<T, E>> R invoke(@Nonnull final C connector,
                                                                                           @Nullable final Metric currentMetric,
                                                                                           @Nullable final IngestionContext ctx) throws Exception;

    /**
     * Value to return when source invocation times out.
     * Non-overridden default implementation returns null
     * from this method.
     * @return Default value (null when not overridden)
     */
    public R defaultDataValue() {
        return null;
    }

    public static Source<Object, Object> singleConfigurable() {
        return new SingleConfigurableSource();
    }

}
