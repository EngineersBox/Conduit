# HTTP Connection Schema

```txt
classpath:/schemas/connection/http_connection.schema.json
```

HTTP connection schema with SSL and context support parameters

| Abstract            | Extensible | Status         | Identifiable | Custom Properties | Additional Properties | Access Restrictions | Defined In                                                                                              |
| :------------------ | :--------- | :------------- | :----------- | :---------------- | :-------------------- | :------------------ | :------------------------------------------------------------------------------------------------------ |
| Can be instantiated | No         | Unknown status | No           | Forbidden         | Forbidden             | none                | [http\_connection.schema.json](../../out/connection/http_connection.schema.json "open original schema") |

## HTTP Connection Type

`object` ([HTTP Connection](http_connection.md))

# HTTP Connection Properties

| Property                           | Type      | Required | Nullable       | Defined by                                                                                                                                                       |
| :--------------------------------- | :-------- | :------- | :------------- | :--------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| [uri](#uri)                        | `string`  | Required | cannot be null | [HTTP Connection](http_connection-properties-uri.md "classpath:/schemas/connection/http_connection.schema.json#/properties/uri")                                 |
| [authentication](#authentication)  | `object`  | Optional | cannot be null | [HTTP Connection](http_connection-defs-http_connection_authentication.md "classpath:/schemas/connection/http_connection.schema.json#/properties/authentication") |
| [ssl\_context](#ssl_context)       | `object`  | Optional | cannot be null | [HTTP Connection](http_connection-defs-http_connection_ssl_context.md "classpath:/schemas/connection/http_connection.schema.json#/properties/ssl_context")       |
| [ssl\_parameters](#ssl_parameters) | `object`  | Optional | cannot be null | [HTTP Connection](http_connection-defs-http_connection_ssl_parameters.md "classpath:/schemas/connection/http_connection.schema.json#/properties/ssl_parameters") |
| [timeout](#timeout)                | `string`  | Optional | cannot be null | [HTTP Connection](http_connection-properties-timeout.md "classpath:/schemas/connection/http_connection.schema.json#/properties/timeout")                         |
| [redirect](#redirect)              | `string`  | Optional | cannot be null | [HTTP Connection](http_connection-properties-redirect.md "classpath:/schemas/connection/http_connection.schema.json#/properties/redirect")                       |
| [priority](#priority)              | `integer` | Optional | cannot be null | [HTTP Connection](http_connection-properties-priority.md "classpath:/schemas/connection/http_connection.schema.json#/properties/priority")                       |
| [version](#version)                | `string`  | Optional | cannot be null | [HTTP Connection](http_connection-properties-version.md "classpath:/schemas/connection/http_connection.schema.json#/properties/version")                         |
| [proxy](#proxy)                    | `string`  | Optional | cannot be null | [HTTP Connection](http_connection-properties-proxy.md "classpath:/schemas/connection/http_connection.schema.json#/properties/proxy")                             |
| [local\_address](#local_address)   | `string`  | Optional | cannot be null | [HTTP Connection](http_connection-properties-local_address.md "classpath:/schemas/connection/http_connection.schema.json#/properties/local_address")             |

## uri



`uri`

*   is required

*   Type: `string`

*   cannot be null

*   defined in: [HTTP Connection](http_connection-properties-uri.md "classpath:/schemas/connection/http_connection.schema.json#/properties/uri")

### uri Type

`string`

## authentication



`authentication`

*   is optional

*   Type: `object` ([Details](http_connection-defs-http_connection_authentication.md))

*   cannot be null

*   defined in: [HTTP Connection](http_connection-defs-http_connection_authentication.md "classpath:/schemas/connection/http_connection.schema.json#/properties/authentication")

### authentication Type

`object` ([Details](http_connection-defs-http_connection_authentication.md))

## ssl\_context



`ssl_context`

*   is optional

*   Type: `object` ([Details](http_connection-defs-http_connection_ssl_context.md))

*   cannot be null

*   defined in: [HTTP Connection](http_connection-defs-http_connection_ssl_context.md "classpath:/schemas/connection/http_connection.schema.json#/properties/ssl_context")

### ssl\_context Type

`object` ([Details](http_connection-defs-http_connection_ssl_context.md))

## ssl\_parameters



`ssl_parameters`

*   is optional

*   Type: `object` ([Details](http_connection-defs-http_connection_ssl_parameters.md))

*   cannot be null

*   defined in: [HTTP Connection](http_connection-defs-http_connection_ssl_parameters.md "classpath:/schemas/connection/http_connection.schema.json#/properties/ssl_parameters")

### ssl\_parameters Type

`object` ([Details](http_connection-defs-http_connection_ssl_parameters.md))

## timeout



`timeout`

*   is optional

*   Type: `string`

*   cannot be null

*   defined in: [HTTP Connection](http_connection-properties-timeout.md "classpath:/schemas/connection/http_connection.schema.json#/properties/timeout")

### timeout Type

`string`

## redirect



`redirect`

*   is optional

*   Type: `string`

*   cannot be null

*   defined in: [HTTP Connection](http_connection-properties-redirect.md "classpath:/schemas/connection/http_connection.schema.json#/properties/redirect")

### redirect Type

`string`

## priority



`priority`

*   is optional

*   Type: `integer`

*   cannot be null

*   defined in: [HTTP Connection](http_connection-properties-priority.md "classpath:/schemas/connection/http_connection.schema.json#/properties/priority")

### priority Type

`integer`

## version



`version`

*   is optional

*   Type: `string`

*   cannot be null

*   defined in: [HTTP Connection](http_connection-properties-version.md "classpath:/schemas/connection/http_connection.schema.json#/properties/version")

### version Type

`string`

## proxy



`proxy`

*   is optional

*   Type: `string`

*   cannot be null

*   defined in: [HTTP Connection](http_connection-properties-proxy.md "classpath:/schemas/connection/http_connection.schema.json#/properties/proxy")

### proxy Type

`string`

## local\_address



`local_address`

*   is optional

*   Type: `string`

*   cannot be null

*   defined in: [HTTP Connection](http_connection-properties-local_address.md "classpath:/schemas/connection/http_connection.schema.json#/properties/local_address")

### local\_address Type

`string`

### local\_address Constraints

**IPv4**: the string must be an IPv4 address (dotted quad), according to [RFC 2673, section 3.2](https://tools.ietf.org/html/rfc2673 "check the specification")

# HTTP Connection Definitions

## Definitions group http\_connection\_authentication

Reference this group by using

```json
{"$ref":"classpath:/schemas/connection/http_connection.schema.json#/$defs/http_connection_authentication"}
```

| Property              | Type     | Required | Nullable       | Defined by                                                                                                                                                                                                          |
| :-------------------- | :------- | :------- | :------------- | :------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| [username](#username) | `string` | Optional | cannot be null | [HTTP Connection](http_connection-defs-http_connection_authentication-properties-username.md "classpath:/schemas/connection/http_connection.schema.json#/$defs/http_connection_authentication/properties/username") |
| [password](#password) | `string` | Optional | cannot be null | [HTTP Connection](http_connection-defs-http_connection_authentication-properties-password.md "classpath:/schemas/connection/http_connection.schema.json#/$defs/http_connection_authentication/properties/password") |

### username



`username`

*   is optional

*   Type: `string`

*   cannot be null

*   defined in: [HTTP Connection](http_connection-defs-http_connection_authentication-properties-username.md "classpath:/schemas/connection/http_connection.schema.json#/$defs/http_connection_authentication/properties/username")

#### username Type

`string`

### password



`password`

*   is optional

*   Type: `string`

*   cannot be null

*   defined in: [HTTP Connection](http_connection-defs-http_connection_authentication-properties-password.md "classpath:/schemas/connection/http_connection.schema.json#/$defs/http_connection_authentication/properties/password")

#### password Type

`string`

## Definitions group http\_connection\_ssl\_context

Reference this group by using

```json
{"$ref":"classpath:/schemas/connection/http_connection.schema.json#/$defs/http_connection_ssl_context"}
```

| Property                                              | Type     | Required | Nullable       | Defined by                                                                                                                                                                                                                                  |
| :---------------------------------------------------- | :------- | :------- | :------------- | :------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| [protocol](#protocol)                                 | `string` | Optional | cannot be null | [HTTP Connection](http_connection-defs-http_connection_ssl_context-properties-protocol.md "classpath:/schemas/connection/http_connection.schema.json#/$defs/http_connection_ssl_context/properties/protocol")                               |
| [provider](#provider)                                 | `string` | Optional | cannot be null | [HTTP Connection](http_connection-defs-http_connection_ssl_context-properties-provider.md "classpath:/schemas/connection/http_connection.schema.json#/$defs/http_connection_ssl_context/properties/provider")                               |
| [key\_manager](#key_manager)                          | `object` | Optional | cannot be null | [HTTP Connection](http_connection-defs-http_connection_ssl_context_manager.md "classpath:/schemas/connection/http_connection.schema.json#/$defs/http_connection_ssl_context/properties/key_manager")                                        |
| [trust\_manager](#trust_manager)                      | `object` | Optional | cannot be null | [HTTP Connection](http_connection-defs-http_connection_ssl_context_manager.md "classpath:/schemas/connection/http_connection.schema.json#/$defs/http_connection_ssl_context/properties/trust_manager")                                      |
| [secure\_random\_algorithm](#secure_random_algorithm) | `string` | Optional | cannot be null | [HTTP Connection](http_connection-defs-http_connection_ssl_context-properties-secure_random_algorithm.md "classpath:/schemas/connection/http_connection.schema.json#/$defs/http_connection_ssl_context/properties/secure_random_algorithm") |
| [secure\_random\_provider](#secure_random_provider)   | `string` | Optional | cannot be null | [HTTP Connection](http_connection-defs-http_connection_ssl_context-properties-secure_random_provider.md "classpath:/schemas/connection/http_connection.schema.json#/$defs/http_connection_ssl_context/properties/secure_random_provider")   |

### protocol



`protocol`

*   is optional

*   Type: `string`

*   cannot be null

*   defined in: [HTTP Connection](http_connection-defs-http_connection_ssl_context-properties-protocol.md "classpath:/schemas/connection/http_connection.schema.json#/$defs/http_connection_ssl_context/properties/protocol")

#### protocol Type

`string`

### provider



`provider`

*   is optional

*   Type: `string`

*   cannot be null

*   defined in: [HTTP Connection](http_connection-defs-http_connection_ssl_context-properties-provider.md "classpath:/schemas/connection/http_connection.schema.json#/$defs/http_connection_ssl_context/properties/provider")

#### provider Type

`string`

### key\_manager



`key_manager`

*   is optional

*   Type: `object` ([Details](http_connection-defs-http_connection_ssl_context_manager.md))

*   cannot be null

*   defined in: [HTTP Connection](http_connection-defs-http_connection_ssl_context_manager.md "classpath:/schemas/connection/http_connection.schema.json#/$defs/http_connection_ssl_context/properties/key_manager")

#### key\_manager Type

`object` ([Details](http_connection-defs-http_connection_ssl_context_manager.md))

### trust\_manager



`trust_manager`

*   is optional

*   Type: `object` ([Details](http_connection-defs-http_connection_ssl_context_manager.md))

*   cannot be null

*   defined in: [HTTP Connection](http_connection-defs-http_connection_ssl_context_manager.md "classpath:/schemas/connection/http_connection.schema.json#/$defs/http_connection_ssl_context/properties/trust_manager")

#### trust\_manager Type

`object` ([Details](http_connection-defs-http_connection_ssl_context_manager.md))

### secure\_random\_algorithm



`secure_random_algorithm`

*   is optional

*   Type: `string`

*   cannot be null

*   defined in: [HTTP Connection](http_connection-defs-http_connection_ssl_context-properties-secure_random_algorithm.md "classpath:/schemas/connection/http_connection.schema.json#/$defs/http_connection_ssl_context/properties/secure_random_algorithm")

#### secure\_random\_algorithm Type

`string`

### secure\_random\_provider



`secure_random_provider`

*   is optional

*   Type: `string`

*   cannot be null

*   defined in: [HTTP Connection](http_connection-defs-http_connection_ssl_context-properties-secure_random_provider.md "classpath:/schemas/connection/http_connection.schema.json#/$defs/http_connection_ssl_context/properties/secure_random_provider")

#### secure\_random\_provider Type

`string`

## Definitions group http\_connection\_ssl\_context\_manager

Reference this group by using

```json
{"$ref":"classpath:/schemas/connection/http_connection.schema.json#/$defs/http_connection_ssl_context_manager"}
```

| Property                                    | Type     | Required | Nullable       | Defined by                                                                                                                                                                                                                                        |
| :------------------------------------------ | :------- | :------- | :------------- | :------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| [key\_store\_path](#key_store_path)         | `string` | Optional | cannot be null | [HTTP Connection](http_connection-defs-http_connection_ssl_context_manager-properties-key_store_path.md "classpath:/schemas/connection/http_connection.schema.json#/$defs/http_connection_ssl_context_manager/properties/key_store_path")         |
| [key\_store\_password](#key_store_password) | `string` | Optional | cannot be null | [HTTP Connection](http_connection-defs-http_connection_ssl_context_manager-properties-key_store_password.md "classpath:/schemas/connection/http_connection.schema.json#/$defs/http_connection_ssl_context_manager/properties/key_store_password") |
| [algorithm](#algorithm)                     | `string` | Optional | cannot be null | [HTTP Connection](http_connection-defs-http_connection_ssl_context_manager-properties-algorithm.md "classpath:/schemas/connection/http_connection.schema.json#/$defs/http_connection_ssl_context_manager/properties/algorithm")                   |
| [provider](#provider-1)                     | `string` | Optional | cannot be null | [HTTP Connection](http_connection-defs-http_connection_ssl_context_manager-properties-provider.md "classpath:/schemas/connection/http_connection.schema.json#/$defs/http_connection_ssl_context_manager/properties/provider")                     |

### key\_store\_path



`key_store_path`

*   is optional

*   Type: `string`

*   cannot be null

*   defined in: [HTTP Connection](http_connection-defs-http_connection_ssl_context_manager-properties-key_store_path.md "classpath:/schemas/connection/http_connection.schema.json#/$defs/http_connection_ssl_context_manager/properties/key_store_path")

#### key\_store\_path Type

`string`

### key\_store\_password



`key_store_password`

*   is optional

*   Type: `string`

*   cannot be null

*   defined in: [HTTP Connection](http_connection-defs-http_connection_ssl_context_manager-properties-key_store_password.md "classpath:/schemas/connection/http_connection.schema.json#/$defs/http_connection_ssl_context_manager/properties/key_store_password")

#### key\_store\_password Type

`string`

### algorithm



`algorithm`

*   is optional

*   Type: `string`

*   cannot be null

*   defined in: [HTTP Connection](http_connection-defs-http_connection_ssl_context_manager-properties-algorithm.md "classpath:/schemas/connection/http_connection.schema.json#/$defs/http_connection_ssl_context_manager/properties/algorithm")

#### algorithm Type

`string`

### provider



`provider`

*   is optional

*   Type: `string`

*   cannot be null

*   defined in: [HTTP Connection](http_connection-defs-http_connection_ssl_context_manager-properties-provider.md "classpath:/schemas/connection/http_connection.schema.json#/$defs/http_connection_ssl_context_manager/properties/provider")

#### provider Type

`string`

## Definitions group http\_connection\_ssl\_parameters

Reference this group by using

```json
{"$ref":"classpath:/schemas/connection/http_connection.schema.json#/$defs/http_connection_ssl_parameters"}
```

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

### cipher\_suites



`cipher_suites`

*   is optional

*   Type: `string[]`

*   cannot be null

*   defined in: [HTTP Connection](http_connection-defs-http_connection_ssl_parameters-properties-cipher_suites.md "classpath:/schemas/connection/http_connection.schema.json#/$defs/http_connection_ssl_parameters/properties/cipher_suites")

#### cipher\_suites Type

`string[]`

### application\_protocols



`application_protocols`

*   is optional

*   Type: `string[]`

*   cannot be null

*   defined in: [HTTP Connection](http_connection-defs-http_connection_ssl_parameters-properties-application_protocols.md "classpath:/schemas/connection/http_connection.schema.json#/$defs/http_connection_ssl_parameters/properties/application_protocols")

#### application\_protocols Type

`string[]`

### protocols



`protocols`

*   is optional

*   Type: `string[]`

*   cannot be null

*   defined in: [HTTP Connection](http_connection-defs-http_connection_ssl_parameters-properties-protocols.md "classpath:/schemas/connection/http_connection.schema.json#/$defs/http_connection_ssl_parameters/properties/protocols")

#### protocols Type

`string[]`

### want\_client\_auth



`want_client_auth`

*   is optional

*   Type: `boolean`

*   cannot be null

*   defined in: [HTTP Connection](http_connection-defs-http_connection_ssl_parameters-properties-want_client_auth.md "classpath:/schemas/connection/http_connection.schema.json#/$defs/http_connection_ssl_parameters/properties/want_client_auth")

#### want\_client\_auth Type

`boolean`

### need\_client\_auth



`need_client_auth`

*   is optional

*   Type: `boolean`

*   cannot be null

*   defined in: [HTTP Connection](http_connection-defs-http_connection_ssl_parameters-properties-need_client_auth.md "classpath:/schemas/connection/http_connection.schema.json#/$defs/http_connection_ssl_parameters/properties/need_client_auth")

#### need\_client\_auth Type

`boolean`

### algorithm\_constraints



`algorithm_constraints`

*   is optional

*   Type: `string`

*   cannot be null

*   defined in: [HTTP Connection](http_connection-defs-http_connection_ssl_parameters-properties-algorithm_constraints.md "classpath:/schemas/connection/http_connection.schema.json#/$defs/http_connection_ssl_parameters/properties/algorithm_constraints")

#### algorithm\_constraints Type

`string`

### endpoint\_identification\_algorithm



`endpoint_identification_algorithm`

*   is optional

*   Type: `string`

*   cannot be null

*   defined in: [HTTP Connection](http_connection-defs-http_connection_ssl_parameters-properties-endpoint_identification_algorithm.md "classpath:/schemas/connection/http_connection.schema.json#/$defs/http_connection_ssl_parameters/properties/endpoint_identification_algorithm")

#### endpoint\_identification\_algorithm Type

`string`

### use\_cipher\_suites\_order



`use_cipher_suites_order`

*   is optional

*   Type: `boolean`

*   cannot be null

*   defined in: [HTTP Connection](http_connection-defs-http_connection_ssl_parameters-properties-use_cipher_suites_order.md "classpath:/schemas/connection/http_connection.schema.json#/$defs/http_connection_ssl_parameters/properties/use_cipher_suites_order")

#### use\_cipher\_suites\_order Type

`boolean`

### enable\_retransmissions



`enable_retransmissions`

*   is optional

*   Type: `boolean`

*   cannot be null

*   defined in: [HTTP Connection](http_connection-defs-http_connection_ssl_parameters-properties-enable_retransmissions.md "classpath:/schemas/connection/http_connection.schema.json#/$defs/http_connection_ssl_parameters/properties/enable_retransmissions")

#### enable\_retransmissions Type

`boolean`

### maximum\_packet\_size



`maximum_packet_size`

*   is optional

*   Type: `integer`

*   cannot be null

*   defined in: [HTTP Connection](http_connection-defs-http_connection_ssl_parameters-properties-maximum_packet_size.md "classpath:/schemas/connection/http_connection.schema.json#/$defs/http_connection_ssl_parameters/properties/maximum_packet_size")

#### maximum\_packet\_size Type

`integer`

### signature\_schemes



`signature_schemes`

*   is optional

*   Type: `string[]`

*   cannot be null

*   defined in: [HTTP Connection](http_connection-defs-http_connection_ssl_parameters-properties-signature_schemes.md "classpath:/schemas/connection/http_connection.schema.json#/$defs/http_connection_ssl_parameters/properties/signature_schemes")

#### signature\_schemes Type

`string[]`

### named\_groups



`named_groups`

*   is optional

*   Type: `string[]`

*   cannot be null

*   defined in: [HTTP Connection](http_connection-defs-http_connection_ssl_parameters-properties-named_groups.md "classpath:/schemas/connection/http_connection.schema.json#/$defs/http_connection_ssl_parameters/properties/named_groups")

#### named\_groups Type

`string[]`
