package info.jerrinot.loomexperiment.mess;

import java.util.Queue;
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
    public void printStats(Queue<?>[] queues) {
        long countersNow = get();
        long countersDelta = countersNow - counterBefore;
        long timeNow = System.nanoTime();
        long timeDeltaNanos = timeNow - timeBefore;
        float opsPerMillis = ((float)countersDelta) / (TimeUnit.NANOSECONDS.toMillis(timeDeltaNanos));
        long microsPerOps = TimeUnit.NANOSECONDS.toMicros(timeDeltaNanos / countersDelta);

        sb.append("Throughput: ").append(opsPerMillis)
                .append(" ops / ms = 1 operation is finished every ")
                .append(microsPerOps)
                .append(" μs");
        if (DEBUG_QUEUE_DEPTH) {
            for (int i = 0; i < queues.length; i++) {
                sb.append("\nq").append(i).append(" size: ").append(queues[i].size());
            }
            sb.append('\n');
        }
        System.out.println(sb);
        sb.setLength(0);

        counterBefore = countersNow;
        timeBefore = timeNow;
    }
}
