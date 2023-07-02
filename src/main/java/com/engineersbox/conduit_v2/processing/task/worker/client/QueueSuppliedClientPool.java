package com.engineersbox.conduit_v2.processing.task.worker.client;

import io.riemann.riemann.client.IRiemannClient;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

public class QueueSuppliedClientPool implements ClientPool {

    private final int poolSize;
    private final AtomicInteger queueSize;
    private final Lock lock;
    private final Deque<IRiemannClient> clientQueue;
    private final Supplier<IRiemannClient> clientProvider;

    public QueueSuppliedClientPool(final Supplier<IRiemannClient> clientProvider,
                                   final int poolSize) {
        this.clientProvider = clientProvider;
        this.queueSize = new AtomicInteger(0);
        this.poolSize = poolSize;
        this.lock = new ReentrantLock(true);
        this.clientQueue = new ArrayDeque<>(poolSize);
    }

    @Override
    public IRiemannClient acquire() {
        IRiemannClient client;
        while (true) {
            lock.lock();
            try {
                client = this.clientQueue.poll();
                if (client != null) {
                    break;
                } else if (this.queueSize.get() == this.poolSize) {
                    client = this.clientProvider.get();
                    break;
                }
            } finally {
                lock.unlock();
            }
        }
        return client;
    }
    

    @Override
    public void release(final IRiemannClient client) {
        if (this.queueSize.get() == this.poolSize) {
            throw new IllegalArgumentException("Cannot release client to full pool");
        }
        try {
            lock.lock();
            this.clientQueue.push(client);
        } finally {
            lock.unlock();
        }
    }
}
