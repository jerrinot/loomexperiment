package info.jerrinot.loomexperiment;

import java.util.concurrent.BlockingQueue;

class BlockingQueuePump implements TaskExecutor.RunnableWithException {
    private final BlockingQueue<Long> in;
    private final BlockingQueue<Long> out;

    BlockingQueuePump(BlockingQueue<Long> in, BlockingQueue<Long> out) {
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
