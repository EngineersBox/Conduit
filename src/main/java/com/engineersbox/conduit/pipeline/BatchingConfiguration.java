package com.engineersbox.conduit.pipeline;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BatchingConfiguration {

    private final int threads;
    private final int bulkSize;

    public BatchingConfiguration(final int threads,
                                 final int bulkSize) {
        this.threads = threads;
        this.bulkSize = bulkSize;
    }

    public List<List<?>> splitWorkload(final List<?> workload) {
        // TODO: Implement this
        return List.of();
    }

    public ExecutorService generateExecutorService() {
        return Executors.newFixedThreadPool(this.threads);
    }

    public int getThreads() {
        return this.threads;
    }

    public int getBulkSize() {
        return this.bulkSize;
    }
}
