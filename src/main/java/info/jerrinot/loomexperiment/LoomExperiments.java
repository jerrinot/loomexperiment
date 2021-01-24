package info.jerrinot.loomexperiment;

import info.jerrinot.loomexperiment.mess.AtomicLongCounter;
import info.jerrinot.loomexperiment.mess.BlockingQueuePump;
import info.jerrinot.loomexperiment.mess.TaskExecutor;
import info.jerrinot.loomexperiment.mess.ThreadSchedulerStrategy;
import info.jerrinot.loomexperiment.mess.Utils;
import org.junit.Test;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.function.Supplier;

import static info.jerrinot.loomexperiment.mess.ThreadSchedulerStrategy.*;
import static info.jerrinot.loomexperiment.mess.Utils.wrapException;

public final class LoomExperiments {
    private static final int PIPELINE_DEPTH = 10;       // how many queues in total
    private static final int QUEUE_CAPACITY = 1;        // capacity of each queue
    private static final int WORKERS_PER_STAGE = 1;     // how many workers per each stage
    private static final Long WORK_ITEM = 1L;           // the item to be send through
    private static final ThreadSchedulerStrategy THREAD_SCHEDULER = PHYSICAL_OS;

    @Test
    public void dataFlow() throws InterruptedException {
        // Simulating DataFlow processing, ie. PRODUCER -> QUEUE-1 -> QUEUE-2 -> ... -> QUEUE-N -> CONSUMER

        Supplier<BlockingQueue<Long>> queueSupplier = () -> new ArrayBlockingQueue<>(QUEUE_CAPACITY);
        var queues = Utils.supplyToArray(queueSupplier, PIPELINE_DEPTH);
        TaskExecutor taskExecutor = new TaskExecutor(THREAD_SCHEDULER);

        // keep inserting item(s) into the first queue
        taskExecutor.startTask(wrapException(() -> {
            for (;;) {
                queues[0].put(WORK_ITEM);
            }
        }));

        // pass items through stages
        for (int i = 0; i < PIPELINE_DEPTH - 1; i++) {
            BlockingQueue<Long> src = queues[i];
            BlockingQueue<Long> dst = queues[i+1];
            for (int w = 0; w < WORKERS_PER_STAGE; w++) {
                taskExecutor.startTask(wrapException(new BlockingQueuePump(src, dst)));
            }
        }

        // remove items from the last queue and count them
        var counter = new AtomicLongCounter();
        taskExecutor.startTask(wrapException(() -> {
            for (; ; ) {
                long item = queues[PIPELINE_DEPTH - 1].take();
                Utils.doSomeWork(item);
                counter.inc();
            }
        }));

        // dump perf data into sysout
        for (;;) {
            Thread.sleep(1000);
            counter.printStats(queues);
        }
    }
}
