package com.engineersbox.conduit.core.retrieval.content;

@FunctionalInterface
public interface RetrievalHandler<T> {

    Object lookup(final T key);

}
