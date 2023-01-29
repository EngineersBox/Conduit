package com.engineersbox.conduit.schema;

import com.jayway.jsonpath.TypeRef;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.function.Function;

public class PathBinding {

    private String path;
    private String metricNamespace;
    private TypeRef<?> dataType;
    private int dimension;
    private DimensionallyIndexedRangeMap dimensionalNamespaceSuffix;
    private Function<Map<String, Object>, Boolean> inclusionHandler;
    private boolean isComplete;

    private PathBinding() {
        this.dimension = 1;
        this.dimensionalNamespaceSuffix = new DimensionallyIndexedRangeMap();
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
        this.metricNamespace = metricName;
        return this;
    }

    public PathBinding type(final TypeRef<?> dataType) {
        if (this.isComplete) {
            throw new IllegalStateException("Path binding is already complete");
        }
        this.dataType = dataType;
        return this;
    }

    public PathBinding dimensions(final int dimension) {
        if (this.isComplete) {
            throw new IllegalStateException("Path binding is already complete");
        } else if (dimension < 0) {
            throw new IllegalArgumentException("Dimension must be positive");
        }
        this.dimension = dimension;
        return this;
    }

    public PathBinding suffixes(final DimensionallyIndexedRangeMap suffixes) {
        if (this.isComplete) {
            throw new IllegalStateException("Path binding is already complete");
        } else if (this.dimension != suffixes.dimensionSize()) {
            throw new IllegalStateException(String.format(
                    "Existing 'dimension' property does not match with dimensions of suffix map: %d != %d",
                    this.dimension,
                    suffixes.dimensionSize()
            ));
        }
        this.dimensionalNamespaceSuffix = suffixes;
        return this;
    }

    public PathBinding suffix(final DimensionIndex dimensionIndex,
                              final String suffixFormat) {
        if (this.isComplete) {
            throw new IllegalStateException("Path binding is already complete");
        } else if (dimensionIndex.getDimension().upperEndpoint() > this.dimension) {
            throw new IllegalArgumentException(String.format(
                    "Dimension range in provided DimensionIndex cannot exceed configured dimension as an upper bound: %s > %s",
                    dimensionIndex.getDimension().upperEndpoint(),
                    this.dimension
            ));
        } else if (dimensionIndex.getDimension().lowerEndpoint() < 0) {
            throw new IllegalArgumentException(String.format(
                    "Dimension range in provided DimensionIndex cannot have negative lower bound: {} < 0",
                    dimensionIndex.getDimension().lowerEndpoint()
            ));
        }
        this.dimensionalNamespaceSuffix.put(dimensionIndex, suffixFormat);
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
        } else if (StringUtils.isBlank(this.metricNamespace)) {
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
        return this.metricNamespace;
    }

    public TypeRef<?> getDataType() {
        return this.dataType;
    }

    public Function<Map<String, Object>, Boolean> getInclusionHandler() {
        return this.inclusionHandler;
    }

}
