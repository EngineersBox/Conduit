package com.engineersbox.conduit.core.retrieval.ingest.source;

import com.engineersbox.conduit.core.retrieval.ingest.IngestionContext;
import com.engineersbox.conduit.core.retrieval.ingest.connection.Connector;
import com.engineersbox.conduit.core.retrieval.ingest.connection.ConnectorConfiguration;
import com.engineersbox.conduit.core.schema.Schema;
import com.engineersbox.conduit.core.schema.metric.Metric;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SingleConfigurableSource extends Source<Object, Object> {

    private boolean configured = false;

    @Override
    public <E extends ConnectorConfiguration, C extends Connector<Object, E>> Object invoke(@Nonnull final C connector,
                                                                                            @Nonnull final Schema schema,
                                                                                            @Nullable final Metric metric,
                                                                                            @Nullable final IngestionContext ctx) throws Exception {
        if (!this.configured) {
            connector.configure();
            this.configured = true;
        }
        return connector.retrieve();
    }

}
