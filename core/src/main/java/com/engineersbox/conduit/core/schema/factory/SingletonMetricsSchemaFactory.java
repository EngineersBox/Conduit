package com.engineersbox.conduit.core.schema.factory;

import com.engineersbox.conduit.core.schema.Schema;

import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
public final class SingletonMetricsSchemaFactory extends MetricsSchemaFactory {

    private final Schema schema;

    SingletonMetricsSchemaFactory(final Schema schema) {
        this.schema = schema;
    }

    @Override
    protected Schema provide() {
        return this.schema;
    }

    @Override
    public void refresh() {

    }

}
