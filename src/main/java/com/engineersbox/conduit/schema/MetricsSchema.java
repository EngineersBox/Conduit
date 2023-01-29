package com.engineersbox.conduit.schema;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.TypeRef;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class MetricsSchema extends HashMap<String, PathBinding> {

    private Configuration jsonPathConfiguration;

    private MetricsSchema() {
    }

    public Configuration getJsonPathConfiguration() {
        return this.jsonPathConfiguration;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static MetricsSchema from(final PathBinding ...bindings) {
        final Builder builder = MetricsSchema.builder();
        Arrays.stream(bindings).forEach(builder::put);
        return builder.build();
    }

    public static class Builder {

        private final MetricsSchema schema;

        private Builder() {
            this.schema = new MetricsSchema();
        }

        public Builder put(final PathBinding binding) {
            binding.validate();
            this.schema.put(
                    binding.getPath(),
                    binding
            );
            return this;
        }

        public Builder put(final String path,
                           final String name,
                           final TypeRef<?> dataType) {
            return put(PathBinding.path(path)
                    .name(name)
                    .type(dataType)
                    .complete()
            );
        }

        public Builder put(final String path,
                           final String name,
                           final TypeRef<?> dataType,
                           final Function<Map<String, Object>, Boolean> inclusionHandler) {
            return put(PathBinding.path(path)
                    .name(name)
                    .type(dataType)
                    .handler(inclusionHandler)
                    .complete()
            );
        }

        public Builder withJsonPathConfig(final Configuration jsonPathConfig) {
            this.schema.jsonPathConfiguration = jsonPathConfig;
            return this;
        }

        public MetricsSchema build() {
            if (this.schema.jsonPathConfiguration == null) {
                this.schema.jsonPathConfiguration = Configuration.defaultConfiguration();
            }
            return this.schema;
        }

    }

}
