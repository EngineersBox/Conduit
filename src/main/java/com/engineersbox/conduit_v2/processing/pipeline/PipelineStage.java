package com.engineersbox.conduit_v2.processing.pipeline;

import com.google.common.reflect.TypeToken;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.reflect.TypeUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

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
        this.previousType = (Class<T>) TypeToken.of(types[0]).getRawType();
        this.nextType = (Class<R>) TypeToken.of(types.length == 1 ? types[0] : types[1]).getRawType();
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
//        if (!this.previousType.isInstance(previousResult)) {
//            throw new ClassCastException(String.format(
//                    "Pipeline stage %s expects %s type for previous result, got %s",
//                    this.name,
//                    this.previousType.getName(),
//                    previousResult.getClass().getName()
//            ));
//        }
        return (StageResult<Object>) invoke((T) previousResult);//this.previousType.cast(previousResult));
    }

}
