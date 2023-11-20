package com.engineersbox.conduit.core.processing.task.worker.client;

import io.riemann.riemann.client.IRiemannClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.OverridingMethodsMustInvokeSuper;

public interface ClientPool {

    IRiemannClient acquire();

    void release(final IRiemannClient client);

    abstract class ClosableRiemannClient implements AutoCloseable {

        private static final Logger LOGGER = LoggerFactory.getLogger(ClosableRiemannClient.class);

        protected final IRiemannClient client;

        private ClosableRiemannClient(final IRiemannClient client) {
            LOGGER.trace("[CRC: {}] Acquired riemann client: {}", this, client);
            this.client = client;
        }

        public IRiemannClient getClient() {
            return this.client;
        }

        @Override
        @OverridingMethodsMustInvokeSuper
        public void close() {
            LOGGER.trace("[CRC: {}] Released acquired riemann client: {}", this, this.client);
        }

    }

    static ClosableRiemannClient acquireCloseable(final ClientPool clientPool) {
        return new ClosableRiemannClient(clientPool.acquire()) {
            @Override
            public void close() {
                clientPool.release(this.client);
                super.close();
            }
        };
    }

}
