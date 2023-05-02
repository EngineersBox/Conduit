package com.engineersbox.conduit.schema;

import com.engineersbox.conduit.util.ObjectMapperModule;
import com.google.common.hash.Hashing;
import com.google.common.io.ByteSource;
import com.google.common.io.Files;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class MetricsSchemaProvider extends ReentrantLock {

    private static final int CHUNK_SIZE_BYTES_DEFAULT = 4096;

    public abstract MetricsSchema provide();
    public abstract void refresh();

    public static MetricsSchemaProvider singleton(final MetricsSchema schema) {
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

    public static MetricsSchemaProvider checksumRefreshed(final String schemaPath) {
        return MetricsSchemaProvider.checksumRefreshed(
                schemaPath,
                CHUNK_SIZE_BYTES_DEFAULT
        );
    }

    public static MetricsSchemaProvider checksumRefreshed(final String schemaPath,
                                                          final int chunkSizeBytes) {
        if (!Path.of(schemaPath).toFile().exists()) {
            throw new IllegalArgumentException("Schema could not be found at path " + schemaPath);
        }
        return new MetricsSchemaProvider() {

            private MetricsSchema schema = provide();
            private long fileSize;
            private long fileLastModifiedTime;
            {
                final File initialFile = Path.of(schemaPath).toFile();
                this.fileSize = initialFile.length();
                this.fileLastModifiedTime = FileUtils.lastModifiedUnchecked(initialFile);
            }
            private final int chunkCount = (int) (this.fileSize / chunkSizeBytes);
            private boolean updateHashes = false;
            private int lastComputedChunkHashIndex;
            private final long[] chunkHashes = new long[this.chunkCount];

            @Override
            public MetricsSchema provide() {
                if (super.isLocked()) {
                    return this.schema;
                }
                final File schemaFile = Path.of(schemaPath).toFile();
                final long updatedFileSize = schemaFile.length();
                final long updatedLastModifiedTime = FileUtils.lastModifiedUnchecked(schemaFile);
                if (updatedFileSize == this.fileSize && updatedLastModifiedTime == this.fileLastModifiedTime) {
                    return this.schema;
                } else if (compareChunkHashes()) {
                    return this.schema;
                }
                this.fileSize = updatedFileSize;
                this.fileLastModifiedTime = updatedLastModifiedTime;
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
                    this.chunkHashes[i] = computeHash(fileByteSource.slice(((long) i) * chunkSizeBytes, chunkSizeBytes));
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
                    this.chunkHashes[i] = computeHash(fileByteSource.slice(((long) i) * chunkSizeBytes, chunkSizeBytes));
                }
                this.lastComputedChunkHashIndex = 0;
                this.updateHashes = false;
            }
        };
    }

}
