package com.engineersbox.conduit.core.processing.pipeline.threading;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class Indicator {

    public final AtomicBoolean end = new AtomicBoolean(false);
    public final AtomicReference<Thread> ref = new AtomicReference<>(null);

}
