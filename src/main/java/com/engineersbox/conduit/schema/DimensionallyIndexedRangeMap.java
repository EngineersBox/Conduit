package com.engineersbox.conduit.schema;

import com.google.common.collect.Maps;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;
import org.apache.commons.collections4.MapUtils;

import java.util.NoSuchElementException;

public class DimensionallyIndexedRangeMap {

    private final RangeMap<Integer, RangeMap<Integer, String>> dimensionMap;

    public DimensionallyIndexedRangeMap() {
        this.dimensionMap = TreeRangeMap.create();
    }

    public String get(final DimensionIndex dimensionIndex) {
        if (!dimensionIndex.isSingletonQuery()) {
            throw new IllegalArgumentException("Cannot query with non-singleton DimensionIndex");
        }
        final RangeMap<Integer, String> indexMap = this.dimensionMap.get(dimensionIndex.getDimensionSingleton());
        if (indexMap == null) {
            return null;
        }
        return indexMap.get(dimensionIndex.getIndexSingleton());
    }

    public void put(final DimensionIndex dimensionIndex,
                    final String suffixFormat) {
        final RangeMap<Integer, String> indexMap = MapUtils.getObject(
                this.dimensionMap.asMapOfRanges(),
                dimensionIndex.getDimension(),
                TreeRangeMap.create()
        );
        indexMap.put(dimensionIndex.getIndex(), suffixFormat);
        this.dimensionMap.put(dimensionIndex.getDimension(), indexMap);
    }

    public int dimensionSize() {
        final Range<Integer> span;
        try {
            span = this.dimensionMap.span();
        } catch (final NoSuchElementException ignored) {
            return 0;
        }
        return span.upperEndpoint() + 1 - span.lowerEndpoint();
    }

}
