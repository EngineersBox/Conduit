package com.engineersbox.conduit_v2.processing.pipeline;

import com.google.common.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

public abstract class PipelineStage<T, R> implements InvocableStage<T, R> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PipelineStage.class);
    private Map<String, Object> context;
    private final String name;
    protected final Class<T> previousType;
    protected final Class<R> nextType;
    private final boolean typeChecked;

    protected PipelineStage(final String name) {
        this(name, false);
    }

    @SuppressWarnings("unchecked")
    protected PipelineStage(final String name,
                            final boolean typeChecked) {
        this.context = null;
        this.name = name;
        this.typeChecked = typeChecked;
        if (this.typeChecked) {
            final Type[] types = getStageTypeArguments();
            this.previousType = (Class<T>) TypeToken.of(types[0]).getRawType();
            this.nextType = (Class<R>) TypeToken.of(types.length == 1 ? types[0] : types[1]).getRawType();
        } else {
            this.previousType = null;
            this.nextType = null;
        }
    }

    private Type[] getStageTypeArguments() {
        Class<?> clazz;
        for (clazz = getClass(); !clazz.getSuperclass().equals(PipelineStage.class); clazz = clazz.getSuperclass());
        return ((ParameterizedType) clazz.getGenericSuperclass()).getActualTypeArguments();
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

    @SuppressWarnings({"unchecked"})
    StageResult<Object> invoke0(final Object previousResult) {
        final T castPreviousResult;
        if (this.typeChecked) {
            if (!this.previousType.isInstance(previousResult)) {
                throw new ClassCastException(String.format(
                        "Pipeline stage %s expects %s type for previous result, got %s",
                        this.name,
                        this.previousType.getName(),
                        previousResult.getClass().getName()
                ));
            }
            castPreviousResult = this.previousType.cast(previousResult);
        } else {
            castPreviousResult = (T) previousResult;
        }
        return (StageResult<Object>) invoke(castPreviousResult);
    }

}
