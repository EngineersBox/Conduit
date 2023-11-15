package com.engineersbox.conduit.core.util;

import java.util.concurrent.Future;

public class FutureUtils {

    private FutureUtils() {
        throw new UnsupportedOperationException("Static utility class");
    }

    public static void waitForDoneOrCancelled(final Future<?> future) {
        while (!future.isDone() && !future.isCancelled());
    }

}
