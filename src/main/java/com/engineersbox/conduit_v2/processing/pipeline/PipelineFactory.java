package com.engineersbox.conduit_v2.processing.pipeline;

public class PipelineFactory {

    public static <T> Pipeline<T> defaultPipeline() {
        final Pipeline<T> pipeline = new Pipeline.Builder<T>()
                .withStages()
                .build();
        return pipeline;
    }

}
