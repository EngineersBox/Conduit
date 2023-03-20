package com.engineersbox.conduit.source.custom;

import com.engineersbox.conduit.pipeline.ingestion.IngestionContext;
import com.engineersbox.conduit.source.Source;
import com.engineersbox.conduit.source.SourceType;

import java.util.Map;

public class CustomSource extends Source {

    private final Map<String, Object> properties;

    public CustomSource(final Map<String, Object> properties) {
        super(SourceType.CUSTOM);
        this.properties = properties;
    }

    public Map<String, Object> getProperties() {
        return this.properties;
    }

    public Object getProperty(final String key) {
        return this.properties.get(key);
    }

    @Override
    public String invoke(final IngestionContext ctx) {
        return null; // TODO
    }

}
