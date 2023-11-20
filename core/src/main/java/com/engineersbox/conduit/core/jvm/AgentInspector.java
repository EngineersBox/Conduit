package com.engineersbox.conduit.core.jvm;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.map.MutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.*;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.math.BigInteger;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

public class AgentInspector {

    private static final Logger LOGGER = LoggerFactory.getLogger(AgentInspector.class);
    private static final MutableMap<String, String> LOADED_AGENTS = Maps.mutable.empty();
    private static final String DIAGNOSTICS_BEAN = "com.sun.management:type=DiagnosticCommand";
    private static final String DYNAMIC_LIBS_OPERATION_NAME = "vmDynlibs";
    private static final String DLL_EXTENSION = ".ddl";
    private static final String DYLIB_EXTENSION = ".dylib";
    private static final String SO_EXTENSION = ".so";
    private static final String AGENT_PATH_OPTION_PREFIX = "-agentpath";
    private static final String AGENT_LIB_OPTION_PREFIX = "-agentlib";
    private static final String AGENT_JAR_OPTION_PREFIX = "-javaagent";

    static {
        resolveAttached();
        resolveCommandLine();
    }

    private AgentInspector() {
        throw new UnsupportedOperationException("Static utility class");
    }

    private static void resolveAttached() {
        final ObjectName diagnosticsCmdName;
        try {
            diagnosticsCmdName = new ObjectName(DIAGNOSTICS_BEAN);
        } catch (final MalformedObjectNameException e) {
            LOGGER.error("Unable to resolve attached agents", e);
            return;
        }
        final MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        final Object result;
        try {
            result = server.invoke(
                    diagnosticsCmdName,
                    DYNAMIC_LIBS_OPERATION_NAME,
                    null,
                    null
            );
        } catch (final ReflectionException | InstanceNotFoundException | MBeanException e) {
            LOGGER.error("Unable to resolve attached agents", e);
            return;
        }
        if (!(result instanceof String libs)) {
            LOGGER.error(
                    "Invoking diagnostics bean [{}] for {} operation yielded non-string result",
                    DIAGNOSTICS_BEAN,
                    DYNAMIC_LIBS_OPERATION_NAME
            );
            return;
        }
        final String[] lines = libs.split("\n");
        for (int i = 1; i < lines.length; i++) {
            final String line = lines[i];
            final String[] segments = StringUtils.split(
                    line,
                    " ",
                    3
            );
            final BigInteger offset = new BigInteger(
                    StringUtils.stripStart(
                            segments[0],
                            "0x"
                    ),
                    16
            );
            final String path = segments[1].trim();
            final String name;
            try {
                name = Path.of(path).toFile().getName();
            } catch (final InvalidPathException e) {
                LOGGER.trace(
                        "Cannot parse {} line [{}], path is invalid, skipping",
                        DYNAMIC_LIBS_OPERATION_NAME,
                        line
                );
                continue;
            }
            final String strippedName;
            if (name.endsWith(DLL_EXTENSION)) {
                strippedName = StringUtils.stripEnd(name, DLL_EXTENSION);
            } else if (name.endsWith(SO_EXTENSION)) {
                strippedName = StringUtils.stripEnd(name, SO_EXTENSION);
            } else if (name.endsWith(DYLIB_EXTENSION)) {
                strippedName = StringUtils.stripEnd(name, DYLIB_EXTENSION);
            } else {
                LOGGER.trace(
                        "{} line {} is not one of [{},{},{}], skipping",
                        DYNAMIC_LIBS_OPERATION_NAME,
                        line,
                        DLL_EXTENSION,
                        DYLIB_EXTENSION,
                        SO_EXTENSION
                );
                continue;
            }
            LOADED_AGENTS.put(
                    strippedName,
                    path
            );

            LOGGER.trace(
                    "Found attach API agent {} at {} with offset 0x{}",
                    strippedName,
                    path,
                    String.format("%016x", offset)
            );
        }
    }

    private static void resolveCommandLine() {
        final RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        final List<String> jvmArgs = runtimeMXBean.getInputArguments();
        for (final String arg : jvmArgs) {
            if (arg.startsWith(AGENT_LIB_OPTION_PREFIX)) {
                parseAgentLib(arg);
            } else if (arg.startsWith(AGENT_PATH_OPTION_PREFIX)) {
                parseAgentPath(arg, AGENT_PATH_OPTION_PREFIX);
            } else if (arg.startsWith(AGENT_JAR_OPTION_PREFIX)) {
                parseAgentPath(arg, AGENT_JAR_OPTION_PREFIX);
            }
        }
    }

    private static void parseAgentLib(final String arg) {
        final String lib = StringUtils.stripStart(
                arg,
                AGENT_LIB_OPTION_PREFIX + ":"
        );
        LOADED_AGENTS.put(lib, lib);
        LOGGER.trace(
                "Found CLI argument agent lib {}",
                lib
        );
    }

    private static void parseAgentPath(final String arg, final String prefix) {
        final String path = StringUtils.stripStart(
                arg,
                prefix + ":"
        );
        final String name;
        try {
            name = Path.of(path).toFile().getName();
        } catch (final InvalidPathException e) {
            LOGGER.trace(
                    "Cannot parse argument {}, path is invalid",
                    arg
            );
            return;
        }
        final String strippedName = String.join(
                ".",
                name.split("\\.")
        );
        LOADED_AGENTS.put(
                strippedName,
                path
        );
        LOGGER.trace(
                "Found CLI argument agent {} with path {}",
                strippedName,
                path
        );
    }

    public static boolean agentLoaded(final Pattern regex) {
        return LOADED_AGENTS.keySet()
                .stream()
                .anyMatch((final String name) -> regex.matcher(name).matches());
    }

}
