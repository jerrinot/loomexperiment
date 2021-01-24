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

import static info.jerrinot.loomexperiment.mess.ThreadSchedulerStrategy.*;
import static info.jerrinot.loomexperiment.mess.WorkSimulator.*;
import static info.jerrinot.loomexperiment.mess.Utils.wrapException;

/**
 * Simulating DataFlow processing: PRODUCER -> QUEUE-1 -> QUEUE-2 -> ... -> QUEUE-N -> CONSUMER
 * Counting number of consumed items
 */
public final class DataFlowExperiment {
    private static final int PIPELINE_LENGTH = 10;       // how many queues in total
    private static final int QUEUE_CAPACITY = 1;        // capacity of each queue
    private static final int WORKERS_PER_STAGE = 1;     // how many workers per each stage
    private static final Long WORK_ITEM = 1L;           // the item to be send through
    private static final ThreadSchedulerStrategy THREAD_SCHEDULER = PHYSICAL_OS;
    private static final WorkSimulator WORK_SIMULATOR = NOOP;

    @Test
    public void dataFlow_blocking_queues() throws InterruptedException {
        Supplier<BlockingQueue<Long>> queueSupplier = () -> new ArrayBlockingQueue<>(QUEUE_CAPACITY);
        var queues = Utils.supplyToArray(queueSupplier, PIPELINE_LENGTH);
        TaskExecutor taskExecutor = new TaskExecutor(THREAD_SCHEDULER);

        // keep inserting item(s) into the first queue
        taskExecutor.startTask(wrapException(() -> {
            for (;;) {
                queues[0].put(WORK_ITEM);
            }
        }));

        // pass items through stages
        for (int i = 0; i < PIPELINE_LENGTH - 1; i++) {
            BlockingQueue<Long> src = queues[i];
            BlockingQueue<Long> dst = queues[i+1];
            for (int w = 0; w < WORKERS_PER_STAGE; w++) {
                taskExecutor.startTask(wrapException(new BlockingQueuePump(src, dst, WORK_SIMULATOR)));
            }
        }

        // remove items from the last queue and count them
        var counter = new AtomicLongCounter();
        taskExecutor.startTask(wrapException(() -> {
            BlockingQueue<Long> lastQueue = queues[PIPELINE_LENGTH - 1];
            for (;;) {
                long item = lastQueue.take();
                WORK_SIMULATOR.accept(item);
                counter.inc();
            }
        }));

        // dump perf data into sysout
        for (;;) {
            Thread.sleep(1000);
            counter.printStats(queues);
        }
    }

    // probably broken
    // needs review
//    @Test
//    public void dataFlow_spinning() throws InterruptedException {
//        // Simulating DataFlow processing, ie. PRODUCER -> QUEUE-1 -> QUEUE-2 -> ... -> QUEUE-N -> CONSUMER
//
//        Supplier<Queue<Long>> queueSupplier = () -> new ArrayBlockingQueue<>(QUEUE_CAPACITY);
//        var queues = Utils.supplyToArray(queueSupplier, PIPELINE_LENGTH);
//        TaskExecutor taskExecutor = new TaskExecutor(THREAD_SCHEDULER);
//
//        // keep inserting item(s) into the first queue
//        taskExecutor.startTask(wrapException(() -> {
//            for (;;) {
//                queues[0].offer(WORK_ITEM);
//            }
//        }));
//
//        // pass items through stages
//        for (int i = 0; i < PIPELINE_LENGTH - 1; i++) {
//            Queue<Long> src = queues[i];
//            Queue<Long> dst = queues[i+1];
//            for (int w = 0; w < WORKERS_PER_STAGE; w++) {
//                taskExecutor.startTask(new SpinningQueuePump(src, dst, WORK_SIMULATOR));
//            }
//        }
//
//        // remove items from the last queue and count them
//        var counter = new AtomicLongCounter();
//        taskExecutor.startTask(wrapException(() -> {
//            Queue<Long> lastQueue = queues[PIPELINE_LENGTH - 1];
//            for (;;) {
//                Long item;
//                do {
//                    item = lastQueue.poll();
//                } while (item == null);
//                WORK_SIMULATOR.accept(item);
//                counter.inc();
//            }
//        }));
//
//        // dump perf data into sysout
//        for (;;) {
//            Thread.sleep(1000);
//            counter.printStats(queues);
//        }
//    }
}
