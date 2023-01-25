package com.engineersbox.conduit.schema;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class MetricsSchema {

    private final Map<String, PathBinding> paths;

    private MetricsSchema() {
        this.paths = new HashMap<>();
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
            this.schema.paths.put(
                    binding.getPath(),
                    binding
            );
            return this;
        }

        public Builder put(final String path,
                           final String name,
                           final Class<?> dataType) {
            return put(PathBinding.path(path)
                    .name(name)
                    .type(dataType)
                    .complete()
            );
        }

        public Builder put(final String path,
                           final String name,
                           final Class<?> dataType,
                           final Function<Map<String, Object>, Boolean> inclusionHandler) {
            return put(PathBinding.path(path)
                    .name(name)
                    .type(dataType)
                    .handler(inclusionHandler)
                    .complete()
            );
        }

        public MetricsSchema build() {
            return this.schema;
        }

    }

}
