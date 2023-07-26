package com.engineersbox.conduit_v2.processing.task.worker;

import com.engineersbox.conduit_v2.processing.task.worker.client.ClientPool;
import io.riemann.riemann.client.IRiemannClient;

import java.util.concurrent.ForkJoinWorkerThread;

public class ClientBoundForkJoinWorkerThead extends ForkJoinWorkerThread {

    private final ClientPool clientPool;
    private IRiemannClient heldClient;
    private long cacheAffinityId;

    protected ClientBoundForkJoinWorkerThead(final ClientBoundForkJoinPool pool,
                                             final ClientPool clientPool) {
        super(pool);
        this.clientPool = clientPool;
        setCacheAffinityId(threadId());
    }

    protected ClientBoundForkJoinWorkerThead(final ClientBoundForkJoinPool pool,
                                             final ClientPool clientPool,
                                             final long cacheAffinityId) {
        super(pool);
        this.clientPool = clientPool;
        setCacheAffinityId(cacheAffinityId);
    }

    public IRiemannClient getClient() {
        if (this.heldClient == null) {
            this.heldClient = this.clientPool.acquire();
        }
        return this.heldClient;
    }

    public long setCacheAffinityId(final long cacheAffinityId) {
        final long previousAffinityId = this.cacheAffinityId;
        this.cacheAffinityId = cacheAffinityId;
        return previousAffinityId;
    }

    public long getCacheAffinityId() {
        return this.cacheAffinityId;
    }

    @Override
    protected void onTermination(final Throwable exception) {
        super.onTermination(exception);
        this.clientPool.release(this.heldClient);
    }

}
