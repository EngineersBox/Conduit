package com.engineersbox.conduit.processing.task.worker;

import com.engineersbox.conduit.processing.task.worker.client.ClientPool;
import io.riemann.riemann.client.IRiemannClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ForkJoinWorkerThread;

public class ClientBoundForkJoinWorkerThead<T, E> extends ForkJoinWorkerThread {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientBoundForkJoinWorkerThead.class);

    private final ClientPool clientPool;
    private IRiemannClient heldClient;
    private long affinityId;

    protected ClientBoundForkJoinWorkerThead(final ClientBoundForkJoinPool<T, E> pool,
                                             final ClientPool clientPool) {
        super(pool);
        this.clientPool = clientPool;
        setAffinityId(threadId());
    }

    protected ClientBoundForkJoinWorkerThead(final ClientBoundForkJoinPool<T, E> pool,
                                             final ClientPool clientPool,
                                             final long affinityId) {
        super(pool);
        this.clientPool = clientPool;
        setAffinityId(affinityId);
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

    public static long getThreadAffinityId() {
        final Thread currentThread = Thread.currentThread();
        final long affinityId;
        if (currentThread instanceof ClientBoundForkJoinWorkerThead cbfjwThread) {
            affinityId = cbfjwThread.getAffinityId();
        } else {
            affinityId = currentThread.threadId();
            LOGGER.trace(
                    "Thread was not an instance of ClientBoundForkJoinWorkerThread, defaulting to thread ID [{}] for affinity ID",
                    affinityId
            );
        }
        return affinityId;
    }

    public static long setThreadAffinityId(final long affinityId) {
        final Thread currentThread = Thread.currentThread();
        if (currentThread instanceof ClientBoundForkJoinWorkerThead cbfjwThread) {
            return cbfjwThread.setAffinityId(affinityId);
        }
        throw new IllegalStateException("Cannot set affinity ID on non-ClientBoundForkJoinWorkerThread");
    }

}
