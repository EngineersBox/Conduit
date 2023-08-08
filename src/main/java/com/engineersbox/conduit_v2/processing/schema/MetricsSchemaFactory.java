package com.engineersbox.conduit_v2.processing.schema;

import com.google.common.hash.Hashing;
import com.google.common.io.ByteSource;
import com.google.common.io.Files;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.locks.ReentrantLock;

public abstract class MetricsSchemaFactory extends ReentrantLock {

    private static final int CHUNK_SIZE_BYTES_DEFAULT = 4096;
    private static final int CHUNK_COUNT_DEFAULT = -1;

    public Schema provide(final boolean lock) {
        if (lock) {
            super.lock();
        }
        return provide();
    }
    protected abstract Schema provide();
    public abstract void refresh();

    public boolean instanceRefreshed() {
        return true;
    }

    public static MetricsSchemaFactory singleton(final Schema schema) {
        return new MetricsSchemaFactory() {
            @Override
            protected Schema provide() {
                return schema;
            }

            @Override
            public void refresh() {

            }
        };
    }

    public static MetricsSchemaFactory checksumRefreshed(final String schemaPath,
                                                         final boolean compareHashes) {
        return MetricsSchemaFactory.checksumRefreshed(
                schemaPath,
                CHUNK_SIZE_BYTES_DEFAULT,
                CHUNK_COUNT_DEFAULT,
                compareHashes
        );
    }

    public static MetricsSchemaFactory checksumRefreshed(final String schemaPath,
                                                         final long chunkSizeBytes,
                                                         final int maxChunkCount,
                                                         final boolean compareHashes) {
        if (!Path.of(schemaPath).toFile().exists()) {
            throw new IllegalArgumentException("Schema could not be found at path " + schemaPath);
        }
        return new MetricsSchemaFactory() {

            private static final Logger LOGGER = LoggerFactory.getLogger(MetricsSchemaFactory.class);

            private final ReentrantLock lock = new ReentrantLock(true);
            private Schema schema;
            private long fileSize;
            private long fileLastModifiedTime;
            {
                final File initialFile = Path.of(schemaPath).toFile();
                this.fileSize = initialFile.length();
                this.fileLastModifiedTime = FileUtils.lastModifiedUnchecked(initialFile);
                LOGGER.trace("[Checksum Refreshed] Initial file properties [Size: {}] [Modified: {}]", this.fileSize, this.fileLastModifiedTime);
                try {
                    this.schema = Schema.from(initialFile);
                } catch (final IOException e) {
                    throw new IllegalStateException("Unable to read schema from path " + schemaPath, e);
                }
            }
            private final int chunkCount = Math.max(maxChunkCount, (int) (this.fileSize / chunkSizeBytes));
            private boolean updateHashes = true;
            private int lastComputedChunkHashIndex;
            private final long[] chunkHashes = new long[this.chunkCount];

            @Override
            protected Schema provide() {
                if (!this.lock.tryLock()) {
                    return this.schema;
                }
                final File schemaFile = Path.of(schemaPath).toFile();
                final long updatedFileSize = schemaFile.length();
                final long updatedLastModifiedTime = FileUtils.lastModifiedUnchecked(schemaFile);
                LOGGER.trace(
                        "[Checksum Refreshed] Cached file: [Size: {}, Modified: {}] Refreshed file: [Size: {}, Modified: {}] Compute and compare hashes: {}",
                        this.fileSize, this.fileLastModifiedTime,
                        updatedFileSize, updatedLastModifiedTime,
                        compareHashes
                );
                if (updatedFileSize == this.fileSize && updatedLastModifiedTime == this.fileLastModifiedTime) {
                    LOGGER.trace(
                            "[Checksum Refreshed] File size is equivalent [{}] and modified time has not been updated [{}], returning cached schema",
                            this.fileSize,
                            this.fileLastModifiedTime
                    );
                    return this.schema;
                } else if (compareHashes && compareChunkHashes()) {
                    LOGGER.trace("[Checksum Refreshed] Computed chunk checksums are equivalent with cached chunk checksums, returning cached schema");
                    return this.schema;
                }
                this.fileSize = updatedFileSize;
                this.fileLastModifiedTime = updatedLastModifiedTime;
                this.updateHashes = true;
                try {
                    LOGGER.debug("[Checksum Refreshed] Refreshed schema required, parsing schema file {}", schemaPath);
                    this.schema = Schema.from(schemaFile);
                    return this.schema;
                } catch (final IOException e) {
                    throw new IllegalStateException("Unable to read schema from path " + schemaPath, e);
                }
            }

            private boolean compareChunkHashes() {
                final ByteSource fileByteSource = Files.asByteSource(Path.of(schemaPath).toFile());
                for (int i = 0; i < this.chunkCount; i++) {
                    final long currentChunkHash = this.chunkHashes[i];
                    this.chunkHashes[i] = computeHash(fileByteSource.slice(((long) i) * chunkSizeBytes, chunkSizeBytes));
                    LOGGER.trace("[Checksum Refreshed] Hash comparison at chunk {}: [Old: {}] [New: {}]", i, currentChunkHash, this.chunkHashes[i]);
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
                if (!this.lock.isLocked()) {
                    return;
                }
                if (!compareHashes || !this.updateHashes) {
                    this.lastComputedChunkHashIndex = 0;
                    return;
                }
                final ByteSource fileByteSource = Files.asByteSource(Path.of(schemaPath).toFile());
                LOGGER.trace("[Checksum Refreshed] Residual chunk hashes to recompute: {}", this.chunkCount - this.lastComputedChunkHashIndex);
                for (int i = this.lastComputedChunkHashIndex; i < this.chunkCount; i++) {
                    this.chunkHashes[i] = computeHash(fileByteSource.slice(i * chunkSizeBytes, chunkSizeBytes));
                }
                this.lastComputedChunkHashIndex = 0;
                this.updateHashes = false;
                this.lock.unlock();
            }

            private boolean refreshFlag = false;

            @Override
            public boolean instanceRefreshed() {
                if (!refreshFlag) {
                    this.refreshFlag = true;
                    return true;
                }
                return false;//this.updateHashes;
            }
        };
    }

}
