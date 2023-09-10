package com.engineersbox.conduit_v2.processing.task.worker.executor;

public interface JobExecutorPool<E> {

    E acquire();

    void release(final E jobExecutor);

    abstract class ClosableJobExecutor<E> implements AutoCloseable {

        protected final E jobExecutor;

        private ClosableJobExecutor(final E jobExecutor) {
            this.jobExecutor = jobExecutor;
        }

        public E getJobExecutor() {
            return this.jobExecutor;
        }

        @Override
        public abstract void close();

    }

    static <E> ClosableJobExecutor<E> acquireClosable(final JobExecutorPool<E> jobExecutorPool) {
        return new ClosableJobExecutor<>(jobExecutorPool.acquire()) {
            @Override
            public void close() {
                jobExecutorPool.release(this.jobExecutor);
            }
        };
    }

}
