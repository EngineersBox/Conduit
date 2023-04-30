package com.engineersbox.conduit_v2.processing.pipeline;

public class PipelineFactory {

    public static Pipeline defaultPipeline() {
        final Pipeline pipeline = new Pipeline();
        pipeline.addStages();
        return pipeline;
    }

}
