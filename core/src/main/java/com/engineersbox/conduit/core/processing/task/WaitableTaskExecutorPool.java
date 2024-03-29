package com.engineersbox.conduit.core.processing.task;

import com.engineersbox.conduit.core.processing.task.worker.client.ClientPool;
import com.engineersbox.conduit.core.processing.task.worker.executor.JobExecutorPool;
import com.engineersbox.conduit.core.processing.task.worker.ClientBoundWorkerTask;
import com.engineersbox.conduit.core.processing.task.worker.DroppingClientBoundWorkerTask;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;
import org.eclipse.collections.api.RichIterable;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.map.MutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.ForkJoinTask;

@ThreadSafe
public class WaitableTaskExecutorPool<T, E> extends TaskExecutorPool<T, E> {

    private static final Logger LOGGER = LoggerFactory.getLogger(WaitableTaskExecutorPool.class);
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
        LOGGER.trace(
                "Submitted DroppingClientBoundWorkerTask [{}] to executor pool",
                forkJoinTask.hashCode()
        );
        return forkJoinTask;
    }

    public void resettingBarrier(final boolean ignoreNull) {
        resettingBarrier(ignoreNull, Thread.currentThread().threadId());
    }

    public void resettingBarrier(final boolean ignoreNull, final long origin) {
        final MutableMap<Integer, ForkJoinTask<T>> taskMap = this.threadTaskMaps.get(origin);
        if (ignoreNull && taskMap == null) {
            LOGGER.trace("No task mappings found for origin id {}, ignoreNull was set, skipping task join", origin);
            return;
        }
        taskMap.forEachValue(ForkJoinTask::quietlyJoin);
        taskMap.clear();
    }

    public RichIterable<ForkJoinTask<T>> getTasksView() {
        return getTasksView(Thread.currentThread().threadId());
    }

    public RichIterable<ForkJoinTask<T>> getTasksView(final long origin) {
        return this.threadTaskMaps.get(origin).valuesView();
    }

}
