package com.engineersbox.conduit.compile.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;

public class SchemaMerger {

    private static final Logger LOGGER = LoggerFactory.getLogger(SchemaMerger.class);
    private static final String METRICS_SCHEMA_PATH = "schemas/metrics.schema.json";
    public static final String UNIFIED_SCHEMA_PATH = "/Users/jackkilrain/Desktop/Projects/Java/Conduit/src/main/resources/schemas/unified.schema.json";

    public static void main(final String[] args) throws IOException {
        // 1. Read initial schema
        // 2. Create "subSchemas" field
        // 3. Find refs with "classpath:" qualifiers
        // 4. For each resolved ref, read the ref'd schema (Thread...getResource(...))
        // 5. Strip header info ($schema, $id)
        // 6. Read sub-schema "title" field and transform to lower snake case
        // 7. Rewrite use of "#/$defs/<def>" to refer to "#/subSchemas/<LSC title>/$defs/<def>"
        // 8. Recursively invoke this process for the ref'd schema with an appended nested path prefix
        // 9. Create entry in "subSchemas" field based on transformed title field
        // 10. Write transformed sub-schema into created field
        // 11. Once all refs are resolved, write unified schema to file
        final SchemaMerger merger = new SchemaMerger(METRICS_SCHEMA_PATH);
        merger.registerTransformer(new EnumRefTransformer());
        merger.merge();
        merger.writeMerged(UNIFIED_SCHEMA_PATH);
    }

    private static final ObjectMapper MAPPER = new ObjectMapper();
    public static final String SUB_SCHEMAS_FIELD_NAME = "subSchemas";
    private static final String REF_FIELD_NAME = "$ref";
    private static final String TITLE_FIELD_NAME = "title";
    private static final String ROOT_REF_PREFIX = "#/";
    private static final String CLASSPATH_PREFIX = "classpath:";
    private static final List<String> HEADER_FIELDS = List.of(
            "$schema",
            "$id"
    );

    private ObjectNode mainSchema;
    private ObjectNode subSchemas;
    private String nestedSchemaPrefix;
    private final MutableList<SchemaTransformer> transformers;

    private SchemaMerger(final String resourcePath) {
        this.mainSchema = readResource(resourcePath);
        this.nestedSchemaPrefix = ROOT_REF_PREFIX + SUB_SCHEMAS_FIELD_NAME;
        this.transformers = Lists.mutable.empty();
    }

    private SchemaMerger(final ObjectNode mainSchema,
                         final String nestedSchemaPrefix) {
        this.mainSchema = mainSchema;
        this.nestedSchemaPrefix = nestedSchemaPrefix;
        this.transformers = Lists.mutable.empty();
    }

    public void registerTransformer(final SchemaTransformer transformer) {
        this.transformers.add(transformer);
    }

    public void registerTransformers(final Collection<SchemaTransformer> transformers) {
        this.transformers.addAll(transformers);
    }

    private ObjectNode readResource(final String resourcePath) {
        String path = resourcePath.startsWith(CLASSPATH_PREFIX)
                ? StringUtils.stripStart(resourcePath, CLASSPATH_PREFIX)
                : resourcePath;
        path = path.startsWith("/")
                ? StringUtils.stripStart(path, "/")
                : path;
        LOGGER.info("Reading resource: {}", path);
        try (final InputStream stream = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(path)) {
            final JsonNode node = MAPPER.readTree(stream);
            if (node == null) {
                throw new IllegalStateException("Unable to read " + path + " as JSON");
            } else if (node instanceof ObjectNode objectNode) {
                return objectNode;
            }
            throw new IllegalStateException("Schema was an array of objects, invalid schema format");
        } catch (final IOException e) {
            throw new RuntimeException("Unable to read resource as JSON from [" + path + "]", e);
        }
    }

    private void applyTransformers() {
        this.mainSchema = this.transformers.injectInto(
                this.mainSchema,
                (final ObjectNode schema, final SchemaTransformer transformer) ->
                        transformer.transform(schema)
        );
        LOGGER.info(
                "Applied {} schema transformers: [{}]",
                this.transformers.size(),
                this.transformers.collect(Object::getClass)
                        .collect(Class::getSimpleName)
                        .makeString(",")
        );
    }

    private List<ObjectNode> findResourceRefParents() {
        final List<JsonNode> refFieldParents = this.mainSchema.findParents(REF_FIELD_NAME);
        LOGGER.info("Found {} ref fields", refFieldParents.size());
        final List<ObjectNode> resourceRefFieldParents = refFieldParents.stream()
                .filter((final JsonNode node) -> {
                    final JsonNode ref = node.get(REF_FIELD_NAME);
                    return ref.isTextual() && StringUtils.startsWith(ref.asText(), CLASSPATH_PREFIX);
                }).filter(JsonNode::isObject)
                .map((final JsonNode node) -> (ObjectNode) node)
                .toList();
        LOGGER.info("Filtered ref fields to {} resource refs", resourceRefFieldParents.size());
        return resourceRefFieldParents;
    }

    private ResourceRef resolveRef(final ObjectNode refParent) {
        final JsonNode refNode = refParent.get(REF_FIELD_NAME);
        final String ref = refNode.asText();
        final ObjectNode schema = readResource(StringUtils.substringAfter(
                ref,
                CLASSPATH_PREFIX
        ));
        LOGGER.info("Resolved ref {}", ref);
        return new ResourceRef(refParent, ref, schema);
    }

    private ResourceRef stripHeaders(final ResourceRef resourceRef) {
        resourceRef.schema.remove(HEADER_FIELDS);
        LOGGER.info("[SCHEMA: {}] Stripped header fields", resourceRef.ref);
        return resourceRef;
    }

    private ResourceRef transformTitle(final ResourceRef resourceRef) {
        final JsonNode title = resourceRef.schema.get(TITLE_FIELD_NAME);
        if (title == null || title.isNull() || title.isMissingNode()) {
            throw new IllegalStateException(String.format(
                    "Schema %s is missing \"%s\" field or is null",
                    resourceRef.ref,
                    TITLE_FIELD_NAME
            ));
        } else if (!title.isTextual()) {
            throw new IllegalStateException(String.format(
                    "Schema \"%s\" has non-textual \"%s\" field",
                    resourceRef.ref,
                    TITLE_FIELD_NAME
            ));
        }

        final String transformedTitle = title.asText()
                .toLowerCase()
                .replace(" ", "_");
        resourceRef.schema.put(TITLE_FIELD_NAME, transformedTitle);
        resourceRef.setTitle(transformedTitle);
        this.nestedSchemaPrefix += "/" + transformedTitle;
        LOGGER.info(
                "[SCHEMA: {}] Transformed title {} -> {}",
                resourceRef.ref,
                title.asText(),
                transformedTitle
        );
        return resourceRef;
    }

    private ResourceRef rewriteMergeRef(final ResourceRef resourceRef) {
        final String def = this.nestedSchemaPrefix;
        resourceRef.refParent.put(REF_FIELD_NAME, def);
        LOGGER.info(
                "[SCHEMA: {}] Rewrote merge ref to {}",
                resourceRef.ref,
                def
        );
        return resourceRef;
    }

    private ResourceRef rewriteDefs(final ResourceRef resourceRef) {
        // TODO: Rewrite all $ref fields with #/$defs prefix, to use nestedSchemaPrefix
        return resourceRef;
    }

    private ResourceRef recursivelyMerge(final ResourceRef resourceRef) {
        final SchemaMerger merger = new SchemaMerger(
                resourceRef.schema,
                this.nestedSchemaPrefix + "/" + SUB_SCHEMAS_FIELD_NAME
        );
        merger.registerTransformers(this.transformers);
        merger.merge();
        final ObjectNode merged = merger.getMerged();
        resourceRef.setSchema(merged);
        LOGGER.info(
                "[SCHEMA: {}] Recursively merged sub-schemas",
                resourceRef.ref
        );
        return resourceRef;
    }

    private void createSubSchemaEntry(final ResourceRef resourceRef) {
        this.subSchemas.putObject(resourceRef.title)
                .setAll(resourceRef.schema);
        LOGGER.info(
                "[SCHEMA: {}] Created sub-schema entry as {}",
                resourceRef.ref,
                resourceRef.title
        );
    }

    public void merge() {
        this.subSchemas = this.mainSchema.putObject(SUB_SCHEMAS_FIELD_NAME);
        applyTransformers();
        findResourceRefParents()
                .stream()
                .map(this::resolveRef)
                .map(this::stripHeaders)
                .map(this::transformTitle)
                .map(this::rewriteMergeRef)
                .map(this::rewriteDefs)
                .map(this::recursivelyMerge)
                .forEach(this::createSubSchemaEntry);
    }

    public ObjectNode getMerged() {
        return this.mainSchema;
    }

    public void writeMerged(final String fileName) {
        try {
            MAPPER.writerWithDefaultPrettyPrinter().writeValue(
                    new File(fileName),
                    this.mainSchema
            );
            LOGGER.info(
                    "Wrote merged schema to file {}",
                    fileName
            );
        } catch (final IOException e) {
            throw new RuntimeException("Unable to write merged schema to " + fileName, e);
        }
    }

}
