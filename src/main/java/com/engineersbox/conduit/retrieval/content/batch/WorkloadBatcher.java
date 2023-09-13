package com.engineersbox.conduit.retrieval.content.batch;

import com.engineersbox.conduit.schema.metric.Metric;
import org.eclipse.collections.api.RichIterable;

@FunctionalInterface
public interface WorkloadBatcher {

    RichIterable<RichIterable<Metric>> chunk(final RichIterable<Metric> metrics, final int chunkSize);

    static WorkloadBatcher defaultBatcher() {
        return RichIterable::chunk;
    }

}
