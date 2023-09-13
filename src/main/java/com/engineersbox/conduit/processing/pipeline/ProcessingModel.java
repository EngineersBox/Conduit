package com.engineersbox.conduit.processing.pipeline;

@FunctionalInterface
public interface ProcessingModel<T, E> {

    T submitAll(final E executor) throws Exception;

}
