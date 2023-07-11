package com.engineersbox.conduit_v2.processing.task;

import com.engineersbox.conduit_v2.processing.task.worker.ClientBoundWorkerTask;
import com.engineersbox.conduit_v2.processing.task.worker.DroppingClientBoundWorkerTask;
import com.engineersbox.conduit_v2.processing.task.worker.client.ClientPool;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;
import org.eclipse.collections.api.RichIterable;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.map.MutableMap;

import java.util.concurrent.ForkJoinTask;

public class WaitableTaskExecutorPool extends TaskExecutorPool {

    private final MutableMap<Long, MutableMap<Integer, ForkJoinTask<?>>> threadTaskMaps;

    public WaitableTaskExecutorPool(final ClientPool clientProvider,
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
