package info.jerrinot.loomexperiment.mess;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public enum WorkSimulator implements Consumer<Long> {
    DEADLINE(WorkSimulator::deadline),
    NOOP(WorkSimulator::noop);

    private final Consumer<Long> strategy;

    WorkSimulator(Consumer<Long> strategy) {
        this.strategy = strategy;
    }

    @Override
    public void accept(Long item) {
        strategy.accept(item);
    }

    private static void deadline(long intensity) {
        long deadLine = System.nanoTime() + TimeUnit.MICROSECONDS.toNanos(intensity);
        while (System.nanoTime() < deadLine) {}
    }

    private static void noop(long intensity) {
    }
}
