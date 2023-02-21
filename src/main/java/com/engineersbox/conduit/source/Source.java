package com.engineersbox.conduit.source;

public class Source {

    private final SourceType type;

    public Source(final SourceType type) {
        this.type = type;
    }

    public SourceType getType() {
        return this.type;
    }

}
