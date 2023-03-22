package com.engineersbox.conduit.source.custom;

import com.engineersbox.conduit.pipeline.ingestion.IngestSource;
import com.engineersbox.conduit.pipeline.ingestion.IngestionContext;
import com.engineersbox.conduit.source.Source;
import com.engineersbox.conduit.source.SourceType;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CustomSource extends Source {

    private final Map<String, Object> properties;
    private IngestSource source;

    public CustomSource(final Map<String, Object> properties) {
        super(SourceType.CUSTOM);
        this.properties = properties;
        this.source = null;
    }

    public Map<String, Object> getProperties() {
        return this.properties;
    }

    public Object getProperty(final String key) {
        return this.properties.get(key);
    }

    public void setSource(final IngestSource source) {
        this.source = source;
    }

    @Override
    public String invoke(final IngestionContext ctx) {
        if (this.source == null) {
            throw new IllegalStateException("Expected a custom source, got none");
        }
        final Map<String, Object> newProps = new HashMap<>();
        newProps.putAll(ctx.getAttributes());
        newProps.putAll(this.properties);
        return this.source.apply(new IngestionContext() {
            {
                this.getAttributes().putAll(newProps);
            }

            @Override
            public void close() throws IOException {

            }
        });
    }

}
