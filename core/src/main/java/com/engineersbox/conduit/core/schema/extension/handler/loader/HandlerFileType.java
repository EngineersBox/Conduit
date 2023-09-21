package com.engineersbox.conduit.core.schema.extension.handler.loader;

import java.io.File;
import java.util.Arrays;
import java.util.stream.Collectors;

public enum HandlerFileType {
    CLASS,
    LUA;

    private static final String FILE_EXTENSIONS = Arrays.stream(HandlerFileType.values())
            .map(HandlerFileType::name)
            .map(String::toLowerCase)
            .map((final String name) -> "." + name)
            .collect(Collectors.joining(","));

    public static HandlerFileType fromFile(final File file) {
        if (file == null) {
            throw new NullPointerException("Cannot determine HandlerFileType for null file");
        }
        final String[] nameExt = file.getName().split("\\.");
        if (nameExt.length < 2) {
            throw new IllegalArgumentException(String.format(
                    "Unknowable file type for file: %s",
                    file.getPath()
            ));
        }
        try {
            return HandlerFileType.valueOf(nameExt[nameExt.length - 1].toUpperCase());
        } catch (final IllegalArgumentException e) {
            throw new IllegalStateException(String.format(
                    "Unsupported file type for file: %s, expected one of [%s]",
                    file.getPath(),
                    FILE_EXTENSIONS
            ), e);
        }
    }

}
