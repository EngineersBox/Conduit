package com.engineersbox.conduit.core.retrieval.ingest.source;

import com.engineersbox.conduit.core.retrieval.ingest.IngestionContext;
import com.engineersbox.conduit.core.retrieval.ingest.connection.Connector;
import com.engineersbox.conduit.core.retrieval.ingest.connection.ConnectorConfiguration;

public class SingleConfigurableSource extends Source<Object> {

    private boolean configured = false;

    @Override
    public <E extends ConnectorConfiguration, C extends Connector<Object, E>> Object invoke(final C connector,
                                                                                            final IngestionContext ctx) throws Exception {
        if (!this.configured) {
            connector.configure();
            this.configured = true;
        }
        return connector.retrieve();
    }

}
