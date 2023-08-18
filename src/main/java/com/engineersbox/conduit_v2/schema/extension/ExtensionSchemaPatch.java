package com.engineersbox.conduit_v2.schema.extension;

import java.io.InputStream;

public interface ExtensionSchemaPatch {

    default String schemaLiteral() {
        return null;
    }

    default InputStream schemaStream() {
        return null;
    }

    default String schemaPatchLiteral() {
        return null;
    }

    default InputStream schemaPatchStream() {
        return null;
    }

}
