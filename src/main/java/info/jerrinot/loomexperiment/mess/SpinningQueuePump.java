package info.jerrinot.loomexperiment.mess;

import java.util.Queue;
import java.util.function.Consumer;

public final class SpinningQueuePump implements Runnable {
    private final Queue<Long> in;
    private final Queue<Long> out;
    private final Consumer<Long> workSimulator;

    public SpinningQueuePump(Queue<Long> in, Queue<Long> out, Consumer<Long> workSimulator) {
        this.in = in;
        this.out = out;
        this.workSimulator = workSimulator;
    }

    @Override
    public void run() {
        for (;;) {
            Long item;
            do {
               item = in.poll();
            } while (item == null);

            workSimulator.accept(item);

            while (!out.offer(item)) { /*intentionally noop*/ };
        }
    }
}
