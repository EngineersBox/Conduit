package com.engineersbox.conduit_v2.processing.schema.metric;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Range;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.map.ImmutableMap;

public class Metric {

    private final String namespace;
    private final String path;
    private final ParameterizedMetricType structure;
    private final DimensionallyIndexedRangeMap suffixes;

    @JsonCreator
    public Metric(@JsonProperty("namespace") final String namespace,
                  @JsonProperty("path") final String path,
                  @JsonProperty("structure") final ParameterizedMetricType structure) {
        this.namespace = namespace;
        this.path = path;
        this.structure = structure;
        this.suffixes = new DimensionallyIndexedRangeMap();
        extractSuffixesToMap(0, this.structure);
    }

    private void extractSuffixesToMap(final int dimension,
                                      final MetricType metricType) {
        if (metricType.isLeaf()) {
            return;
        }
        metricType.getSuffixes()
                .forEach((final Pair<Range<Integer>, String> format) -> this.suffixes.put(
                        new DimensionIndex(dimension, format.getLeft()),
                        format.getRight()
                ));
        extractSuffixesToMap(
                dimension + 1,
                metricType.getStructure()
        );
    }

    public String getPath() {
        return this.path;
    }

    public String getNamespace() {
        return this.namespace;
    }

    public ParameterizedMetricType getStructure() {
        return this.structure;
    }

    public String getSuffix(final DimensionIndex query) {
        return this.suffixes.get(query);
    }

    public ImmutableMap<String, String> getHandlers() {
        return Maps.immutable.of();
    }
}
