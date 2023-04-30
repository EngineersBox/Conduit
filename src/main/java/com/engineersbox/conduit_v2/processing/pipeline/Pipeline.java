package com.engineersbox.conduit_v2.processing.pipeline;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public class Pipeline<T> implements Consumer<T> {

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
    public void accept(final T initialValue) {
        Object previousResult = initialValue;
        for (final PipelineStage<?, ?> stage : this.stageQueue) {
            stage.resetState();
            stage.nextState(StageState.EXECUTING);
            previousResult = stage.apply0(previousResult);
            stage.nextState(StageState.FINISHED);
        }
    }
}
