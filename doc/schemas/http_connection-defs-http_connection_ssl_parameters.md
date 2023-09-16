# Untitled object in HTTP Connection Schema

```txt
classpath:/schemas/connection/http_connection.schema.json#/$defs/http_connection_ssl_parameters
```



| Abstract            | Extensible | Status         | Identifiable | Custom Properties | Additional Properties | Access Restrictions | Defined In                                                                                                |
| :------------------ | :--------- | :------------- | :----------- | :---------------- | :-------------------- | :------------------ | :-------------------------------------------------------------------------------------------------------- |
| Can be instantiated | No         | Unknown status | No           | Forbidden         | Forbidden             | none                | [http\_connection.schema.json\*](../../out/connection/http_connection.schema.json "open original schema") |

## http\_connection\_ssl\_parameters Type

`object` ([Details](http_connection-defs-http_connection_ssl_parameters.md))

# http\_connection\_ssl\_parameters Properties

| Property                                                                  | Type      | Required | Nullable       | Defined by                                                                                                                                                                                                                                                            |
| :------------------------------------------------------------------------ | :-------- | :------- | :------------- | :-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| [cipher\_suites](#cipher_suites)                                          | `array`   | Optional | cannot be null | [HTTP Connection](http_connection-defs-http_connection_ssl_parameters-properties-cipher_suites.md "classpath:/schemas/connection/http_connection.schema.json#/$defs/http_connection_ssl_parameters/properties/cipher_suites")                                         |
| [application\_protocols](#application_protocols)                          | `array`   | Optional | cannot be null | [HTTP Connection](http_connection-defs-http_connection_ssl_parameters-properties-application_protocols.md "classpath:/schemas/connection/http_connection.schema.json#/$defs/http_connection_ssl_parameters/properties/application_protocols")                         |
| [protocols](#protocols)                                                   | `array`   | Optional | cannot be null | [HTTP Connection](http_connection-defs-http_connection_ssl_parameters-properties-protocols.md "classpath:/schemas/connection/http_connection.schema.json#/$defs/http_connection_ssl_parameters/properties/protocols")                                                 |
| [want\_client\_auth](#want_client_auth)                                   | `boolean` | Optional | cannot be null | [HTTP Connection](http_connection-defs-http_connection_ssl_parameters-properties-want_client_auth.md "classpath:/schemas/connection/http_connection.schema.json#/$defs/http_connection_ssl_parameters/properties/want_client_auth")                                   |
| [need\_client\_auth](#need_client_auth)                                   | `boolean` | Optional | cannot be null | [HTTP Connection](http_connection-defs-http_connection_ssl_parameters-properties-need_client_auth.md "classpath:/schemas/connection/http_connection.schema.json#/$defs/http_connection_ssl_parameters/properties/need_client_auth")                                   |
| [algorithm\_constraints](#algorithm_constraints)                          | `string`  | Optional | cannot be null | [HTTP Connection](http_connection-defs-http_connection_ssl_parameters-properties-algorithm_constraints.md "classpath:/schemas/connection/http_connection.schema.json#/$defs/http_connection_ssl_parameters/properties/algorithm_constraints")                         |
| [endpoint\_identification\_algorithm](#endpoint_identification_algorithm) | `string`  | Optional | cannot be null | [HTTP Connection](http_connection-defs-http_connection_ssl_parameters-properties-endpoint_identification_algorithm.md "classpath:/schemas/connection/http_connection.schema.json#/$defs/http_connection_ssl_parameters/properties/endpoint_identification_algorithm") |
| [use\_cipher\_suites\_order](#use_cipher_suites_order)                    | `boolean` | Optional | cannot be null | [HTTP Connection](http_connection-defs-http_connection_ssl_parameters-properties-use_cipher_suites_order.md "classpath:/schemas/connection/http_connection.schema.json#/$defs/http_connection_ssl_parameters/properties/use_cipher_suites_order")                     |
| [enable\_retransmissions](#enable_retransmissions)                        | `boolean` | Optional | cannot be null | [HTTP Connection](http_connection-defs-http_connection_ssl_parameters-properties-enable_retransmissions.md "classpath:/schemas/connection/http_connection.schema.json#/$defs/http_connection_ssl_parameters/properties/enable_retransmissions")                       |
| [maximum\_packet\_size](#maximum_packet_size)                             | `integer` | Optional | cannot be null | [HTTP Connection](http_connection-defs-http_connection_ssl_parameters-properties-maximum_packet_size.md "classpath:/schemas/connection/http_connection.schema.json#/$defs/http_connection_ssl_parameters/properties/maximum_packet_size")                             |
| [signature\_schemes](#signature_schemes)                                  | `array`   | Optional | cannot be null | [HTTP Connection](http_connection-defs-http_connection_ssl_parameters-properties-signature_schemes.md "classpath:/schemas/connection/http_connection.schema.json#/$defs/http_connection_ssl_parameters/properties/signature_schemes")                                 |
| [named\_groups](#named_groups)                                            | `array`   | Optional | cannot be null | [HTTP Connection](http_connection-defs-http_connection_ssl_parameters-properties-named_groups.md "classpath:/schemas/connection/http_connection.schema.json#/$defs/http_connection_ssl_parameters/properties/named_groups")                                           |

## cipher\_suites



`cipher_suites`

*   is optional

*   Type: `string[]`

*   cannot be null

*   defined in: [HTTP Connection](http_connection-defs-http_connection_ssl_parameters-properties-cipher_suites.md "classpath:/schemas/connection/http_connection.schema.json#/$defs/http_connection_ssl_parameters/properties/cipher_suites")

### cipher\_suites Type

`string[]`

## application\_protocols



`application_protocols`

*   is optional

*   Type: `string[]`

*   cannot be null

*   defined in: [HTTP Connection](http_connection-defs-http_connection_ssl_parameters-properties-application_protocols.md "classpath:/schemas/connection/http_connection.schema.json#/$defs/http_connection_ssl_parameters/properties/application_protocols")

### application\_protocols Type

`string[]`

## protocols



`protocols`

*   is optional

*   Type: `string[]`

*   cannot be null

*   defined in: [HTTP Connection](http_connection-defs-http_connection_ssl_parameters-properties-protocols.md "classpath:/schemas/connection/http_connection.schema.json#/$defs/http_connection_ssl_parameters/properties/protocols")

### protocols Type

`string[]`

## want\_client\_auth



`want_client_auth`

*   is optional

*   Type: `boolean`

*   cannot be null

*   defined in: [HTTP Connection](http_connection-defs-http_connection_ssl_parameters-properties-want_client_auth.md "classpath:/schemas/connection/http_connection.schema.json#/$defs/http_connection_ssl_parameters/properties/want_client_auth")

### want\_client\_auth Type

`boolean`

## need\_client\_auth



`need_client_auth`

*   is optional

*   Type: `boolean`

*   cannot be null

*   defined in: [HTTP Connection](http_connection-defs-http_connection_ssl_parameters-properties-need_client_auth.md "classpath:/schemas/connection/http_connection.schema.json#/$defs/http_connection_ssl_parameters/properties/need_client_auth")

### need\_client\_auth Type

`boolean`

## algorithm\_constraints



`algorithm_constraints`

*   is optional

*   Type: `string`

*   cannot be null

*   defined in: [HTTP Connection](http_connection-defs-http_connection_ssl_parameters-properties-algorithm_constraints.md "classpath:/schemas/connection/http_connection.schema.json#/$defs/http_connection_ssl_parameters/properties/algorithm_constraints")

### algorithm\_constraints Type

`string`

## endpoint\_identification\_algorithm



`endpoint_identification_algorithm`

*   is optional

*   Type: `string`

*   cannot be null

*   defined in: [HTTP Connection](http_connection-defs-http_connection_ssl_parameters-properties-endpoint_identification_algorithm.md "classpath:/schemas/connection/http_connection.schema.json#/$defs/http_connection_ssl_parameters/properties/endpoint_identification_algorithm")

### endpoint\_identification\_algorithm Type

`string`

## use\_cipher\_suites\_order



`use_cipher_suites_order`

*   is optional

*   Type: `boolean`

*   cannot be null

*   defined in: [HTTP Connection](http_connection-defs-http_connection_ssl_parameters-properties-use_cipher_suites_order.md "classpath:/schemas/connection/http_connection.schema.json#/$defs/http_connection_ssl_parameters/properties/use_cipher_suites_order")

### use\_cipher\_suites\_order Type

`boolean`

## enable\_retransmissions



`enable_retransmissions`

*   is optional

*   Type: `boolean`

*   cannot be null

*   defined in: [HTTP Connection](http_connection-defs-http_connection_ssl_parameters-properties-enable_retransmissions.md "classpath:/schemas/connection/http_connection.schema.json#/$defs/http_connection_ssl_parameters/properties/enable_retransmissions")

### enable\_retransmissions Type

`boolean`

## maximum\_packet\_size



`maximum_packet_size`

*   is optional

*   Type: `integer`

*   cannot be null

*   defined in: [HTTP Connection](http_connection-defs-http_connection_ssl_parameters-properties-maximum_packet_size.md "classpath:/schemas/connection/http_connection.schema.json#/$defs/http_connection_ssl_parameters/properties/maximum_packet_size")

### maximum\_packet\_size Type

`integer`

## signature\_schemes



`signature_schemes`

*   is optional

*   Type: `string[]`

*   cannot be null

*   defined in: [HTTP Connection](http_connection-defs-http_connection_ssl_parameters-properties-signature_schemes.md "classpath:/schemas/connection/http_connection.schema.json#/$defs/http_connection_ssl_parameters/properties/signature_schemes")

### signature\_schemes Type

`string[]`

## named\_groups



`named_groups`

*   is optional

*   Type: `string[]`

*   cannot be null

*   defined in: [HTTP Connection](http_connection-defs-http_connection_ssl_parameters-properties-named_groups.md "classpath:/schemas/connection/http_connection.schema.json#/$defs/http_connection_ssl_parameters/properties/named_groups")

### named\_groups Type

`string[]`
