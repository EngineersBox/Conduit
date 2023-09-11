package com.engineersbox.conduit_v2.schema.factory;

import com.engineersbox.conduit_v2.schema.Schema;

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
        return new SingletonMetricsSchemaFactory(schema);
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
        return new ChecksumRefreshedMetricsSchemaFactory(
                schemaPath,
                chunkSizeBytes,
                maxChunkCount,
                compareHashes
        );
    }

}
