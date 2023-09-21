package com.engineersbox.conduit.schemamerger;

import java.util.List;

public class JsonSchemaConstants {

    public static final String SUB_SCHEMAS_FIELD_NAME = "$subSchemas";
    static final String REF_FIELD_NAME = "$ref";
    static final String DEFS_FIELD_NAME = "$defs";
    static final String TITLE_FIELD_NAME = "title";
    static final String ROOT_REF_PREFIX = "#/";
    static final String CLASSPATH_PREFIX = "classpath:";
    static final List<String> HEADER_FIELDS = List.of(
            "$schema",
            "$id"
    );

    private JsonSchemaConstants() {
        throw new UnsupportedOperationException("Static utility class");
    }

}
