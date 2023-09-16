# Untitled object in HTTP Connection Schema

```txt
classpath:/schemas/connection/http_connection.schema.json#/$defs/http_connection_authentication
```



| Abstract            | Extensible | Status         | Identifiable | Custom Properties | Additional Properties | Access Restrictions | Defined In                                                                                                |
| :------------------ | :--------- | :------------- | :----------- | :---------------- | :-------------------- | :------------------ | :-------------------------------------------------------------------------------------------------------- |
| Can be instantiated | No         | Unknown status | No           | Forbidden         | Forbidden             | none                | [http\_connection.schema.json\*](../../out/connection/http_connection.schema.json "open original schema") |

## http\_connection\_authentication Type

`object` ([Details](http_connection-defs-http_connection_authentication.md))

# http\_connection\_authentication Properties

| Property              | Type     | Required | Nullable       | Defined by                                                                                                                                                                                                          |
| :-------------------- | :------- | :------- | :------------- | :------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| [username](#username) | `string` | Optional | cannot be null | [HTTP Connection](http_connection-defs-http_connection_authentication-properties-username.md "classpath:/schemas/connection/http_connection.schema.json#/$defs/http_connection_authentication/properties/username") |
| [password](#password) | `string` | Optional | cannot be null | [HTTP Connection](http_connection-defs-http_connection_authentication-properties-password.md "classpath:/schemas/connection/http_connection.schema.json#/$defs/http_connection_authentication/properties/password") |

## username



`username`

*   is optional

*   Type: `string`

*   cannot be null

*   defined in: [HTTP Connection](http_connection-defs-http_connection_authentication-properties-username.md "classpath:/schemas/connection/http_connection.schema.json#/$defs/http_connection_authentication/properties/username")

### username Type

`string`

## password



`password`

*   is optional

*   Type: `string`

*   cannot be null

*   defined in: [HTTP Connection](http_connection-defs-http_connection_authentication-properties-password.md "classpath:/schemas/connection/http_connection.schema.json#/$defs/http_connection_authentication/properties/password")

### password Type

`string`
