package info.jerrinot.loomexperiment.mess;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public final class AtomicLongCounter implements Counter {
    private static final boolean DEBUG_QUEUE_DEPTH = false;

    private final AtomicLong counter = new AtomicLong();
    private final StringBuilder sb = new StringBuilder();

    private long counterBefore = 0;
    private long timeBefore = System.nanoTime();

    @Override
    public void inc() {
        counter.incrementAndGet();
    }

    @Override
    public long get() {
        return counter.get();
    }

    // not thread safe
    public void printStats(BlockingQueue<?>[] queues) {
        long countersNow = get();
        long countersDelta = countersNow - counterBefore;
        long timeNow = System.nanoTime();
        long timeDeltaNanos = timeNow - timeBefore;
        long opsPerMillis = countersDelta / (TimeUnit.NANOSECONDS.toMillis(timeDeltaNanos));

        sb.append("Ops per ms: ").append(opsPerMillis).append('\n');
        if (DEBUG_QUEUE_DEPTH) {
            for (int i = 0; i < queues.length; i++) {
                sb.append('q').append(i).append(" size: ").append(queues[i].size()).append('\n');
            }
            sb.append('\n');
        }
        System.out.println(sb);
        sb.setLength(0);

        counterBefore = countersNow;
        timeBefore = timeNow;
    }
}
