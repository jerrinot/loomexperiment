package info.jerrinot.loomexperiment.mess;

import java.util.concurrent.BlockingQueue;

public final class BlockingQueuePump implements RunnableWithException {
    private final BlockingQueue<Long> in;
    private final BlockingQueue<Long> out;

    public BlockingQueuePump(BlockingQueue<Long> in, BlockingQueue<Long> out) {
        this.in = in;
        this.out = out;
    }

    @Override
    public void run() throws Exception {
        for (;;) {
            Long item = in.take();
            Utils.doSomeWork(item);
            out.put(item);
        }
    }
}
