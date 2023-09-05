package com.engineersbox.conduit_v2.processing.task.worker.executor;

import org.jeasy.batch.core.job.JobExecutor;

public interface JobExecutorPool {

    JobExecutor acquire();

    void release(final JobExecutor jobExecutor);

    abstract class ClosableJobExecutor implements AutoCloseable {

        protected final JobExecutor jobExecutor;

        private ClosableJobExecutor(final JobExecutor jobExecutor) {
            this.jobExecutor = jobExecutor;
        }

        public JobExecutor getJobExecutor() {
            return this.jobExecutor;
        }

        @Override
        public abstract void close();

    }

    static ClosableJobExecutor acquireClosable(final JobExecutorPool jobExecutorPool) {
        return new ClosableJobExecutor(jobExecutorPool.acquire()) {
            @Override
            public void close() {
                jobExecutorPool.release(this.jobExecutor);
            }
        };
    }

}
