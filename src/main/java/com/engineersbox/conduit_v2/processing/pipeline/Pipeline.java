package com.engineersbox.conduit_v2.processing.pipeline;

import org.apache.commons.lang3.reflect.TypeUtils;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class Pipeline implements Runnable {

    private final LinkedList<PipelineStage<?, ?>> stageQueue;

    public Pipeline() {
        this.stageQueue = new LinkedList<>();
    }

    public boolean addStage(final PipelineStage<?, ?> stage) {
        return this.stageQueue.add(stage);
    }

    public boolean addStages(final PipelineStage<?, ?> ...stages) {
        return addStages(List.of(stages));
    }

    public boolean addStages(final Collection<PipelineStage<?, ?>> stages) {
        return this.stageQueue.addAll(stages);
    }

    @Override
    public void run() {
        Object previousResult = null;
        for (final PipelineStage<?, ?> stage : this.stageQueue) {
            stage.resetState();
            stage.nextState(StageState.EXECUTING);
            previousResult = stage.apply0(previousResult);
            stage.nextState(StageState.FINISHED);
        }
    }
}
