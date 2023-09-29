package com.engineersbox.conduit.core.processing.task.worker.executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.OverridingMethodsMustInvokeSuper;

public interface JobExecutorPool<E> {


    E acquire();

    void release(final E jobExecutor);

    abstract class ClosableJobExecutor<E> implements AutoCloseable {

        private static final Logger LOGGER = LoggerFactory.getLogger(ClosableJobExecutor.class);

        protected final E jobExecutor;

        private ClosableJobExecutor(final E jobExecutor) {
            LOGGER.trace("[CJE: {}] Acquired job executor: {}", this, jobExecutor);
            this.jobExecutor = jobExecutor;
        }

        public E getJobExecutor() {
            return this.jobExecutor;
        }

        @Override
        @OverridingMethodsMustInvokeSuper
        public void close() {
            LOGGER.trace("[CJE: {}] Released acquired job executor: {}", this, this.jobExecutor);
        }

    }

    static <E> ClosableJobExecutor<E> acquireClosable(final JobExecutorPool<E> jobExecutorPool) {

        return new ClosableJobExecutor<>(jobExecutorPool.acquire()) {
            @Override
            public void close() {
                super.close();
                jobExecutorPool.release(this.jobExecutor);
            }
        };
    }

}
