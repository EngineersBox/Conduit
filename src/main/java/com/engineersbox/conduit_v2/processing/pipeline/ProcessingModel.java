package com.engineersbox.conduit_v2.processing.pipeline;

import java.util.List;

public interface ProcessingModel<T, E> {

    List<T> submitAll(final E executor);

}
