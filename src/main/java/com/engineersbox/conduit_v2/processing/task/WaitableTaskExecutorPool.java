package com.engineersbox.conduit_v2.processing.task;

import com.engineersbox.conduit_v2.processing.task.worker.ClientBoundWorkerTask;
import com.engineersbox.conduit_v2.processing.task.worker.DroppingClientBoundWorkerTask;
import com.engineersbox.conduit_v2.processing.task.worker.client.ClientPool;
import com.engineersbox.conduit_v2.processing.task.worker.executor.JobExecutorPool;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;
import org.eclipse.collections.api.RichIterable;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.map.MutableMap;

import java.util.concurrent.ForkJoinTask;

public class WaitableTaskExecutorPool<T, E> extends TaskExecutorPool<T, E> {

    private final MutableMap<Long, MutableMap<Integer, ForkJoinTask<T>>> threadTaskMaps;

    public WaitableTaskExecutorPool(final ClientPool clientProvider,
                                    final JobExecutorPool<E> jobExecutorPool,
                                    final int parallelism) {
        super(clientProvider, jobExecutorPool, parallelism);
        this.threadTaskMaps = Maps.mutable.withInitialCapacity(parallelism);
    }

    @Override
    public ForkJoinTask<T> submit(final ClientBoundWorkerTask<T, E> task) {
        return submit(
                task,
                Thread.currentThread().threadId()
        );
    }

    public ForkJoinTask<T> submit(final ClientBoundWorkerTask<T, E> task,
                                  final long origin) {
        final MutableMap<Integer, ForkJoinTask<T>> taskMap = this.threadTaskMaps.computeIfAbsent(
                origin,
                _origin -> Maps.mutable.empty()
        );
        final Mutable<ForkJoinTask<T>> taskReference = new MutableObject<>();
        final ForkJoinTask<T> forkJoinTask = super.submit(new DroppingClientBoundWorkerTask<>(
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
        final MutableMap<Integer, ForkJoinTask<T>> taskMap = this.threadTaskMaps.get(origin);
        taskMap.forEachValue(ForkJoinTask::quietlyJoin);
        taskMap.clear();
    }

    public RichIterable<? super ForkJoinTask<T>> getTasksView() {
        return getTasksView(Thread.currentThread().threadId());
    }

    public RichIterable<? super ForkJoinTask<T>> getTasksView(final long origin) {
        return this.threadTaskMaps.get(origin).valuesView();
    }

}
