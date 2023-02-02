package com.engineersbox.conduit.schema.metric;

import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

public class MetricType {

    private final Optional<MetricType> child;
    private final MetricContainerType containerType;
    private final MetricValueType valueType;
    private final String suffixFormat;

    public MetricType(final MetricType child,
                      final MetricContainerType containerType,
                      final String suffixFormat) {
        this.child = Optional.ofNullable(child);
        this.containerType = containerType;
        this.valueType = MetricValueType.CONTAINER;
        this.suffixFormat = StringUtils.isBlank(suffixFormat) ? "/{index}" : suffixFormat;
    }

    public MetricType(final MetricValueType valueType,
                      final String suffixFormat) {
        this.child = Optional.empty();
        this.containerType = MetricContainerType.NONE;
        this.valueType = valueType;
        this.suffixFormat = StringUtils.isBlank(suffixFormat) ? "/{index}" : suffixFormat;
    }

    public MetricType(final MetricType child,
                      final MetricContainerType containerType,
                      final MetricValueType valueType,
                      final String suffixFormat) {
        this.child = Optional.ofNullable(child);
        this.containerType = containerType;
        this.valueType = valueType;
        this.suffixFormat = StringUtils.isBlank(suffixFormat) ? "/{index}" : suffixFormat;
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

    public String getSuffixFormat() {
        return this.suffixFormat;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private MetricType _child;
        private MetricContainerType _containerType;
        private MetricValueType _valueType;
        private String _suffixFormat;

        private Builder() {
            this._child = null;
            this._containerType = MetricContainerType.NONE;
            this._valueType = MetricValueType.STRING;
            this._suffixFormat = "/{index}";
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

        public Builder withSuffixFormat(final String suffixFormat) {
            this._suffixFormat = suffixFormat;
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
