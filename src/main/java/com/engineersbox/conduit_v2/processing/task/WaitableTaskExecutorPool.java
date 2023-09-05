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
import org.jeasy.batch.core.job.JobReport;

import java.util.List;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.Future;

public class WaitableTaskExecutorPool extends TaskExecutorPool {

    private final MutableMap<Long, MutableMap<Integer, ForkJoinTask<List<Future<JobReport>>>>> threadTaskMaps;

    public WaitableTaskExecutorPool(final ClientPool clientProvider,
                                    final JobExecutorPool jobExecutorPool,
                                    final int parallelism) {
        super(clientProvider, jobExecutorPool, parallelism);
        this.threadTaskMaps = Maps.mutable.withInitialCapacity(parallelism);
    }

    @Override
    public ForkJoinTask<List<Future<JobReport>>> submit(final ClientBoundWorkerTask task) {
        return submit(
                task,
                Thread.currentThread().threadId()
        );
    }

    public ForkJoinTask<List<Future<JobReport>>> submit(final ClientBoundWorkerTask task,
                                  final long origin) {
        final MutableMap<Integer, ForkJoinTask<List<Future<JobReport>>>> taskMap = this.threadTaskMaps.computeIfAbsent(
                origin,
                _origin -> Maps.mutable.empty()
        );
        final Mutable<ForkJoinTask<List<Future<JobReport>>>> taskReference = new MutableObject<>();
        final ForkJoinTask<List<Future<JobReport>>> forkJoinTask = super.submit(new DroppingClientBoundWorkerTask<>(
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
        final MutableMap<Integer, ForkJoinTask<List<Future<JobReport>>>> taskMap = this.threadTaskMaps.get(origin);
        taskMap.forEachValue(ForkJoinTask::quietlyJoin);
        taskMap.clear();
    }

    public RichIterable<? super ForkJoinTask<List<Future<JobReport>>>> getTasksView() {
        return getTasksView(Thread.currentThread().threadId());
    }

    public RichIterable<? super ForkJoinTask<List<Future<JobReport>>>> getTasksView(final long origin) {
        return this.threadTaskMaps.get(origin).valuesView();
    }

}
