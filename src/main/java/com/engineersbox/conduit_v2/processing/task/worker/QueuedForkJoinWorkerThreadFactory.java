package com.engineersbox.conduit_v2.processing.task;

import com.google.common.collect.Queues;
import io.riemann.riemann.client.RiemannClient;

import java.util.Queue;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.function.Supplier;

public class QueuedForkJoinWorkerThreadFactory implements ForkJoinPool.ForkJoinWorkerThreadFactory, AutoCloseable {

    private final Supplier<RiemannClient> clientProvider;
    private final Queue<RiemannClient> clientQueue;

    public QueuedForkJoinWorkerThreadFactory(final Supplier<RiemannClient> clientProvider) {
        this.clientProvider = clientProvider;
        this.clientQueue = Queues.newConcurrentLinkedQueue();
    }

    @Override
    public final ForkJoinWorkerThread newThread(final ForkJoinPool pool) {
        if (!(pool instanceof ClientBoundForkJoinPool clientPool)) {
            throw new IllegalArgumentException("Queued fork-join worker threads must be bound to a ClientBoundForkJoinPool, instead got: " + pool.getClass().getName());
        }
        final RiemannClient client = this.clientProvider.get();
        this.clientQueue.add(client);
        return new ClientBoundForkJoinWorkerThead(clientPool, client);
    }

    @Override
    public void close() throws Exception {
        RiemannClient client;
        while ((client = this.clientQueue.poll()) != null) {
            client.close();
        }
    }

}
