package com.engineersbox.conduit_v2.processing.task.worker;

import com.engineersbox.conduit_v2.processing.task.worker.client.ClientPool;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;

public class QueuedForkJoinWorkerThreadFactory<T, E> implements ForkJoinPool.ForkJoinWorkerThreadFactory {

    private final ClientPool clientProvider;

    public QueuedForkJoinWorkerThreadFactory(final ClientPool clientProvider) {
        this.clientProvider = clientProvider;
    }

    @Override
    public final ForkJoinWorkerThread newThread(final ForkJoinPool pool) {
        if (!(pool instanceof ClientBoundForkJoinPool clientPool)) {
            throw new IllegalArgumentException("Queued fork-join worker threads must be bound to a ClientBoundForkJoinPool, instead got: " + pool.getClass().getName());
        }
        return new ClientBoundForkJoinWorkerThead<T, E>(clientPool, this.clientProvider);
    }

}
