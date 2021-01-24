package info.jerrinot.loomexperiment;

import info.jerrinot.loomexperiment.mess.AtomicLongCounter;
import info.jerrinot.loomexperiment.mess.BlockingQueuePump;
import info.jerrinot.loomexperiment.mess.SpinningQueuePump;
import info.jerrinot.loomexperiment.mess.TaskExecutor;
import info.jerrinot.loomexperiment.mess.ThreadSchedulerStrategy;
import info.jerrinot.loomexperiment.mess.Utils;
import info.jerrinot.loomexperiment.mess.WorkSimulator;
import org.junit.Test;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.function.Supplier;

import static info.jerrinot.loomexperiment.mess.ThreadSchedulerStrategy.PHYSICAL_OS;
import static info.jerrinot.loomexperiment.mess.ThreadSchedulerStrategy.VIRTUAL_BUSY_SPINNING_SCHED;
import static info.jerrinot.loomexperiment.mess.Utils.wrapException;
import static info.jerrinot.loomexperiment.mess.WorkSimulator.NOOP;

/**
 * Simulating DataFlow processing in queue connected to a ring: QUEUE-1 -> QUEUE-2 -> ... -> QUEUE-N -> QUEUE-1
 * Counting number of items who made it through the full ring
 */
public final class RingExperiment {
    private static final int PIPELINE_LENGTH = 5;       // how many queues in total
    private static final int QUEUE_CAPACITY = 1000;        // capacity of each queue
    private static final int WORKERS_PER_STAGE = 1;     // how many workers per each stage
    private static final Long WORK_ITEM = 1L;           // the item to be send through
    private static final ThreadSchedulerStrategy THREAD_SCHEDULER = PHYSICAL_OS;
    private static final WorkSimulator WORK_SIMULATOR = NOOP;

    @Test
    public void ring_blocking_queues() throws InterruptedException {
        validateParameters();

        Supplier<BlockingQueue<Long>> queueSupplier = () -> new ArrayBlockingQueue<>(QUEUE_CAPACITY);
        var queues = Utils.supplyToArray(queueSupplier, PIPELINE_LENGTH);
        TaskExecutor taskExecutor = new TaskExecutor(THREAD_SCHEDULER);

        // inject item to the first queue
        queues[0].put(WORK_ITEM);

        // pass items through stages
        for (int i = 0; i < PIPELINE_LENGTH - 1; i++) {
            BlockingQueue<Long> src = queues[i];
            BlockingQueue<Long> dst = queues[i+1];
            for (int w = 0; w < WORKERS_PER_STAGE; w++) {
                taskExecutor.startTask(wrapException(new BlockingQueuePump(src, dst, WORK_SIMULATOR)));
            }
        }

        // pass item from the last stage back to the first stage and increase counter
        var counter = new AtomicLongCounter();
        taskExecutor.startTask(wrapException(() -> {
            BlockingQueue<Long> firstQueue = queues[0];
            BlockingQueue<Long> lastQueue = queues[PIPELINE_LENGTH - 1];
            for (;;) {
                long item = lastQueue.take();
                WORK_SIMULATOR.accept(item);
                firstQueue.put(item);
                counter.inc();
            }
        }));

        // dump perf data into sysout
        for (;;) {
            Thread.sleep(1000);
            counter.printStats(queues);
        }
    }

    private void validateParameters() {
        if (PIPELINE_LENGTH < 2) {
            throw new IllegalStateException("You need at least 2 queues to create a ring");
        }
    }
}
