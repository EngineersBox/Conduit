package com.engineersbox.conduit.compile.schema;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class ResourceRef {
    ObjectNode refParent;
    String ref;
    ObjectNode schema;
    String title;

    public ResourceRef(final ObjectNode refParent,
                       final String ref,
                       final ObjectNode schema) {
        this.refParent = refParent;
        this.ref = ref;
        this.schema = schema;
        title = "";
    }

    public ResourceRef setRefParent(final ObjectNode refParent) {
        this.refParent = refParent;
        return this;
    }

    public ResourceRef setRef(final String ref) {
        this.ref = ref;
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
