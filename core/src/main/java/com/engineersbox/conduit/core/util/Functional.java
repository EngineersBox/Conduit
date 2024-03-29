package com.engineersbox.conduit.core.util;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class Functional {

    public interface ThrowsRunnable {
        void run() throws Exception;
    }

    public interface ThrowsFunction<T, R> {
        R apply(final T t) throws Exception;
    }

    public interface ThrowsSupplier<T> {
        T get() throws Exception;
    }

    public interface ThrowsConsumer<T> {
        void accept(final T t) throws Exception;
    }

    public interface ThrowsBiConsumer<T, U> {
        void accept(final T t, final U u) throws Exception;
    }

    public static Runnable uncheckedRunnable(final ThrowsRunnable runnable) {
        return () -> {
            try {
                runnable.run();
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    public static <T, R> Function<T, R> uncheckedFunction(final ThrowsFunction<T, R> function) {
        return (final T t) -> {
            try {
                return function.apply(t);
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    public static <T> Supplier<T> uncheckedSupplier(final ThrowsSupplier<T> supplier) {
        return () -> {
            try {
                return supplier.get();
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    public static <T> Consumer<T> uncheckedConsumer(final ThrowsConsumer<T> consumer) {
        return (final T t) -> {
            try {
                consumer.accept(t);
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    static <T, U> BiConsumer<T, U> uncheckedBiConsumer(final ThrowsBiConsumer<T, U> consumer) {
        return (final T t, final U u) -> {
            try {
                consumer.accept(t, u);
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    public static <T> void checkedApply(final Consumer<T> method,
                                        final Functional.ThrowsSupplier<T> supplier) throws Exception {
        if (supplier == null) {
            return;
        }
        checkedApply(method, supplier.get());
    }

    public static <T> void checkedApply(final Consumer<T> method,
                                        final T value) {
        if (value == null) {
            return;
        }
        method.accept(value);
    }

    public static <T,R> R checkedApply(final Supplier<T> supplier,
                                       final Function<T, R> function) {
        if (supplier == null) {
            return null;
        }
        final T value = supplier.get();
        if (value == null) {
            return null;
        }
        return function.apply(value);
    }

    public static <T,R> R checkedApply(final Supplier<T> supplier,
                                       final Function<T, R> function,
                                       final R defaultValue) {
        return Objects.requireNonNullElse(
                checkedApply(
                        supplier,
                        function
                ),
                defaultValue
        );
    }

}