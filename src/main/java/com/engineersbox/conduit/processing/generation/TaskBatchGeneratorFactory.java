package com.engineersbox.conduit.processing.generation;

import com.engineersbox.conduit.processing.task.MetricProcessingTask;
import org.jeasy.batch.core.job.JobExecutor;
import org.jeasy.batch.core.job.JobReport;

import java.util.List;
import java.util.concurrent.Future;

public abstract class TaskBatchGeneratorFactory {

    private TaskBatchGeneratorFactory() {
        throw new IllegalArgumentException("Factory class");
    }

    public static TaskBatchGenerator<List<Future<JobReport>>, JobExecutor> defaultGenerator() {
        return MetricProcessingTask::new;
    }

}
