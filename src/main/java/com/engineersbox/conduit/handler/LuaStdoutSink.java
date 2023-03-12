package com.engineersbox.conduit.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.function.Consumer;

public class LuaStdoutSink extends PrintStream {

    private final String name;
    private final Level level;
    private final OutputStream stream;

    private LuaStdoutSink(final String name,
                          final Level level,
                          final OutputStream stream) {
        super(stream);
        this.name = name;
        this.level = level;
        this.stream = stream;

    }

    public String getName() {
        return this.name;
    }

    public Level getLevel() {
        return this.level;
    }

    public OutputStream getStream() {
        return this.stream;
    }

    public static LuaStdoutSink createSlf4j(final String name,
                                            final Level level) {
        return new LuaStdoutSink(
                name,
                level,
                new LoggerOutputStream(
                        LoggerFactory.getLogger(name),
                        level
                )
        );
    }

    public static class LoggerOutputStream extends OutputStream {
        private final Consumer<String> loggerMethod;

        private StringBuffer mem;

        public LoggerOutputStream(final Logger logger,
                                  final Level level) {
            this.mem = new StringBuffer();
            this.loggerMethod = switch (level) {
                case ERROR -> logger::error;
                case WARN -> logger::warn;
                case INFO -> logger::info;
                case DEBUG -> logger::debug;
                case TRACE -> logger::trace;
            };
        }

        @Override
        public void write(final int b) {
            if ((char) b == '\n') {
                flush();
                return;
            }
            this.mem.append((char) b);
        }

        @Override
        public void flush() {
            this.loggerMethod.accept(this.mem.toString());
            this.mem = new StringBuffer();
        }
    }

}
