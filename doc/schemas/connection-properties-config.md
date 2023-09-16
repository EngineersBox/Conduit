# Untitled object in Connection Schema

```txt
classpath:/schemas/connection/connection.schema.json#/properties/config
```



| Abstract            | Extensible | Status         | Identifiable | Custom Properties | Additional Properties | Access Restrictions | Defined In                                                                                     |
| :------------------ | :--------- | :------------- | :----------- | :---------------- | :-------------------- | :------------------ | :--------------------------------------------------------------------------------------------- |
| Can be instantiated | No         | Unknown status | No           | Forbidden         | Allowed               | none                | [connection.schema.json\*](../../out/connection/connection.schema.json "open original schema") |

## config Type

`object` ([Details](connection-properties-config.md))

# config Properties

| Property      | Type          | Required | Nullable       | Defined by                                                                                                                                                              |
| :------------ | :------------ | :------- | :------------- | :---------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| [if](#if)     | Not specified | Optional | cannot be null | [Connection](connection-properties-config-properties-if.md "classpath:/schemas/connection/connection.schema.json#/properties/config/properties/if")                     |
| [then](#then) | `object`      | Optional | cannot be null | [Connection](connection-properties-config-properties-http-connection.md "classpath:/schemas/connection/http_connection.schema.json#/properties/config/properties/then") |
| [else](#else) | Not specified | Optional | cannot be null | [Connection](connection-properties-config-properties-else.md "classpath:/schemas/connection/connection.schema.json#/properties/config/properties/else")                 |

## if



`if`

*   is optional

*   Type: unknown

*   cannot be null

*   defined in: [Connection](connection-properties-config-properties-if.md "classpath:/schemas/connection/connection.schema.json#/properties/config/properties/if")

### if Type

unknown

## then

HTTP connection schema with SSL and context support parameters

`then`

*   is optional

*   Type: `object` ([HTTP Connection](connection-properties-config-properties-http-connection.md))

*   cannot be null

*   defined in: [Connection](connection-properties-config-properties-http-connection.md "classpath:/schemas/connection/http_connection.schema.json#/properties/config/properties/then")

### then Type

`object` ([HTTP Connection](connection-properties-config-properties-http-connection.md))

## else



`else`

*   is optional

*   Type: unknown

*   cannot be null

*   defined in: [Connection](connection-properties-config-properties-else.md "classpath:/schemas/connection/connection.schema.json#/properties/config/properties/else")

### else Type

unknown
