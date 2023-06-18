package com.engineersbox.conduit_v2.processing.pipeline;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Optional;

public abstract class PipelineStage<T, R> implements InvocableStage<T, R> {

    private Map<String, Object> context;
    private final String name;
    protected final Class<T> previousType;
    protected final Class<R> nextType;

    @SuppressWarnings("unchecked")
    protected PipelineStage(final String name) {
        this.context = null;
        this.name = name;
        final Type[] types = getStageTypeArguments();
        this.previousType = (Class<T>) types[0];
        this.nextType = (Class<R>) (types.length == 1 ? types[0] : types[1]);
    }

    private Type[] getStageTypeArguments() {
        return ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments();
    }

    public String getName() {
        return this.name;
    }

    void injectContext(final Map<String, Object> context) {
        this.context = context;
    }

    protected Object getContextAttribute(final String key) {
        return this.context.get(key);
    }

    protected void setContextAttribute(final String key, final Object value) {
        this.context.put(key, value);
    }

    @Override
    public abstract StageResult<R> invoke(final T previousResult);

    @SuppressWarnings("unchecked")
    StageResult<Object> invoke0(final Object previousResult) {
        if (!this.previousType.isInstance(previousResult)) {
            throw new ClassCastException(String.format(
                    "Pipeline stage %s expects %s type for previous result, got %s",
                    this.name,
                    this.previousType.getName(),
                    previousResult.getClass().getName()
            ));
        }
        return (StageResult<Object>) invoke(this.previousType.cast(previousResult));
    }

}
