package com.engineersbox.conduit_v2.processing.task.worker;

import com.engineersbox.conduit_v2.processing.task.worker.client.ClientPool;
import io.riemann.riemann.client.IRiemannClient;

import java.util.concurrent.ForkJoinWorkerThread;

public class ClientBoundForkJoinWorkerThead extends ForkJoinWorkerThread {

    private final ClientPool clientPool;
    private IRiemannClient heldClient;
    private long affinityId;

    protected ClientBoundForkJoinWorkerThead(final ClientBoundForkJoinPool pool,
                                             final ClientPool clientPool) {
        super(pool);
        this.clientPool = clientPool;
        this.affinityId = threadId();
    }

    public IRiemannClient getClient() {
        if (this.heldClient == null) {
            this.heldClient = this.clientPool.acquire();
        }
        return this.heldClient;
    }

    public long setAffinityId(final long affinityId) {
        final long previousAffinityId = this.affinityId;
        this.affinityId = affinityId;
        return previousAffinityId;
    }

    public long getAffinityId() {
        return this.affinityId;
    }

    @Override
    protected void onTermination(final Throwable exception) {
        super.onTermination(exception);
        this.clientPool.release(this.heldClient);
    }

}
