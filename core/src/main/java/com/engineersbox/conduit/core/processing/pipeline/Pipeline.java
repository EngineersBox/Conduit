package com.engineersbox.conduit.core.processing.pipeline;

import org.apache.commons.lang3.reflect.TypeUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.map.MutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Deprecated(forRemoval = true)
public class Pipeline<T> implements Consumer<T> {

    // TODO: Remove this class and associated classes

    private static final Logger LOGGER = LoggerFactory.getLogger(Pipeline.class);

    private final MutableMap<String, Object> context;
    private final MutableList<PipelineStage<?, ?>> stageQueue;

    private Pipeline() {
        this.stageQueue = Lists.mutable.empty();
        this.context = Maps.mutable.empty();
    }

    @Override
    public void accept(final T initialValue) {
        final Deque<Pair<StageResult<Object>, Integer>> valueQueue = new LinkedBlockingDeque<>();
        valueQueue.push(ImmutablePair.of(
                new StageResult<>(
                        StageResult.Type.SINGLETON,
                        initialValue,
                        false
                ),
                0
        ));
        final int stageCount = this.stageQueue.size();
        LOGGER.trace("Starting pipeline execution with {} stages", stageCount);
        while (!valueQueue.isEmpty()) {
            final Pair<StageResult<Object>, Integer> value = valueQueue.pop();
            final StageResult<Object> stageState = value.getLeft();
            final int stageIdx = value.getRight();
            if (stageState.type() == StageResult.Type.COMBINE) {
                final StageResult<Object> combinedValue = combineResults(stageState, valueQueue);
                valueQueue.push(ImmutablePair.of(
                        combinedValue,
                        stageIdx
                ));
                continue;
            }
            final PipelineStage<?, ?> stage = this.stageQueue.get(stageIdx);
            LOGGER.debug("[SPLIT | SINGLETON] Invoking pipeline stage: {}", stage.getName());
            final StageResult<Object> result = stage.invoke0(value.getKey().result());
            if (result.terminate()) {
                LOGGER.debug("Received termination condition from stage {}, terminating branch", stage.getName());
                continue;
            } else if (stageIdx == stageCount - 1) {
                LOGGER.debug("Reached end of pipeline stages, terminating branch");
                continue;
            }
            final Object resultValue = result.result();
            final StageResult.Type resultType = result.type();
            LOGGER.trace("Received {} type result from pipeline stage {}", resultType.name(), stage.getName());
            switch (resultType) {
                case SPLIT -> {
                    Stream<Object> valueStream;
                    if (TypeUtils.isArrayType(TypeUtils.wrap(resultValue.getClass()).getType())) {
                        valueStream = Arrays.stream((Object[]) resultValue);
                    } else if (resultValue instanceof Iterator<?> iterator) {
                        valueStream = StreamSupport.stream(Spliterators.spliteratorUnknownSize(
                                iterator,
                                0
                        ), false);
                    } else if (resultValue instanceof Iterable<?> iterable) {
                        valueStream = (Stream<Object>) StreamSupport.stream(iterable.spliterator(), false);
                    } else {
                        throw new ClassCastException(String.format(
                                "Expected splittable result type, got: %s",
                                resultValue.getClass().getName()
                        ));
                    }
                    valueStream.forEach((final Object o) -> valueQueue.push(
                            ImmutablePair.of(
                                    new StageResult<>(
                                            result.type(),
                                            o,
                                            result.terminate()
                                    ),
                                    stageIdx + 1
                            )
                    ));
                }
                case COMBINE, SINGLETON -> valueQueue.push(ImmutablePair.of(
                        result,
                        stageIdx + 1
                ));
            }
        }
        LOGGER.trace("Finished pipeline execution");
    }

    private StageResult<Object> combineResults(final StageResult<Object> primary,
                                               final Deque<Pair<StageResult<Object>, Integer>> deque) {
        final Object[] combined = new Object[primary.combineCount()];
        combined[0] = primary.result();
        final Type combineType = TypeUtils.wrap(combined[0].getClass()).getType();
        final int count = primary.combineCount();
        Pair<StageResult<Object>, Integer> val;
        for (int i = 1; i < count; i++) {
            val = deque.pop();
            final PipelineStage<?, ?> stage = this.stageQueue.get(val.getRight());
            LOGGER.debug("[COMBINE] Invoking pipeline stage: {}", stage.getName());
            final StageResult<Object> result = stage.invoke0(val.getLeft().result());
            final Object resultValue = result.result();
            final Type resultType = TypeUtils.wrap(resultValue.getClass()).getType();
            if (result.type() != StageResult.Type.COMBINE) {
                throw new IllegalStateException(String.format(
                        "Cannot combine results from non-COMBINE stage result at index %d in [0..%d)",
                        i, count
                ));
            } else if (!TypeUtils.equals(combineType, resultType)) {
                throw new IllegalStateException(String.format(
                        "Cannot combine results for non-matching stage result types: %s != %s",
                        combineType.getTypeName(),
                        resultType.getTypeName()
                ));
            }
            combined[i] = resultValue;
        }
        return new StageResult<>(
                StageResult.Type.SINGLETON,
                combined,
                false
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
            this.pipeline.stageQueue.forEach((final PipelineStage<?,?> stage) -> stage.injectContext(this.pipeline.context));
            return this.pipeline;
        }

    }

}
