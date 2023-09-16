# Connection Schema

```txt
classpath:/schemas/connection/connection.schema.json#/properties/connection
```

Connection types and attributes for connecting to a metrics source

| Abstract            | Extensible | Status         | Identifiable | Custom Properties | Additional Properties | Access Restrictions | Defined In                                                                    |
| :------------------ | :--------- | :------------- | :----------- | :---------------- | :-------------------- | :------------------ | :---------------------------------------------------------------------------- |
| Can be instantiated | No         | Unknown status | No           | Forbidden         | Forbidden             | none                | [metrics.schema.json\*](../../out/metrics.schema.json "open original schema") |

## connection Type

`object` ([Connection](metrics-properties-connection.md))

# connection Properties

| Property          | Type          | Required | Nullable       | Defined by                                                                                                              |
| :---------------- | :------------ | :------- | :------------- | :---------------------------------------------------------------------------------------------------------------------- |
| [type](#type)     | Not specified | Required | cannot be null | [Connection](connection-properties-type.md "classpath:/schemas/connection/connection.schema.json#/properties/type")     |
| [config](#config) | `object`      | Optional | cannot be null | [Connection](connection-properties-config.md "classpath:/schemas/connection/connection.schema.json#/properties/config") |

## type



`type`

*   is required

*   Type: unknown

*   cannot be null

*   defined in: [Connection](connection-properties-type.md "classpath:/schemas/connection/connection.schema.json#/properties/type")

### type Type

unknown

### type Constraints

**enum**: the value of this property must be equal to one of the following values:

| Value      | Explanation |
| :--------- | :---------- |
| `"HTTP"`   |             |
| `"CUSTOM"` |             |

## config



`config`

*   is optional

*   Type: `object` ([Details](connection-properties-config.md))

*   cannot be null

*   defined in: [Connection](connection-properties-config.md "classpath:/schemas/connection/connection.schema.json#/properties/config")

### config Type

`object` ([Details](connection-properties-config.md))

# Connection Definitions

## Definitions group connection\_type

Reference this group by using

```json
{"$ref":"classpath:/schemas/connection/connection.schema.json#/$defs/connection_type"}
```

| Property | Type | Required | Nullable | Defined by |
| :------- | :--- | :------- | :------- | :--------- |

## Definitions group custom\_connection

Reference this group by using

```json
{"$ref":"classpath:/schemas/connection/connection.schema.json#/$defs/custom_connection"}
```

| Property              | Type | Required | Nullable    | Defined by |
| :-------------------- | :--- | :------- | :---------- | :--------- |
| Additional Properties | Any  | Optional | can be null |            |

### Additional Properties

Additional properties are allowed and do not have to follow a specific schema
