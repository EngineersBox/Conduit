package com.engineersbox.conduit_v2.processing.pipeline;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class Pipeline<T> implements Consumer<T> {

    private final Map<String, Object> context;
    private final MutableList<PipelineStage<?, ?>> stageQueue;

    private Pipeline() {
        this.stageQueue = Lists.mutable.empty();
        this.context = new HashMap<>();
    }

    @Override
    public void accept(final T initialValue) {
        this.stageQueue.injectInto(
                initialValue,
                (final Object previousResult, final PipelineStage<?, ?> stage) -> {
                    stage.injectContext(this.context);
                    return stage.invoke0(previousResult);
                }
        );
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
