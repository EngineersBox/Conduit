package com.engineersbox.conduit.type;

import java.util.function.Consumer;

public class Functional {

    @FunctionalInterface
    public interface ThrowsConsumer<T> {

        void accept(final T value) throws Exception;

    }

    public static <T> Consumer<T> uncheckedConsumer(final ThrowsConsumer<T> consumer) {
        return (final T value) -> {
            try {
                consumer.accept(value);
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

}
