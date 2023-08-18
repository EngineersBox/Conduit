package com.engineersbox.conduit_v2.retrieval.content.batch;

import com.engineersbox.conduit_v2.schema.metric.Metric;
import org.eclipse.collections.api.LazyIterable;
import org.eclipse.collections.api.RichIterable;

@FunctionalInterface
public interface WorkloadBatcher {

    LazyIterable<RichIterable<Metric>> chunk(final LazyIterable<Metric> metrics, final int chunkSize);

    static WorkloadBatcher defaultbatcher() {
        return LazyIterable::chunk;
    }

}
