package com.engineersbox.conduit_v2.processing.task.worker;

import io.riemann.riemann.client.RiemannClient;
import org.eclipse.collections.api.collection.MutableCollection;

import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.atomic.AtomicReference;

public class DroppingClientBoundWorkerTask<T extends ForkJoinTask<?>, C extends MutableCollection<T>> implements ClientBoundWorkerTask {

    private final ClientBoundWorkerTask task;
    private final AtomicReference<T> ref;
    private final C dropFromCollection;

    public DroppingClientBoundWorkerTask(final ClientBoundWorkerTask task,
                                         final AtomicReference<T> ref,
                                         final C dropFromCollection) {
        this.task = task;
        this.ref = ref;
        this.dropFromCollection = dropFromCollection;
    }

    @Override
    public void accept(final RiemannClient riemannClient) {
        this.task.accept(riemannClient);
        final T forkJoinTask = ref.get();
        if (forkJoinTask == null) {
            throw new IllegalStateException("Held parent ForkJoinTask for submitted ClientBoundWorkerTask was not present");
        }
        this.dropFromCollection.remove(forkJoinTask);
    }
}
