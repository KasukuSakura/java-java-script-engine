package io.github.kasukusakura.jjse.test;

import org.opentest4j.AssertionFailedError;

public class Testing {
    public interface UncheckedCode {
        void run() throws Throwable;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Throwable> T assertFailed(Class<T> excepted, UncheckedCode code) {
        try {
            code.run();
        } catch (Throwable throwable) {
            if (excepted.isInstance(throwable)) return (T) throwable;
            throw new AssertionFailedError("Thrown not match: except " + excepted.getName() + " but got " + throwable.getClass().getName(), throwable);
        }
        throw new AssertionFailedError("Code not thrown");
    }
}
