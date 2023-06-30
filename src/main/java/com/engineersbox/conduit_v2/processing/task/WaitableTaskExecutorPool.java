package com.engineersbox.conduit_v2.processing.task;

import com.engineersbox.conduit_v2.processing.task.worker.ClientBoundWorkerTask;
import com.engineersbox.conduit_v2.processing.task.worker.DroppingClientBoundWorkerTask;
import io.riemann.riemann.client.RiemannClient;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.list.mutable.FastList;

import java.util.concurrent.ForkJoinTask;
import java.util.function.Supplier;

public class WaitableTaskExecutorPool extends TaskExecutorPool {

    private final MutableList<ForkJoinTask<?>> tasks;
    // TODO: Support a pool of clients to be usable in this executor pool
    //       A thread should pull from the client pool based on some condition
    //       provided as a predicate for allowing it to be selected (e.g. count
    //       incremented. Once the thread is finished with the client it is
    //       returned to the pool and the condition state is updated (e.g.
    //       count decremented).

    public WaitableTaskExecutorPool(final Supplier<RiemannClient> clientProvider,
                                    final int parallelism) {
        super(clientProvider, parallelism);
        this.tasks = FastList.newList(parallelism);
    }

    @Override
    public ForkJoinTask<?> submit(final ClientBoundWorkerTask task) {
        final Mutable<ForkJoinTask<?>> taskReference = new MutableObject<>();
        final ForkJoinTask<?> forkJoinTask = super.submit(new DroppingClientBoundWorkerTask<>(
                task,
                taskReference,
                this.tasks
        ));
        taskReference.setValue(forkJoinTask);
        this.tasks.add(forkJoinTask);
        return forkJoinTask;
    }

    public void resettingBarrier() {
        this.tasks.forEach(ForkJoinTask::quietlyJoin);
        this.tasks.clear();
    }

    public MutableList<? super ForkJoinTask<?>> getTasksView() {
        return this.tasks.asUnmodifiable();
    }

}
