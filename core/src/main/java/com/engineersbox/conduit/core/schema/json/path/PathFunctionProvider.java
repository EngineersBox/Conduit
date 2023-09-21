package com.engineersbox.conduit.core.schema.json.path;

import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.internal.function.PathFunction;
import com.jayway.jsonpath.internal.function.PathFunctionFactory;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.concurrent.ThreadSafe;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@ThreadSafe
@SuppressWarnings("unchecked")
public class PathFunctionProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(PathFunctionProvider.class);
    private static final Map<String, Class<?>> FUNCTIONS;
    private static final ReadWriteLock RW_LOCK = new ReentrantReadWriteLock(true);
    private static final Lock READ_LOCK = RW_LOCK.readLock();
    private static final Lock WRITE_LOCK = RW_LOCK.writeLock();

    static {
        try {
            FUNCTIONS = (Map<String, Class<?>>) FieldUtils.readField(
                    PathFunctionFactory.FUNCTIONS,
                    "m",
                    true
            );
        } catch (final IllegalAccessException e) {
            throw new RuntimeException("Unable to obtain mutable inner reference to PathFunctionFactory.FUNCTIONS:", e);
        }
    }

    private PathFunctionProvider() {
        throw new UnsupportedOperationException("Static provider class");
    }

    public static synchronized void bindFunction(final String functionName,
                                                 final Class<? extends PathFunction> function) {
        WRITE_LOCK.lock();
        try {
            final Class<?> prevFunction = FUNCTIONS.put(functionName, function);
            if (prevFunction != null) {
                LOGGER.warn(
                        "Overwritten previous json-path function registration [Previous: {}] [New: {}]",
                        prevFunction.getName(),
                        function.getName()
                );
            }
        } finally {
            WRITE_LOCK.unlock();
        }
    }

    static synchronized PathFunction getFunctionInstance(final String functionName) {
        READ_LOCK.lock();
        try {
            final Class<?> function = FUNCTIONS.get(functionName);
            if (function == null) {
                return null;
            }
            try {
                return (PathFunction) ConstructorUtils.invokeConstructor(function);
            } catch (final NoSuchMethodException | IllegalAccessException | InstantiationException |
                           InvocationTargetException e) {
                throw new PathNotFoundException("Cannot instantiate custom path function: " + functionName, e);
            }
        } finally {
            READ_LOCK.unlock();
        }
    }

}
