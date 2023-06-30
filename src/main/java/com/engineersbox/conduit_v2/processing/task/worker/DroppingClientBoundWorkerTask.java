package com.engineersbox.conduit_v2.processing.task.worker;

import io.riemann.riemann.client.RiemannClient;
import org.apache.commons.lang3.mutable.Mutable;
import org.eclipse.collections.api.collection.MutableCollection;
import org.eclipse.collections.api.map.MutableMap;

import java.util.concurrent.ForkJoinTask;

public class DroppingClientBoundWorkerTask<T extends ForkJoinTask<?>, C extends MutableMap<Integer, T>> implements ClientBoundWorkerTask {

    private final ClientBoundWorkerTask task;
    private final Mutable<T> ref;
    private final C dropFromCollection;

    public DroppingClientBoundWorkerTask(final ClientBoundWorkerTask task,
                                         final Mutable<T> ref,
                                         final C dropFromCollection) {
        this.task = task;
        this.ref = ref;
        this.dropFromCollection = dropFromCollection;
    }

    @Override
    public void accept(final RiemannClient riemannClient) {
        this.task.accept(riemannClient);
        final T forkJoinTask = ref.getValue();
        if (forkJoinTask == null) {
            throw new IllegalStateException("Held parent ForkJoinTask for submitted ClientBoundWorkerTask was not present");
        }
        this.dropFromCollection.remove(forkJoinTask.hashCode());
    }
}
