package info.jerrinot.loomexperiment;

import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

import static java.util.concurrent.TimeUnit.SECONDS;

public final class TaskExecutor {
    private static final int BUSY_SPINNING_EXECUTOR_THREAD_COUNT = Runtime.getRuntime().availableProcessors();

    private final Executor scheduler;
    private final Strategy strategy;

    public enum Strategy {
        PHYSICAL_OS,
        VIRTUAL_DEFAULT_SCHED,
        VIRTUAL_FORK_JOIN_SCHED,
        VIRTUAL_BUSY_SPINNING_SCHED
    }

    public interface RunnableWithException  {
        void run() throws Exception;
    }

    public TaskExecutor(Strategy strategy) {
        this.scheduler = createScheduler(strategy);
        this.strategy = strategy;
    }

    private static Executor createScheduler(Strategy threadStrategy) {
        return switch (threadStrategy) {
            case PHYSICAL_OS, VIRTUAL_DEFAULT_SCHED -> null;
            case VIRTUAL_FORK_JOIN_SCHED -> forkJoinPool();
            case VIRTUAL_BUSY_SPINNING_SCHED -> busySpinningExecutor(BUSY_SPINNING_EXECUTOR_THREAD_COUNT);
        };
    }

    void startTask(RunnableWithException task) {
        Runnable r = withWrappedExceptions(task);
        var builder = Thread.builder().task(r);
        builder = switch (strategy) {
            case PHYSICAL_OS -> builder;
            case VIRTUAL_DEFAULT_SCHED -> builder.virtual();
            case VIRTUAL_BUSY_SPINNING_SCHED, VIRTUAL_FORK_JOIN_SCHED -> builder.virtual(scheduler);
        };
        builder.start();
    }

    private static Runnable withWrappedExceptions(RunnableWithException task) {
        return () -> {
            try {
                task.run();
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    public static Executor busySpinningExecutor(int threadCount) {
        var executor = new BusySpinningExecutor(threadCount);
        executor.start();
        return executor;
    }

    public static Executor forkJoinPool() {
        Thread.UncaughtExceptionHandler handler = (t, e) -> { };
        return new ForkJoinPool(4, ForkJoinPool.defaultForkJoinWorkerThreadFactory, handler, true,
                4, 4, 4, pool -> true, 60, SECONDS);
    }
}