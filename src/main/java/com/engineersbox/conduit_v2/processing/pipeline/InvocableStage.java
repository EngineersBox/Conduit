package com.engineersbox.conduit_v2.processing.pipeline;

import java.util.Optional;

@FunctionalInterface
public interface InvocableStage<T, R> {

    StageResult<R> invoke(final T t);

}
