# Untitled object in HTTP Connection Schema

```txt
classpath:/schemas/connection/http_connection.schema.json#/$defs/http_connection_ssl_context
```



| Abstract            | Extensible | Status         | Identifiable | Custom Properties | Additional Properties | Access Restrictions | Defined In                                                                                                |
| :------------------ | :--------- | :------------- | :----------- | :---------------- | :-------------------- | :------------------ | :-------------------------------------------------------------------------------------------------------- |
| Can be instantiated | No         | Unknown status | No           | Forbidden         | Forbidden             | none                | [http\_connection.schema.json\*](../../out/connection/http_connection.schema.json "open original schema") |

## http\_connection\_ssl\_context Type

`object` ([Details](http_connection-defs-http_connection_ssl_context.md))

# http\_connection\_ssl\_context Properties

| Property                                              | Type     | Required | Nullable       | Defined by                                                                                                                                                                                                                                  |
| :---------------------------------------------------- | :------- | :------- | :------------- | :------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| [protocol](#protocol)                                 | `string` | Optional | cannot be null | [HTTP Connection](http_connection-defs-http_connection_ssl_context-properties-protocol.md "classpath:/schemas/connection/http_connection.schema.json#/$defs/http_connection_ssl_context/properties/protocol")                               |
| [provider](#provider)                                 | `string` | Optional | cannot be null | [HTTP Connection](http_connection-defs-http_connection_ssl_context-properties-provider.md "classpath:/schemas/connection/http_connection.schema.json#/$defs/http_connection_ssl_context/properties/provider")                               |
| [key\_manager](#key_manager)                          | `object` | Optional | cannot be null | [HTTP Connection](http_connection-defs-http_connection_ssl_context_manager.md "classpath:/schemas/connection/http_connection.schema.json#/$defs/http_connection_ssl_context/properties/key_manager")                                        |
| [trust\_manager](#trust_manager)                      | `object` | Optional | cannot be null | [HTTP Connection](http_connection-defs-http_connection_ssl_context_manager.md "classpath:/schemas/connection/http_connection.schema.json#/$defs/http_connection_ssl_context/properties/trust_manager")                                      |
| [secure\_random\_algorithm](#secure_random_algorithm) | `string` | Optional | cannot be null | [HTTP Connection](http_connection-defs-http_connection_ssl_context-properties-secure_random_algorithm.md "classpath:/schemas/connection/http_connection.schema.json#/$defs/http_connection_ssl_context/properties/secure_random_algorithm") |
| [secure\_random\_provider](#secure_random_provider)   | `string` | Optional | cannot be null | [HTTP Connection](http_connection-defs-http_connection_ssl_context-properties-secure_random_provider.md "classpath:/schemas/connection/http_connection.schema.json#/$defs/http_connection_ssl_context/properties/secure_random_provider")   |

## protocol



`protocol`

*   is optional

*   Type: `string`

*   cannot be null

*   defined in: [HTTP Connection](http_connection-defs-http_connection_ssl_context-properties-protocol.md "classpath:/schemas/connection/http_connection.schema.json#/$defs/http_connection_ssl_context/properties/protocol")

### protocol Type

`string`

## provider



`provider`

*   is optional

*   Type: `string`

*   cannot be null

*   defined in: [HTTP Connection](http_connection-defs-http_connection_ssl_context-properties-provider.md "classpath:/schemas/connection/http_connection.schema.json#/$defs/http_connection_ssl_context/properties/provider")

### provider Type

`string`

## key\_manager



`key_manager`

*   is optional

*   Type: `object` ([Details](http_connection-defs-http_connection_ssl_context_manager.md))

*   cannot be null

*   defined in: [HTTP Connection](http_connection-defs-http_connection_ssl_context_manager.md "classpath:/schemas/connection/http_connection.schema.json#/$defs/http_connection_ssl_context/properties/key_manager")

### key\_manager Type

`object` ([Details](http_connection-defs-http_connection_ssl_context_manager.md))

## trust\_manager



`trust_manager`

*   is optional

*   Type: `object` ([Details](http_connection-defs-http_connection_ssl_context_manager.md))

*   cannot be null

*   defined in: [HTTP Connection](http_connection-defs-http_connection_ssl_context_manager.md "classpath:/schemas/connection/http_connection.schema.json#/$defs/http_connection_ssl_context/properties/trust_manager")

### trust\_manager Type

`object` ([Details](http_connection-defs-http_connection_ssl_context_manager.md))

## secure\_random\_algorithm



`secure_random_algorithm`

*   is optional

*   Type: `string`

*   cannot be null

*   defined in: [HTTP Connection](http_connection-defs-http_connection_ssl_context-properties-secure_random_algorithm.md "classpath:/schemas/connection/http_connection.schema.json#/$defs/http_connection_ssl_context/properties/secure_random_algorithm")

### secure\_random\_algorithm Type

`string`

## secure\_random\_provider



`secure_random_provider`

*   is optional

*   Type: `string`

*   cannot be null

*   defined in: [HTTP Connection](http_connection-defs-http_connection_ssl_context-properties-secure_random_provider.md "classpath:/schemas/connection/http_connection.schema.json#/$defs/http_connection_ssl_context/properties/secure_random_provider")

### secure\_random\_provider Type

`string`
