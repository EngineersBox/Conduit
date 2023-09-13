package com.engineersbox.conduit.retrieval.ingest.connection;

import com.engineersbox.conduit.retrieval.ingest.connection.builtin.http.HTTPConnector;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.map.ConcurrentMutableMap;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.impl.map.mutable.ConcurrentHashMap;

public class ConnectorTypeResolver extends TypeIdResolverBase {

    private static final ConcurrentMutableMap<String, Class<? extends Connector<?,?>>> CONNECTOR_CLASSES = ConcurrentHashMap.newMap();

    static {
        CONNECTOR_CLASSES.put("HTTP", HTTPConnector.class);
    }

    public static void bindImplementation(final String name,
                                          final Class<? extends Connector<?,?>> subType) {
        CONNECTOR_CLASSES.compute(
                name,
                (final String key, final Class<? extends Connector<?,?>> mapping) -> {
                    if (mapping == null) {
                        return subType;
                    }
                    throw new IllegalStateException("Connector implementation is already bound: " + name);
                }
        );
    }

    private JavaType superType;

    @Override
    public void init(final JavaType baseType) {
        superType = baseType;
    }

    @Override
    public JsonTypeInfo.Id getMechanism() {
        return JsonTypeInfo.Id.CUSTOM;
    }

    @Override
    public String idFromValue(final Object obj) {
        return idFromValueAndType(obj, obj.getClass());
    }

    @Override
    public String idFromValueAndType(final Object obj,
                                     final Class<?> subType) {
        throw new IllegalStateException("Serialising metric schema is unsupported");
    }

    @Override
    public JavaType typeFromId(final DatabindContext context,
                               final String id) {
        final Class<? extends Connector<?,?>> subType = CONNECTOR_CLASSES.get(id);
        if (subType == null) {
            throw new IllegalStateException("Unsupported connector type encountered when deserialising metric schema. Did you forget to bind your custom implementation with ConnectorTypeResolver#bindImpementation(<name>,<class>)?");
        }
        return context.constructSpecializedType(superType, subType);
    }
}
