package com.engineersbox.conduit_v2.processing.pipeline;

@FunctionalInterface
public interface ProcessingModel<T, E> {

    T submitAll(final E executor) throws Exception;

}
