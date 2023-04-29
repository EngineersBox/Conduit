package com.engineersbox.conduit_v2.processing.task.worker;

import com.engineersbox.conduit_v2.processing.task.MetricProcessingTask;

import java.io.Serial;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RunnableFuture;

public class ClientBoundForkJoinTask extends ForkJoinTask<Void> implements RunnableFuture<Void> {

    @Serial
    private static final long serialVersionUID = 2988328017776527845L;

    @SuppressWarnings("serial") // Conditionally serializable
    private final MetricProcessingTask runnable;

    public ClientBoundForkJoinTask(final MetricProcessingTask runnable) {
        if (runnable == null) throw new NullPointerException();
        this.runnable = runnable;
    }

    @Override
    public final Void getRawResult() {
        return null;
    }

    @Override
    public final void setRawResult(final Void v) {}

    @Override
    public final boolean exec() {
        final Thread thread;
        if ((thread = Thread.currentThread()) instanceof ClientBoundForkJoinWorkerThead workerThread) {
            runnable.accept(workerThread.getClient());
            return true;
        }
        throw new IllegalThreadStateException("Cannot invoke client bound fork-join task on non-ClientBoundForkJoinWorkerThread instances, got instead: " + thread.getClass().getName());
    }

    @Override
    public final void run() {
        invoke();
    }

    @Override
    public String toString() {
        return super.toString() + "[Wrapped task = " + this.runnable + "]";
    }

}
