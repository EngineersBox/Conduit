package com.engineersbox.conduit_v2.processing.pipeline;

import org.apache.commons.lang3.reflect.TypeUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.utility.Iterate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.function.Consumer;
import java.util.stream.Collector;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Pipeline<T> implements Consumer<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Pipeline.class);

    private final Map<String, Object> context;
    private final MutableList<PipelineStage<?, ?>> stageQueue;

    private Pipeline() {
        this.stageQueue = Lists.mutable.empty();
        this.context = new HashMap<>();
    }

    @Override
    public void accept(final T initialValue) {
        final Deque<Pair<StageResult<Object>, Integer>> valueQueue = new LinkedBlockingDeque<>();
        valueQueue.push(ImmutablePair.of(
                new StageResult<>(
                        StageResult.Type.SINGLE,
                        initialValue,
                        false
                ),
                0
        ));
        final int stageCount = this.stageQueue.size();
        while (!valueQueue.isEmpty()) {
            final Pair<StageResult<Object>, Integer> value = valueQueue.pop();
            final StageResult<Object> stageState = value.getLeft();
            final int stageIdx = value.getRight();
            if (stageState.type() == StageResult.Type.COMBINE) {
                final StageResult<Object> combinedValue = combineResults(stageState, valueQueue);
                valueQueue.addFirst(ImmutablePair.of(
                        combinedValue,
                        stageIdx + 1
                ));
                continue;
            }
            final StageResult<Object> result = stageQueue.get(stageIdx).invoke0(value.getKey().result());
            if (result.terminate() || stageIdx == stageCount - 1) {
                continue;
            }
            final Object resultValue = result.result();
            switch (result.type()) {
                case SPLIT -> {
                    Stream<Object> valueStream;
                    if (TypeUtils.isArrayType(TypeUtils.wrap(resultValue.getClass()).getType())) {
                        valueStream = Arrays.stream((Object[]) resultValue);
                    } else if (resultValue instanceof Collection<?> collection) {
                        valueStream = (Stream<Object>) collection.stream();
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
                    valueStream.forEach((final Object o) -> valueQueue.addFirst(
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
                case COMBINE -> {

                }
                case SINGLE -> valueQueue.addFirst(ImmutablePair.of(
                        result,
                        stageIdx + 1
                ));
            }
        }
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
            final StageResult<Object> result = val.getLeft();
            final Object resultValue = result.result();
            final Type resultType = TypeUtils.wrap(result.getClass()).getType();
            if (result.type() != StageResult.Type.COMBINE) {
                throw new IllegalStateException(String.format(
                        "Cannot combine results from non-COMBINE stage result at index %d of %d",
                        i, count
                ));
            } else if (!TypeUtils.equals(combineType, resultType)) {
                throw new IllegalStateException(String.format(
                        "Cannot combine results for non-matching stage result types: %s != %s",
                        combineType.getTypeName(),
                        resultType.getTypeName()
                ));
            }
            combined[1] = resultValue;
        }
        return new StageResult<>(
                StageResult.Type.SINGLE,
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
            return this.pipeline;
        }

    }

}
