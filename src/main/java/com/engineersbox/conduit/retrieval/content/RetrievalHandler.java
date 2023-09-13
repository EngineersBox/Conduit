package com.engineersbox.conduit.retrieval.content;

@FunctionalInterface
public interface RetrievalHandler<T> {

    Object lookup(final T key);

}
