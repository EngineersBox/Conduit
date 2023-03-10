package com.engineersbox.conduit.schema.metric;

import com.google.common.collect.Range;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MetricType {

    private final Optional<MetricType> child;
    private final MetricContainerType containerType;
    private final MetricValueType valueType;
    private final List<Pair<Range<Integer>, String>> suffixFormat;

    public MetricType(final MetricType child,
                      final MetricContainerType containerType,
                      final List<Pair<Range<Integer>, String>> suffixFormat) {
        this.child = Optional.ofNullable(child);
        this.containerType = containerType;
        this.valueType = MetricValueType.CONTAINER;
        this.suffixFormat = suffixFormat.isEmpty()
                ? List.of(Pair.of(Range.all(), "/{index}"))
                : suffixFormat;
    }

    public MetricType(final MetricValueType valueType,
                      final List<Pair<Range<Integer>, String>> suffixFormat) {
        this.child = Optional.empty();
        this.containerType = MetricContainerType.NONE;
        this.valueType = valueType;
        this.suffixFormat = suffixFormat.isEmpty()
                ? List.of(Pair.of(Range.all(), "/{index}"))
                : suffixFormat;
    }

    public MetricType(final MetricType child,
                      final MetricContainerType containerType,
                      final MetricValueType valueType,
                      final List<Pair<Range<Integer>, String>> suffixFormat) {
        this.child = Optional.ofNullable(child);
        this.containerType = containerType;
        this.valueType = valueType;
        this.suffixFormat = suffixFormat.isEmpty()
                ? List.of(Pair.of(Range.all(), "/{index}"))
                : suffixFormat;
    }

    public boolean isLeaf() {
        return this.child.isEmpty();
    }

    public Optional<MetricType> getChild() {
        return this.child;
    }

    public MetricContainerType getContainerType() {
        return this.containerType;
    }

    public MetricValueType getValueType() {
        return this.valueType;
    }

    public List<Pair<Range<Integer>, String>> getSuffixFormat() {
        return this.suffixFormat;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private MetricType _child;
        private MetricContainerType _containerType;
        private MetricValueType _valueType;
        private List<Pair<Range<Integer>, String>> _suffixFormat;

        private Builder() {
            this._child = null;
            this._containerType = MetricContainerType.NONE;
            this._valueType = MetricValueType.STRING;
            this._suffixFormat = new ArrayList<>();
        }

        public Builder withChild(final MetricType child) {
            this._child = child;
            return this;
        }

        public Builder withContainerType(final MetricContainerType containerType) {
            this._containerType = containerType;
            return this;
        }

        public Builder withValueType(final MetricValueType valueType) {
            this._valueType = valueType;
            return this;
        }

        public Builder addSuffixFormat(final Range<Integer> range,
                                       final String format) {
            this._suffixFormat.add(Pair.of(range, format));
            return this;
        }

        public Builder withSuffixFormat(final List<Pair<Range<Integer>, String>> suffixFormat) {
            this._suffixFormat = suffixFormat;
            return this;
        }

        public MetricType build() {
            return new MetricType(
                    this._child,
                    this._containerType,
                    this._valueType,
                    this._suffixFormat
            );
        }
    }
}
