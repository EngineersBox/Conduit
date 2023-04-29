package com.engineersbox.conduit_v2.processing.task;

import java.io.Serial;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RunnableFuture;

public class ClientBoundForkJoinTask extends ForkJoinTask<Void> implements RunnableFuture<Void> {

    @SuppressWarnings("serial") // Conditionally serializable
    private final MetricProcessingTask runnable;
    public ClientBoundForkJoinTask(final MetricProcessingTask runnable) {
        if (runnable == null) throw new NullPointerException();
        this.runnable = runnable;
    }
    public final Void getRawResult() {
        return null;
    }

    public final void setRawResult(final Void v) { }

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

    public String toString() {
        return super.toString() + "[Wrapped task = " + runnable + "]";
    }

    @Serial
    private static final long serialVersionUID = 5232453952276885070L;

}
