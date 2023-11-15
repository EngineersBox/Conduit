package com.engineersbox.conduit.core.util.threading;

import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

@ThreadSafe
public class ScopedThreadLocal<T, P> {

    private final ThreadLocal<T> local;
    private final AtomicReference<T> global;
    private final Predicate<P> globalPredicate;

    public ScopedThreadLocal(final Predicate<P> globalPredicate) {
        this.local = new ThreadLocal<>();
        this.global = new AtomicReference<>(null);
        this.globalPredicate = globalPredicate;
    }

    public ScopedThreadLocal() {
        this((final P _p) -> true);
    }

    public void setLocal(final T value) {
        this.local.set(value);
    }

    public T getLocal() {
        return this.local.get();
    }

    public void setGlobal(final T value) {
        this.global.set(value);
    }

    public T getGlobal() {
        return this.global.get();
    }

    public void set(final T value,
                    final P conditionValue) {
        if (this.globalPredicate.test(conditionValue)) {
            setGlobal(value);
        } else {
            setLocal(value);
        }
    }

    public T get(final P conditionValue) {
        if (this.globalPredicate.test(conditionValue)) {
            return getGlobal();
        }
        return getLocal();
    }

}
