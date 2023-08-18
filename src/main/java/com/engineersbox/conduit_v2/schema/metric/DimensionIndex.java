package com.engineersbox.conduit_v2.schema.metric;

import com.google.common.collect.Range;

public class DimensionIndex {

    private final Range<Integer> dimension;
    private final Range<Integer> index;

    public DimensionIndex(final int dimension,
                          final int index) {
        this(
                Range.singleton(dimension),
                Range.singleton(index)
        );
    }

    public DimensionIndex(final Range<Integer> dimension,
                          final int index) {
        this(
                dimension,
                Range.singleton(index)
        );
    }

    public DimensionIndex(final int dimension,
                          final Range<Integer> index) {
        this(
                Range.singleton(dimension),
                index
        );
    }

    public DimensionIndex(final Range<Integer> dimension,
                          final Range<Integer> index) {
        this.dimension = dimension;
        this.index = index;
    }

    public static DimensionIndex ofQuery(final int dimension,
                                         final int index) {
        return new DimensionIndex(dimension, index);
    }

    public boolean isSingletonQuery() {
        return this.dimension.lowerEndpoint().equals(this.dimension.upperEndpoint())
                && this.index.lowerEndpoint().equals(this.index.upperEndpoint());
    }

    public Range<Integer> getDimension() {
        return this.dimension;
    }

    public int getDimensionSingleton() {
        return this.dimension.lowerEndpoint();
    }

    public Range<Integer> getIndex() {
        return this.index;
    }

    public int getIndexSingleton() {
        return this.index.lowerEndpoint();
    }

}
