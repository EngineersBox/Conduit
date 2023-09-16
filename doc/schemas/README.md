# README

## Top-level Schemas

*   [Connection](./connection.md "Connection types and attributes for connecting to a metrics source") – `classpath:/schemas/connection/connection.schema.json`

*   [HTTP Connection](./http_connection.md "HTTP connection schema with SSL and context support parameters") – `classpath:/schemas/connection/http_connection.schema.json`

*   [Metrics](./metrics.md "Metrics ingestion schema for creating Riemann events") – `classpath:/schemas/metrics.schema.json`

## Other Schemas

### Objects

*   [Untitled object in Connection](./connection-properties-config.md) – `classpath:/schemas/connection/connection.schema.json#/properties/config`

*   [Untitled object in HTTP Connection](./http_connection-defs-http_connection_authentication.md) – `classpath:/schemas/connection/http_connection.schema.json#/$defs/http_connection_authentication`

*   [Untitled object in HTTP Connection](./http_connection-defs-http_connection_ssl_context.md) – `classpath:/schemas/connection/http_connection.schema.json#/$defs/http_connection_ssl_context`

*   [Untitled object in HTTP Connection](./http_connection-defs-http_connection_ssl_context_manager.md) – `classpath:/schemas/connection/http_connection.schema.json#/$defs/http_connection_ssl_context_manager`

*   [Untitled object in HTTP Connection](./http_connection-defs-http_connection_ssl_parameters.md) – `classpath:/schemas/connection/http_connection.schema.json#/$defs/http_connection_ssl_parameters`

*   [Untitled object in Metrics](./metrics-properties-event_template.md) – `classpath:/schemas/metrics.schema.json#/properties/event_template`

*   [Untitled object in Metrics](./metrics-defs-metric-properties-extensions.md) – `classpath:/schemas/metrics.schema.json#/$defs/metric/properties/extensions`

*   [Untitled object in Metrics](./metrics-properties-extensions.md) – `classpath:/schemas/metrics.schema.json#/properties/extensions`

*   [Untitled object in Metrics](./metrics-defs-configuration.md) – `classpath:/schemas/metrics.schema.json#/$defs/configuration`

*   [Untitled object in Metrics](./metrics-defs-primitive_metric_structure.md) – `classpath:/schemas/metrics.schema.json#/$defs/primitive_metric_structure`

*   [Untitled object in Metrics](./metrics-defs-container_metric_structure.md) – `classpath:/schemas/metrics.schema.json#/$defs/container_metric_structure`

*   [Untitled object in Metrics](./metrics-defs-metric.md) – `classpath:/schemas/metrics.schema.json#/$defs/metric`

*   [Untitled object in Metrics](./metrics-defs-range_suffix_format.md) – `classpath:/schemas/metrics.schema.json#/$defs/range_suffix_format`

### Arrays

*   [Untitled array in HTTP Connection](./http_connection-defs-http_connection_ssl_parameters-properties-cipher_suites.md) – `classpath:/schemas/connection/http_connection.schema.json#/$defs/http_connection_ssl_parameters/properties/cipher_suites`

*   [Untitled array in HTTP Connection](./http_connection-defs-http_connection_ssl_parameters-properties-application_protocols.md) – `classpath:/schemas/connection/http_connection.schema.json#/$defs/http_connection_ssl_parameters/properties/application_protocols`

*   [Untitled array in HTTP Connection](./http_connection-defs-http_connection_ssl_parameters-properties-protocols.md) – `classpath:/schemas/connection/http_connection.schema.json#/$defs/http_connection_ssl_parameters/properties/protocols`

*   [Untitled array in HTTP Connection](./http_connection-defs-http_connection_ssl_parameters-properties-signature_schemes.md) – `classpath:/schemas/connection/http_connection.schema.json#/$defs/http_connection_ssl_parameters/properties/signature_schemes`

*   [Untitled array in HTTP Connection](./http_connection-defs-http_connection_ssl_parameters-properties-named_groups.md) – `classpath:/schemas/connection/http_connection.schema.json#/$defs/http_connection_ssl_parameters/properties/named_groups`

*   [Untitled array in Metrics](./metrics-properties-metrics.md) – `classpath:/schemas/metrics.schema.json#/properties/metrics`

*   [Untitled array in Metrics](./metrics-defs-container_metric_structure-properties-suffixes.md) – `classpath:/schemas/metrics.schema.json#/$defs/container_metric_structure/properties/suffixes`

## Version Note

The schemas linked above follow the JSON Schema Spec version: `https://json-schema.org/draft/2020-12/schema`
