package info.jerrinot.loomexperiment;

import org.jctools.queues.MpmcUnboundedXaddArrayQueue;

import java.util.Queue;
import java.util.concurrent.Executor;

class BusySpinningExecutor implements Executor {
    private final int threadCount;
    private Queue<Runnable> queue = new MpmcUnboundedXaddArrayQueue<>(64);

    public BusySpinningExecutor(int threadCount) {
        this.threadCount = threadCount;
    }

    @Override
    public void execute(Runnable command) {
        while (!queue.offer(command)) {
        }
    }

    public void start() {
        for (int i = 0; i < threadCount; i++) {
            Thread.builder().daemon(true).task(() -> {
                for (; ; ) {
                    Runnable task;
                    do {
                        task = queue.poll();
                    } while (task == null);

                    try {
                        task.run();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }


}
