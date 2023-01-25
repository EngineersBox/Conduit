package com.engineersbox.conduit.schema;

import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.function.Function;

public class PathBinding {

    private String path;
    private String metricName;
    private Class<?> dataType;
    private Function<Map<String, Object>, Boolean> inclusionHandler;
    private boolean isComplete;

    private PathBinding() {
        this.isComplete = false;
        this.inclusionHandler = (_ignored) -> true;
    }

    public static PathBinding path(final String path) {
        final PathBinding binding = new PathBinding();
        binding.path = path;
        return binding;
    }

    public PathBinding name(final String metricName) {
        if (this.isComplete) {
            throw new IllegalStateException("Path binding is already complete");
        }
        this.metricName = metricName;
        return this;
    }

    public PathBinding type(final Class<?> dataType) {
        if (this.isComplete) {
            throw new IllegalStateException("Path binding is already complete");
        }
        this.dataType = dataType;
        return this;
    }

    public PathBinding handler(final Function<Map<String, Object>, Boolean> inclusionHandler) {
        if (this.isComplete) {
            throw new IllegalStateException("Path binding is already complete");
        }
        this.inclusionHandler = inclusionHandler;
        return this;
    }

    public PathBinding complete() {
        if (this.isComplete) {
            throw new IllegalStateException("Path binding is already complete");
        }
        this.isComplete = true;
        return this;
    }

    public void validate() {
        if (StringUtils.isBlank(this.path)) {
            throw new IllegalStateException("Path cannot be blank/empty/null in binding");
        } else if (StringUtils.isBlank(this.metricName)) {
            throw new IllegalStateException("Metric name cannot be blank/empty/null in binding");
        } else if (this.dataType == null) {
            throw new IllegalStateException("Data type cannot be null in binding");
        } else if (this.inclusionHandler == null) {
            throw new IllegalStateException("Inclusion handler cannot be null in binding");
        }
    }

    public String getPath() {
        return this.path;
    }

    public String getMetricName() {
        return this.metricName;
    }

    public Class<?> getDataType() {
        return this.dataType;
    }

    public Function<Map<String, Object>, Boolean> getInclusionHandler() {
        return this.inclusionHandler;
    }

}
