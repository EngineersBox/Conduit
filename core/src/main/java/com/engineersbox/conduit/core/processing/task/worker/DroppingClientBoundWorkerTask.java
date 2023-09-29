package com.engineersbox.conduit.core.processing.task.worker;

import com.engineersbox.conduit.core.processing.pipeline.ProcessingModel;
import io.riemann.riemann.client.IRiemannClient;
import org.apache.commons.lang3.mutable.Mutable;
import org.eclipse.collections.api.map.MutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ForkJoinTask;

public class DroppingClientBoundWorkerTask<T, E, F extends ForkJoinTask<T>, C extends MutableMap<Integer, F>> implements ClientBoundWorkerTask<T, E> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DroppingClientBoundWorkerTask.class);

    private final ClientBoundWorkerTask<T, E> task;
    private final Mutable<F> ref;
    private final C dropFromCollection;

    public DroppingClientBoundWorkerTask(final ClientBoundWorkerTask<T, E> task,
                                         final Mutable<F> ref,
                                         final C dropFromCollection) {
        this.task = task;
        this.ref = ref;
        this.dropFromCollection = dropFromCollection;
    }

    @Override
    public ProcessingModel<T, E> apply(final IRiemannClient riemannClient) {
        final ProcessingModel<T, E> model = this.task.apply(riemannClient);
        final F forkJoinTask = ref.getValue();
        if (forkJoinTask == null) {
            throw new IllegalStateException("Held parent ForkJoinTask for submitted ClientBoundWorkerTask was not present");
        }
        LOGGER.trace(
                "Dropping ClientBoundWorkerTask {} from parent pool {}",
                forkJoinTask,
                this.dropFromCollection
        );
        this.dropFromCollection.remove(forkJoinTask.hashCode());
        return model;
    }
}
