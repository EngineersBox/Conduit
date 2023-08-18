package com.engineersbox.conduit_v2.schema.metric;

import com.google.common.collect.Range;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.collections.api.LazyIterable;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.impl.factory.Iterables;

public class MetricType {

    private static final LazyIterable<Pair<Range<Integer>, String>> DEFAULT_EMPTY_ITERABLE = Iterables.<Pair<Range<Integer>, String>>iList().asLazy();

    private final MetricKind type; // Union<ContainerType | ValueType>
    private final ParameterizedMetricType structure; // Child element type structure, nullable
    private final ImmutableList<Pair<Range<Integer>, String>> suffixes;

    public MetricType(final MetricKind type,
                      final ParameterizedMetricType structure,
                      final ImmutableList<Pair<Range<Integer>, String>> suffixes) {
        this.type = type;
        this.structure = structure;
        this.suffixes = suffixes;
    }

    public boolean isLeaf() {
        return this.structure == null;
    }

    public MetricKind getType() {
        return this.type;
    }

    public ParameterizedMetricType getStructure() {
        return this.structure;
    }

    public LazyIterable<Pair<Range<Integer>, String>> getSuffixes() {
        return this.suffixes == null ? DEFAULT_EMPTY_ITERABLE : this.suffixes.asLazy();
    }

}
