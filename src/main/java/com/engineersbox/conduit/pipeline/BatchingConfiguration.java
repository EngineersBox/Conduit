package com.engineersbox.conduit.pipeline;

import com.google.common.collect.Lists;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public record BatchingConfiguration(int threads, int bulkSize) {

    public <T> List<List<T>> splitWorkload(final Collection<T> workload) {
        return Lists.partition(workload.stream().toList(), this.bulkSize);
    }

    public ExecutorService generateExecutorService() {
        return Executors.newFixedThreadPool(this.threads);
    }
}
