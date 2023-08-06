package com.engineersbox.conduit_v2.processing.schema.extension;

import java.io.InputStream;

public interface ExtensionSchemaPatch {

    default String schemaPatchLiteral() {
        return null;
    }

    default InputStream schemaPatchStream() {
        return null;
    }

}
