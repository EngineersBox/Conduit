package com.engineersbox.conduit_v2.processing.pipeline;

import com.jayway.jsonpath.TypeRef;
import org.apache.commons.lang3.reflect.TypeLiteral;

import java.util.function.Function;

public abstract class PipelineStage<T, R> implements Function<T, R> {

    private final String name;
    private final StageType type;
    private StageState state;
    private final Class<T> previousType;
    private final Class<R> nextType;

    protected PipelineStage(final String name,
                            final StageType type) {
        this.name = name;
        this.type = type;
        this.state = StageState.PENDING;
        this.previousType = (Class<T>) new TypeLiteral<T>(){}.getType();
        this.nextType = (Class<R>) new TypeLiteral<R>(){}.getType();
    }

    public String getName() {
        return this.name;
    }

    public StageType getType() {
        return this.type;
    }

    public StageState getState() {
        return this.state;
    }

    public void nextState(final StageState newState) {
        final StageState next = this.state.next();
        if (next != null && next != newState) {
            throw new IllegalStateException(String.format(
                    "Pipeline stage %s was moved to an invalid state %s, it was expected to be %s",
                    this.name,
                    newState.name(),
                    next.name()
            ));
        }
        this.state = next;
    }

    public void resetState() {
        if (!this.state.isLast() || this.state.equals(StageState.EVICTED)) {
            throw new IllegalStateException("Cannot reset stage in non-final state: " + this.state.name());
        }
        this.state = StageState.PENDING;
    }

    @Override
    public abstract R apply(final T previousResult);

    Object apply0(final Object previousResult) {
        if (!this.previousType.isInstance(previousResult)) {
            throw new ClassCastException(String.format(
                    "Pipeline stage %s expects %s type for previous result, got %s",
                    this.name,
                    this.previousType.getName(),
                    previousResult.getClass().getName()
            ));
        }
        return apply(this.previousType.cast(previousResult));
    }

}
