package com.engineersbox.conduit.pipeline;

import com.google.common.collect.Lists;

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

    public <T> List<List<T>> splitWorkload(final List<T> workload) {
        return Lists.partition(workload, this.bulkSize);
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
