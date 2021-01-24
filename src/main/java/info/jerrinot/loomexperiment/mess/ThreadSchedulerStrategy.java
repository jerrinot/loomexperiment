package info.jerrinot.loomexperiment.mess;

public enum ThreadSchedulerStrategy {
    PHYSICAL_OS,
    VIRTUAL_DEFAULT_SCHED,
    VIRTUAL_FORK_JOIN_SCHED,
    VIRTUAL_BUSY_SPINNING_SCHED
}
