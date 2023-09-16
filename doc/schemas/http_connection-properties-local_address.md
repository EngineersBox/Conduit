# Untitled string in HTTP Connection Schema

```txt
classpath:/schemas/connection/http_connection.schema.json#/properties/local_address
```



| Abstract            | Extensible | Status         | Identifiable            | Custom Properties | Additional Properties | Access Restrictions | Defined In                                                                                                |
| :------------------ | :--------- | :------------- | :---------------------- | :---------------- | :-------------------- | :------------------ | :-------------------------------------------------------------------------------------------------------- |
| Can be instantiated | No         | Unknown status | Unknown identifiability | Forbidden         | Allowed               | none                | [http\_connection.schema.json\*](../../out/connection/http_connection.schema.json "open original schema") |

## local\_address Type

`string`

## local\_address Constraints

**IPv4**: the string must be an IPv4 address (dotted quad), according to [RFC 2673, section 3.2](https://tools.ietf.org/html/rfc2673 "check the specification")
