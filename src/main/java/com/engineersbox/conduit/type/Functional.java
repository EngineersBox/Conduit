package com.engineersbox.conduit.type;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class Functional {

    public interface ThrowsFunction<T, R> {
        R apply(final T t) throws Exception;
    }

    public interface ThrowsConsumer<T> {
        void accept(final T t) throws Exception;
    }

    public interface ThrowsBiConsumer<T, U> {
        void accept(final T t, final U u) throws Exception;
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

}
