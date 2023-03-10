package com.engineersbox.conduit.schema;

import com.engineersbox.conduit.util.ObjectMapperModule;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

@FunctionalInterface
public interface MetricsSchemaProvider {

    MetricsSchema provide();

    static MetricsSchemaProvider singleton(final MetricsSchema schema) {
        return () -> schema;
    }

    static MetricsSchemaProvider checksumRefreshed(final String schemaPath) {
        if (!Path.of(schemaPath).toFile().exists()) {
            throw new IllegalArgumentException("Schema could not be found at path " + schemaPath);
        }
        return new MetricsSchemaProvider() {

            private MetricsSchema schema = provide();
            private long schemaHash;

            @Override
            public MetricsSchema provide() {
                /*  TODO: Improve the diff check between files by doing the following:
                 *        Check file sizes are equal
                 *        Break the files into chunks of size N bytes
                 *        Compute checksum on each pair of matching blocks and compare.
                 *        Any differences prove files are not the same.
                 *        Look into lazy stream of file with n chunks (store previous file chunk hashes)
                 *        and compare each chunk progressively for a fail-early approach
                 */
                final File schemaFile = Path.of(schemaPath).toFile();
                final long newSchemaHash = computeHash(schemaFile);
                if (this.schemaHash == newSchemaHash) {
                    return this.schema;
                }
                this.schemaHash = newSchemaHash;
                try {
                    this.schema = MetricsSchema.from(ObjectMapperModule.OBJECT_MAPPER.readTree(schemaFile));
                    return this.schema;
                } catch (IOException e) {
                    throw new IllegalStateException("Unable to read schema from path " + schemaPath, e);
                }
            }

            private static long computeHash(final File file) {
                try {
                    return Files.asByteSource(file).hash(Hashing.adler32()).padToLong();
                } catch (final IOException e) {
                    throw new IllegalStateException("Unable to compute Adler32 hash for file " + file.getName());
                }
            }
        };
    }

}
