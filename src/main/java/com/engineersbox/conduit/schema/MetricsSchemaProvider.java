package com.engineersbox.conduit.schema;

import com.engineersbox.conduit.util.ObjectMapperModule;
import com.google.common.hash.Hashing;
import com.google.common.io.ByteSource;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public interface MetricsSchemaProvider {

    int CHUNK_SIZE_DEFAULT = 4096;

    MetricsSchema provide();
    void refresh();

    static MetricsSchemaProvider singleton(final MetricsSchema schema) {
        return new MetricsSchemaProvider() {
            @Override
            public MetricsSchema provide() {
                return schema;
            }

            @Override
            public void refresh() {

            }
        };
    }

    static MetricsSchemaProvider checksumRefreshed(final String schemaPath) {
        return MetricsSchemaProvider.checksumRefreshed(
                schemaPath,
                CHUNK_SIZE_DEFAULT
        );
    }

    static MetricsSchemaProvider checksumRefreshed(final String schemaPath,
                                                   final int chunkSize) {
        if (!Path.of(schemaPath).toFile().exists()) {
            throw new IllegalArgumentException("Schema could not be found at path " + schemaPath);
        }
        return new MetricsSchemaProvider() {

            private MetricsSchema schema = provide();
            private long fileSize;
            private final int chunkCount;
            private boolean updateHashes;
            private int lastComputedChunkHashIndex;
            private final long[] chunkHashes;
            private long schemaHash;

            {
                this.fileSize = Path.of(schemaPath).toFile().length();
                this.chunkCount = (int) (this.fileSize / chunkSize);
                this.chunkHashes = new long[this.chunkCount];
                this.updateHashes = false;
            }

            @Override
            public MetricsSchema provide() {
                final File schemaFile = Path.of(schemaPath).toFile();
                final long updatedFileSize = schemaFile.length();
                if (updatedFileSize == this.fileSize && compareChunkHashes()) {
                    return this.schema;
                }
                this.fileSize = updatedFileSize;
                this.updateHashes = true;
                try {
                    this.schema = MetricsSchema.from(ObjectMapperModule.OBJECT_MAPPER.readTree(schemaFile));
                    return this.schema;
                } catch (IOException e) {
                    throw new IllegalStateException("Unable to read schema from path " + schemaPath, e);
                }
            }

            private boolean compareChunkHashes() {
                final ByteSource fileByteSource = Files.asByteSource(Path.of(schemaPath).toFile());
                for (int i = 0; i < this.chunkCount; i++) {
                    final long currentChunkHash = this.chunkHashes[i];
                    this.chunkHashes[i] = computeHash(fileByteSource.slice(((long) i) * chunkSize, chunkSize));
                    if (currentChunkHash != this.chunkHashes[i]) {
                        this.lastComputedChunkHashIndex = i;
                        return false;
                    }
                }
                return true;
            }

            private long computeHash(final ByteSource chunkSlice) {
                try {
                    return chunkSlice.hash(Hashing.adler32()).padToLong();
                } catch (final IOException e) {
                    throw new IllegalStateException("Unable to compute Adler32 hash for file " + schemaPath);
                }
            }

            @Override
            public void refresh() {
                if (!this.updateHashes) {
                    this.lastComputedChunkHashIndex = 0;
                    return;
                }
                final ByteSource fileByteSource = Files.asByteSource(Path.of(schemaPath).toFile());
                for (int i = this.lastComputedChunkHashIndex; i < this.chunkCount; i++) {
                    this.chunkHashes[i] = computeHash(fileByteSource.slice(((long) i) * chunkSize, chunkSize));
                }
                this.lastComputedChunkHashIndex = 0;
                this.updateHashes = false;
            }
        };
    }

}
