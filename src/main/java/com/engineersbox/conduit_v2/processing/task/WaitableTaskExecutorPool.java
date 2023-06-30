package com.engineersbox.conduit_v2.processing.task;

import com.engineersbox.conduit_v2.processing.task.worker.ClientBoundWorkerTask;
import com.engineersbox.conduit_v2.processing.task.worker.DroppingClientBoundWorkerTask;
import io.riemann.riemann.client.RiemannClient;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;
import org.eclipse.collections.api.RichIterable;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.map.MutableMap;

import java.util.concurrent.ForkJoinTask;
import java.util.function.Supplier;

public class WaitableTaskExecutorPool extends TaskExecutorPool {

    private final MutableMap<Long, MutableMap<Integer, ForkJoinTask<?>>> threadTaskMaps;
    // TODO: Support a pool of clients to be usable in this executor pool
    //       A thread should pull from the client pool based on some condition
    //       provided as a predicate for allowing it to be selected (e.g. count
    //       incremented. Once the thread is finished with the client it is
    //       returned to the pool and the condition state is updated (e.g.
    //       count decremented).

    public WaitableTaskExecutorPool(final Supplier<RiemannClient> clientProvider,
                                    final int parallelism) {
        super(clientProvider, parallelism);
        this.threadTaskMaps = Maps.mutable.withInitialCapacity(parallelism);
    }

    @Override
    public ForkJoinTask<?> submit(final ClientBoundWorkerTask task) {
        return submit(
                task,
                Thread.currentThread().threadId()
        );
    }

    public ForkJoinTask<?> submit(final ClientBoundWorkerTask task,
                                  final long origin) {
        final MutableMap<Integer, ForkJoinTask<?>> taskMap = this.threadTaskMaps.computeIfAbsent(
                origin,
                _origin -> Maps.mutable.empty()
        );
        final Mutable<ForkJoinTask<?>> taskReference = new MutableObject<>();
        final ForkJoinTask<?> forkJoinTask = super.submit(new DroppingClientBoundWorkerTask<>(
                task,
                taskReference,
                taskMap
        ));
        taskReference.setValue(forkJoinTask);
        taskMap.put(forkJoinTask.hashCode(), forkJoinTask);
        return forkJoinTask;
    }

    public void resettingBarrier() {
        resettingBarrier(Thread.currentThread().threadId());
    }

    public void resettingBarrier(final long origin) {
        final MutableMap<Integer, ForkJoinTask<?>> taskMap = this.threadTaskMaps.get(origin);
        taskMap.forEachValue(ForkJoinTask::quietlyJoin);
        taskMap.clear();
    }

    public RichIterable<? super ForkJoinTask<?>> getTasksView() {
        return getTasksView(Thread.currentThread().threadId());
    }

    public RichIterable<? super ForkJoinTask<?>> getTasksView(final long origin) {
        return this.threadTaskMaps.get(origin).valuesView();
    }

}
