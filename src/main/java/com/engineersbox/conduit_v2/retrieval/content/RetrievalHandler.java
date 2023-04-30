package com.engineersbox.conduit_v2.retrieval.content;

@FunctionalInterface
public interface RetrievalHandler<T> {

    Object lookup(final T key);

}
