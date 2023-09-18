package com.engineersbox.conduit.compile;

public class SchemaMerger {

    private static final String METRICS_SCHEMA_NAME = "metrics.schema.json";
    private static final String UNIFIED_SCHEMA_NAME = "unified.schema.json";

    public static void main(final String[] args) {
        // 1. Read initial schema
        // 2. Create "subSchemas" field
        // 3. Find refs with "classpath:" qualifiers
        // 4. For each resolved ref, read the ref'd schema (Thread...getResource(...))
        // 5. Strip header info ($schema, $id)
        // 6. Read sub-schema "title" field and transform to lower snake case
        // 7. Rewrite use of "#/$defs/<def>" to refer to "#/subSchemas/<LSC title>/<def>"
        // 8. Create entry in "subSchemas" field based on transformed title field
        // 9. Write transformed sub-schema into created field
        // 10. Once all refs are resolved, write unified schema to file
    }

}
