package com.engineersbox.conduit.schema.metric;

import com.engineersbox.conduit.schema.extension.ExtensionDeserializer;
import com.engineersbox.conduit.schema.json.SuffixFormatDeserializer;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.collect.Range;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.map.ImmutableMap;

import java.util.Objects;

public class Metric {

    private final String namespace;
    private final String path;
    private final ParameterizedMetricType structure;
    private final DimensionallyIndexedRangeMap suffixes;
    private final ImmutableMap<String, Object> extensions;

    @JsonCreator
    public Metric(@JsonProperty("namespace") final String namespace,
                  @JsonProperty("path") final String path,
                  @JsonProperty("structure") final ParameterizedMetricType structure,
                  @JsonProperty("extensions") @JsonDeserialize(using = ExtensionDeserializer.class) final ImmutableMap<String, Object> extensions) {
        this.namespace = namespace;
        this.path = path;
        this.structure = structure;
        this.extensions = Objects.requireNonNullElseGet(extensions, Maps.immutable::empty);
        if (!structure.getSuffixes().isEmpty()) {
            this.suffixes = new DimensionallyIndexedRangeMap();
            extractSuffixesToMap(0, this.structure);
        } else {
            this.suffixes = null;
        }
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
        return this.suffixes == null
                ? SuffixFormatDeserializer.DEFAULT_SUFFIX_FORMAT
                : this.suffixes.get(query);
    }

    public ImmutableMap<String, String> getHandlers() {
        return Maps.immutable.of();
    }

    public ImmutableMap<String, Object> getExtensions() {
        return this.extensions;
    }

}
