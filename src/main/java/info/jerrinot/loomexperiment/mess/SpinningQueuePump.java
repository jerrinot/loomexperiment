package info.jerrinot.loomexperiment.mess;

import java.util.Queue;

public final class SpinningQueuePump implements Runnable {
    private final Queue<Long> in;
    private final Queue<Long> out;

    public SpinningQueuePump(Queue<Long> in, Queue<Long> out) {
        this.in = in;
        this.out = out;
    }

    @Override
    public void run() {
        for (;;) {
            Long item;
            do {
               item = in.poll();
            } while (item == null);

            Utils.doSomeWork(item);

            while (!out.offer(item)) { /*intentionally noop*/ };
        }
    }
}
