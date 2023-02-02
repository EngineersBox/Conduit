package com.engineersbox.conduit.schema.metric;

import com.engineersbox.conduit.schema.DimensionIndex;
import com.engineersbox.conduit.schema.DimensionallyIndexedRangeMap;
import com.google.common.collect.Range;
import com.jayway.jsonpath.TypeRef;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.function.Function;

public class Metric {

    private String path;
    private String metricNamespace;
    private ParameterizedMetricType type;
    private final DimensionallyIndexedRangeMap suffixes;
    private boolean isComplete;

    private Metric() {
        this.suffixes = new DimensionallyIndexedRangeMap();
        this.isComplete = false;
    }

    public static Metric path(final String path) {
        final Metric binding = new Metric();
        binding.path = path;
        return binding;
    }

    public Metric namespace(final String namespace) {
        if (this.isComplete) {
            throw new IllegalStateException("Path binding is already complete");
        }
        this.metricNamespace = namespace;
        return this;
    }

    public Metric type(final MetricType type) {
        if (this.isComplete) {
            throw new IllegalStateException("Path binding is already complete");
        }
        this.type = new ParameterizedMetricType(
                type.getChild().orElse(null),
                type.getContainerType(),
                type.getValueType(),
                type.getSuffixFormat()
        );
        extractSuffixesToMap(0, type);
        return this;
    }

    private void extractSuffixesToMap(final int dimension, final MetricType metricType) {
        if (metricType.getChild().isEmpty()) {
            return;
        }
        this.suffixes.put(
                new DimensionIndex(dimension, Range.all()),
                metricType.getSuffixFormat()
        );
        extractSuffixesToMap(
                dimension + 1,
                metricType.getChild().get()
        );
    }

    public Metric complete() {
        if (this.isComplete) {
            throw new IllegalStateException("Path binding is already complete");
        }
        this.isComplete = true;
        return this;
    }

    public void validate() {
        if (StringUtils.isBlank(this.path)) {
            throw new IllegalStateException("Path cannot be blank/empty/null in binding");
        } else if (StringUtils.isBlank(this.metricNamespace)) {
            throw new IllegalStateException("Metric name cannot be blank/empty/null in binding");
        } else if (this.type == null) {
            throw new IllegalStateException("Data type cannot be null in binding");
        }
    }

    public String getPath() {
        return this.path;
    }

    public String getMetricNamespace() {
        return this.metricNamespace;
    }

    public String getSuffix(final DimensionIndex query) {
        return this.suffixes.get(query);
    }

    public ParameterizedMetricType getType() {
        return this.type;
    }

}
