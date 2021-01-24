package info.jerrinot.loomexperiment;

import java.lang.reflect.Array;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public final class Utils {

    private Utils() {
    }

    public static  <T> T[] supplyForArray(Supplier<T> supplier, int length) {
        Class<T> component = (Class<T>) supplier.get().getClass();
        T[] arr = (T[]) Array.newInstance(component, length);
        for (int i = 0; i < length; i++) {
            arr[i] = supplier.get();
        }
        return arr;
    }

    public static void doSomeWork(long intensity) {
        long deadLine = System.nanoTime() + TimeUnit.MICROSECONDS.toNanos(intensity);
        while (System.nanoTime() < deadLine) {}
    }

}
