package com.engineersbox.conduit_v2.processing.generation;

import com.engineersbox.conduit_v2.processing.task.MetricProcessingTask;

public abstract class TaskBatchGeneratorFactory {

    private TaskBatchGeneratorFactory() {
        throw new IllegalArgumentException("Factory class");
    }

    public static TaskBatchGenerator defaultGenerator() {
        return MetricProcessingTask::new;
    }

}
