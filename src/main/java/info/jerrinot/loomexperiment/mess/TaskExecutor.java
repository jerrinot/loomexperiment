package info.jerrinot.loomexperiment.mess;

import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

import static java.util.concurrent.TimeUnit.SECONDS;

public final class TaskExecutor {
    private static final int BUSY_SPINNING_EXECUTOR_THREAD_COUNT = Runtime.getRuntime().availableProcessors();

    private final Executor scheduler;
    private final ThreadSchedulerStrategy strategy;

    public TaskExecutor(ThreadSchedulerStrategy strategy) {
        this.scheduler = createScheduler(strategy);
        this.strategy = strategy;
    }

    public void startTask(Runnable task) {
        var builder = Thread.builder().task(task);
        builder = switch (strategy) {
            case PHYSICAL_OS -> builder;
            case VIRTUAL_DEFAULT_SCHED -> builder.virtual();
            case VIRTUAL_BUSY_SPINNING_SCHED, VIRTUAL_FORK_JOIN_SCHED -> builder.virtual(scheduler);
        };
        builder.start();
    }

    private static Executor createScheduler(ThreadSchedulerStrategy threadStrategy) {
        return switch (threadStrategy) {
            case PHYSICAL_OS, VIRTUAL_DEFAULT_SCHED -> null;
            case VIRTUAL_FORK_JOIN_SCHED -> forkJoinPool();
            case VIRTUAL_BUSY_SPINNING_SCHED -> busySpinningExecutor(BUSY_SPINNING_EXECUTOR_THREAD_COUNT);
        };
    }

    private static Executor busySpinningExecutor(int threadCount) {
        var executor = new BusySpinningExecutor(threadCount);
        executor.start();
        return executor;
    }

    private static Executor forkJoinPool() {
        Thread.UncaughtExceptionHandler handler = (t, e) -> { };
        return new ForkJoinPool(4, ForkJoinPool.defaultForkJoinWorkerThreadFactory, handler, true,
                4, 4, 4, pool -> true, 60, SECONDS);
    }
}