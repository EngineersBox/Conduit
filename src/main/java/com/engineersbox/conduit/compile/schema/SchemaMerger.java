package com.engineersbox.conduit.compile.schema;

import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.networknt.schema.JsonMetaSchema;
import com.networknt.schema.NonValidationKeyword;
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
        if (args.length < 2) {
            throw new IllegalArgumentException("Usage: SchemaMerger <input resource> <output resource absolute path>");
        }
        final String inputResource = args[0];
        final String outputResourceAbsolutePath = args[1];
        LOGGER.info(
                "Merging sub-schemas of {} to unified file at {}",
                inputResource,
                outputResourceAbsolutePath
        );
        final SchemaMerger merger = new SchemaMerger(inputResource);
        merger.registerTransformer(new EnumRefTransformer());
        merger.merge();
        merger.writeMerged(outputResourceAbsolutePath);
    }

    private static final ObjectMapper MAPPER = new ObjectMapper();
    public static final String SUB_SCHEMAS_FIELD_NAME = "$subSchemas";
    private static final String REF_FIELD_NAME = "$ref";
    private static final String DEFS_FIELD_NAME = "$defs";
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
    private final String name;
    private final MutableList<SchemaTransformer> transformers;

    private SchemaMerger(final String resourcePath) {
        this.mainSchema = readResource(resourcePath);
        this.nestedSchemaPrefix = "#";
        this.transformers = Lists.mutable.empty();
        this.name = getTitle();
    }

    private SchemaMerger(final ObjectNode mainSchema,
                         final String nestedSchemaPrefix) {
        this.mainSchema = mainSchema;
        this.nestedSchemaPrefix = nestedSchemaPrefix;
        this.transformers = Lists.mutable.empty();
        this.name = getTitle();
    }

    private String getTitle() {
        if (this.mainSchema == null || this.mainSchema.isNull() || this.mainSchema.isMissingNode()) {
            return null;
        }
        final JsonNode titleNode = this.mainSchema.get(TITLE_FIELD_NAME);
        if (!titleNode.isTextual()) {
            return null;
        }
        return titleNode.asText();
    }

    public void registerTransformer(final SchemaTransformer transformer) {
        this.transformers.add(transformer);
    }

    public void registerTransformers(final Collection<SchemaTransformer> transformers) {
        this.transformers.addAll(transformers);
    }

    private String stripIfPresent(final String str, final String... prefixes) {
        String string = str;
        for (final String prefix : prefixes) {
            if (StringUtils.startsWith(string, prefix)) {
                string = StringUtils.stripStart(string, prefix);
            }
        }
        return string;
    }

    private ObjectNode readResource(final String resourcePath) {
        final String path = stripIfPresent(
                resourcePath,
                CLASSPATH_PREFIX, "/"
        );
        LOGGER.info(
                "{}Reading resource: {}",
                this.name == null ? "" : "[SCHEMA: " + this.name + "] ",
                path
        );
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
                "[SCHEMA: {}] Applied {} schema transformers: [{}]",
                this.name,
                this.transformers.size(),
                this.transformers.collect(Object::getClass)
                        .collect(Class::getSimpleName)
                        .makeString(",")
        );
    }

    private List<ObjectNode> findResourceRefParents() {
        final List<JsonNode> refFieldParents = this.mainSchema.findParents(REF_FIELD_NAME);
        LOGGER.info(
                "[SCHEMA: {}] Found {} ref fields",
                this.name,
                refFieldParents.size()
        );
        final List<ObjectNode> resourceRefFieldParents = refFieldParents.stream()
                .filter((final JsonNode node) -> {
                    final JsonNode ref = node.get(REF_FIELD_NAME);
                    return ref.isTextual() && StringUtils.startsWith(ref.asText(), CLASSPATH_PREFIX);
                }).filter(JsonNode::isObject)
                .map((final JsonNode node) -> (ObjectNode) node)
                .toList();
        LOGGER.info(
                "[SCHEMA: {}] Filtered ref fields to {} resource refs",
                this.name,
                resourceRefFieldParents.size()
        );
        return resourceRefFieldParents;
    }

    private ResourceRef resolveRef(final ObjectNode refParent) {
        final JsonNode refNode = refParent.get(REF_FIELD_NAME);
        final String ref = refNode.asText();
        final ObjectNode schema = readResource(StringUtils.substringAfter(
                ref,
                CLASSPATH_PREFIX
        ));
        LOGGER.info(
                "[SCHEMA: {}] Resolved ref {}",
                this.name,
                ref
        );
        return new ResourceRef(refParent, ref, schema);
    }

    private ResourceRef stripHeaders(final ResourceRef resourceRef) {
        resourceRef.schema.remove(HEADER_FIELDS);
        LOGGER.info(
                "[SCHEMA: {}] Stripped header fields",
                this.name
        );
        return resourceRef;
    }

    private ResourceRef rewriteDefs(final ResourceRef resourceRef) {
        final List<JsonNode> refFieldParents = resourceRef.schema.findParents(REF_FIELD_NAME)
                .stream()
                .filter((final JsonNode node) -> {
                    final JsonNode ref = node.get(REF_FIELD_NAME);
                    return ref.isTextual() && StringUtils.startsWith(ref.asText(), ROOT_REF_PREFIX + DEFS_FIELD_NAME);
                }).toList();
        int rewritten = 0;
        for (final JsonNode refParent : refFieldParents) {
            final JsonNode ref = refParent.get(REF_FIELD_NAME);
            if (!ref.isTextual() || !StringUtils.startsWith(ref.asText(), ROOT_REF_PREFIX + DEFS_FIELD_NAME)) {
                LOGGER.debug(
                        "[SCHEMA: {}] Ref is either non-textual or does not reference via {}",
                        this.name,
                        ROOT_REF_PREFIX + DEFS_FIELD_NAME
                );
                continue;
            }
            final String refLiteral = ref.asText().replace(
                    ROOT_REF_PREFIX,
                    this.nestedSchemaPrefix + "/"
            );
            final ObjectNode refParentObject = (ObjectNode) refParent;
            refParentObject.put(
                    REF_FIELD_NAME,
                    refLiteral
            );
            rewritten++;
        }
        LOGGER.info(
                "[SCHEMA: {}] {} refs with localised {} prefix to absolute unified path",
                this.name,
                rewritten,
                DEFS_FIELD_NAME
        );
        return resourceRef;
    }

    private ResourceRef transformTitle(final ResourceRef resourceRef) {
        final JsonNode title = resourceRef.schema.get(TITLE_FIELD_NAME);
        if (title == null || title.isNull() || title.isMissingNode()) {
            throw new IllegalStateException(String.format(
                    "Schema %s is missing \"%s\" field or is null",
                    this.name,
                    TITLE_FIELD_NAME
            ));
        } else if (!title.isTextual()) {
            throw new IllegalStateException(String.format(
                    "Schema \"%s\" has non-textual \"%s\" field",
                    this.name,
                    TITLE_FIELD_NAME
            ));
        }

        final String transformedTitle = title.asText()
                .toLowerCase()
                .replace(" ", "_");
        resourceRef.schema.put(TITLE_FIELD_NAME, transformedTitle);
        resourceRef.setTitle(transformedTitle);
        this.nestedSchemaPrefix += "/" + SUB_SCHEMAS_FIELD_NAME + "/" +  transformedTitle;
        LOGGER.info(
                "[SCHEMA: {}] Transformed title {} -> {}",
                this.name,
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
                this.name,
                def
        );
        return resourceRef;
    }

    private ResourceRef recursivelyMerge(final ResourceRef resourceRef) {
        final SchemaMerger merger = new SchemaMerger(
                resourceRef.schema,
                this.nestedSchemaPrefix
        );
        merger.registerTransformers(this.transformers);
        merger.merge();
        final ObjectNode merged = merger.getMerged();
        resourceRef.setSchema(merged);
        LOGGER.info(
                "[SCHEMA: {}] Recursively merged sub-schemas",
                this.name
        );
        return resourceRef;
    }

    private void createSubSchemaEntry(final ResourceRef resourceRef) {
        this.subSchemas.putObject(resourceRef.title)
                .setAll(resourceRef.schema);
        LOGGER.info(
                "[SCHEMA: {}] Created sub-schema entry as {}",
                this.name,
                resourceRef.title
        );
    }

    public void merge() {
        final List<ObjectNode> resourceRefParents = findResourceRefParents();
        if (!resourceRefParents.isEmpty()) {
            LOGGER.info(
                    "[SCHEMA: {}] No resource refs found, schema is a leaf. Skipping sub-schema merge.",
                    this.name
            );
            this.subSchemas = this.mainSchema.putObject(SUB_SCHEMAS_FIELD_NAME);
        }
        applyTransformers();
        resourceRefParents.stream()
                .map(this::resolveRef)
                .map(this::stripHeaders)
                .map(this::transformTitle)
                .map(this::rewriteDefs)
                .map(this::rewriteMergeRef)
                .map(this::recursivelyMerge)
                .forEach(this::createSubSchemaEntry);
        LOGGER.info(
                "[SCHEMA: {}] Finished schema merge",
                this.name
        );
    }

    public ObjectNode getMerged() {
        return this.mainSchema;
    }

    private PrettyPrinter createPrettyPrinter() {
        final DefaultIndenter indenter = new DefaultIndenter(
                "\t",
                DefaultIndenter.SYS_LF
        );
        return new DefaultPrettyPrinter()
                .withArrayIndenter(indenter)
                .withObjectIndenter(indenter);
    }

    public void writeMerged(final String fileName) {
        try {
            MAPPER.writer(createPrettyPrinter()).writeValue(
                    new File(fileName),
                    this.mainSchema
            );
            LOGGER.info(
                    "[SCHEMA: {}] Wrote merged schema to file {}",
                    this.name,
                    fileName
            );
        } catch (final IOException e) {
            throw new RuntimeException("Unable to write merged schema to " + fileName, e);
        }
    }

    public static JsonMetaSchema amendMetaSchema(final String uri,
                                                 final JsonMetaSchema template) {
        return JsonMetaSchema.builder(uri, template)
                .addKeyword(new NonValidationKeyword(SUB_SCHEMAS_FIELD_NAME))
                .build();
    }

}
