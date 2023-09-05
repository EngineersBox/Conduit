package com.engineersbox.conduit_v2.processing.task.worker.client;

import io.riemann.riemann.client.IRiemannClient;

public interface ClientPool {

    IRiemannClient acquire();

    void release(final IRiemannClient client);

    abstract class ClosableRiemannClient implements AutoCloseable {

        protected final IRiemannClient client;

        private ClosableRiemannClient(final IRiemannClient client) {
            this.client = client;
        }

        public IRiemannClient getClient() {
            return this.client;
        }

        @Override
        public abstract void close();

    }

    static ClosableRiemannClient acquireCloseable(final ClientPool clientPool) {
        return new ClosableRiemannClient(clientPool.acquire()) {
            @Override
            public void close() {
                clientPool.release(this.client);
            }
        };
    }

}
