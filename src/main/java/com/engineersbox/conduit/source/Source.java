package com.engineersbox.conduit.source;

public abstract class Source {

    private final SourceType type;

    public Source(final SourceType type) {
        this.type = type;
    }

    public SourceType getType() {
        return this.type;
    }

    abstract public String invoke();

}
