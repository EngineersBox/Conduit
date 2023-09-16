# Untitled object in HTTP Connection Schema

```txt
classpath:/schemas/connection/http_connection.schema.json#/$defs/http_connection_ssl_context_manager
```



| Abstract            | Extensible | Status         | Identifiable | Custom Properties | Additional Properties | Access Restrictions | Defined In                                                                                                |
| :------------------ | :--------- | :------------- | :----------- | :---------------- | :-------------------- | :------------------ | :-------------------------------------------------------------------------------------------------------- |
| Can be instantiated | No         | Unknown status | No           | Forbidden         | Forbidden             | none                | [http\_connection.schema.json\*](../../out/connection/http_connection.schema.json "open original schema") |

## http\_connection\_ssl\_context\_manager Type

`object` ([Details](http_connection-defs-http_connection_ssl_context_manager.md))

# http\_connection\_ssl\_context\_manager Properties

| Property                                    | Type     | Required | Nullable       | Defined by                                                                                                                                                                                                                                        |
| :------------------------------------------ | :------- | :------- | :------------- | :------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| [key\_store\_path](#key_store_path)         | `string` | Optional | cannot be null | [HTTP Connection](http_connection-defs-http_connection_ssl_context_manager-properties-key_store_path.md "classpath:/schemas/connection/http_connection.schema.json#/$defs/http_connection_ssl_context_manager/properties/key_store_path")         |
| [key\_store\_password](#key_store_password) | `string` | Optional | cannot be null | [HTTP Connection](http_connection-defs-http_connection_ssl_context_manager-properties-key_store_password.md "classpath:/schemas/connection/http_connection.schema.json#/$defs/http_connection_ssl_context_manager/properties/key_store_password") |
| [algorithm](#algorithm)                     | `string` | Optional | cannot be null | [HTTP Connection](http_connection-defs-http_connection_ssl_context_manager-properties-algorithm.md "classpath:/schemas/connection/http_connection.schema.json#/$defs/http_connection_ssl_context_manager/properties/algorithm")                   |
| [provider](#provider)                       | `string` | Optional | cannot be null | [HTTP Connection](http_connection-defs-http_connection_ssl_context_manager-properties-provider.md "classpath:/schemas/connection/http_connection.schema.json#/$defs/http_connection_ssl_context_manager/properties/provider")                     |

## key\_store\_path



`key_store_path`

*   is optional

*   Type: `string`

*   cannot be null

*   defined in: [HTTP Connection](http_connection-defs-http_connection_ssl_context_manager-properties-key_store_path.md "classpath:/schemas/connection/http_connection.schema.json#/$defs/http_connection_ssl_context_manager/properties/key_store_path")

### key\_store\_path Type

`string`

## key\_store\_password



`key_store_password`

*   is optional

*   Type: `string`

*   cannot be null

*   defined in: [HTTP Connection](http_connection-defs-http_connection_ssl_context_manager-properties-key_store_password.md "classpath:/schemas/connection/http_connection.schema.json#/$defs/http_connection_ssl_context_manager/properties/key_store_password")

### key\_store\_password Type

`string`

## algorithm



`algorithm`

*   is optional

*   Type: `string`

*   cannot be null

*   defined in: [HTTP Connection](http_connection-defs-http_connection_ssl_context_manager-properties-algorithm.md "classpath:/schemas/connection/http_connection.schema.json#/$defs/http_connection_ssl_context_manager/properties/algorithm")

### algorithm Type

`string`

## provider



`provider`

*   is optional

*   Type: `string`

*   cannot be null

*   defined in: [HTTP Connection](http_connection-defs-http_connection_ssl_context_manager-properties-provider.md "classpath:/schemas/connection/http_connection.schema.json#/$defs/http_connection_ssl_context_manager/properties/provider")

### provider Type

`string`
