package com.engineersbox.conduit.handler.loader;

import java.io.File;

public enum HandlerFileType {
    CLASS,
    LUA;

    public static HandlerFileType fromFile(final File file) {
        if (file == null) {
            return null;
        }
        final String[] nameExt = file.getName().split("\\.");
        if (nameExt.length < 2) {
            throw new IllegalArgumentException(String.format(
                    "Unknowable file type for file: %s",
                    file.getPath()
            ));
        }
        return switch (nameExt[nameExt.length - 1].toLowerCase()) {
            case "class" -> HandlerFileType.CLASS;
            case "lua" -> HandlerFileType.LUA;
            default -> throw new IllegalStateException(String.format(
                    "Unsupported file type for file: %s, expected either .class or .lua",
                    file.getPath()
            ));
        };
    }

}
