package com.engineersbox.conduit_v2.processing.pipeline;

import org.apache.commons.lang3.mutable.Mutable;

import java.util.*;
import java.util.function.Consumer;

public class Pipeline<T> implements Consumer<T> {

    private final Map<String, Object> context;
    private final LinkedList<PipelineStage<?, ?>> stageQueue;

    private Pipeline() {
        this.stageQueue = new LinkedList<>();
        this.context = new HashMap<>();
    }

    @Override
    public void accept(final T initialValue) {
        Object previousResult = initialValue;
        for (final PipelineStage<?, ?> stage : this.stageQueue) {
            stage.injectContext(this.context);
            previousResult = stage.invoke0(previousResult);
        }
    }

    public static class Builder<T> {

        private final Pipeline<T> pipeline;

        public Builder() {
            this.pipeline = new Pipeline<>();
        }

        public Builder<T> withStage(final PipelineStage<?, ?> stage) {
            this.pipeline.stageQueue.add(stage);
            return this;
        }

        public Builder<T> withStages(final PipelineStage<?, ?> ...stages) {
            return withStages(List.of(stages));
        }

        public Builder<T> withStages(final Collection<PipelineStage<?, ?>> stages) {
            this.pipeline.stageQueue.addAll(stages);
            return this;
        }

        public Builder<T> withContext(final String key, final Object value) {
            this.pipeline.context.put(key, value);
            return this;
        }

        public Builder<T> withContext(final Map<String, Object> context) {
            this.pipeline.context.putAll(context);
            return this;
        }

        public Pipeline<T> build() {
            return this.pipeline;
        }

    }

}
