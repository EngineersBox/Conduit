package com.engineersbox.conduit.schemamerger;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class ResourceRef {
    ObjectNode refParent;
    String prefix;
    ObjectNode schema;
    String title;

    public ResourceRef(final ObjectNode refParent,
                       final ObjectNode schema) {
        this.refParent = refParent;
        this.schema = schema;
        title = "";
    }

    public ResourceRef setRefParent(final ObjectNode refParent) {
        this.refParent = refParent;
        return this;
    }

    public ResourceRef setPrefix(final String prefix) {
        this.prefix = prefix;
        return this;
    }

    public ResourceRef setSchema(final ObjectNode schema) {
        this.schema = schema;
        return this;
    }

    public ResourceRef setTitle(final String title) {
        this.title = title;
        return this;
    }

}
