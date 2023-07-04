package com.engineersbox.conduit_v2.retrieval.ingest;

import com.engineersbox.conduit_v2.retrieval.ingest.connection.Connector;
import com.engineersbox.conduit_v2.retrieval.ingest.connection.ConnectorConfiguration;

public abstract class Source<T> {

    abstract public <E extends ConnectorConfiguration, C extends Connector<T, E>> T invoke(final C connector, final IngestionContext ctx) throws Exception;

    public static Source<Object> singleConfigurable() {
        return new SingleConfigurable();
    }

    public static class SingleConfigurable extends Source<Object> {

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

}
