package info.jerrinot.loomexperiment.mess;

import java.util.concurrent.BlockingQueue;
import java.util.function.Consumer;

public final class BlockingQueuePump implements RunnableWithException {
    private final BlockingQueue<Long> in;
    private final BlockingQueue<Long> out;
    private final Consumer<Long> workSimulator;

    public BlockingQueuePump(BlockingQueue<Long> in, BlockingQueue<Long> out, Consumer<Long> workSimulator) {
        this.in = in;
        this.out = out;
        this.workSimulator = workSimulator;
    }

    @Override
    public void run() throws Exception {
        for (;;) {
            Long item = in.take();
            workSimulator.accept(item);
            out.put(item);
        }
    }
}
