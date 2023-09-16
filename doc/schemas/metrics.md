# Metrics Schema

```txt
classpath:/schemas/metrics.schema.json
```

Metrics ingestion schema for creating Riemann events

| Abstract            | Extensible | Status         | Identifiable | Custom Properties | Additional Properties | Access Restrictions | Defined In                                                                  |
| :------------------ | :--------- | :------------- | :----------- | :---------------- | :-------------------- | :------------------ | :-------------------------------------------------------------------------- |
| Can be instantiated | No         | Unknown status | No           | Forbidden         | Forbidden             | none                | [metrics.schema.json](../../out/metrics.schema.json "open original schema") |

## Metrics Type

`object` ([Metrics](metrics.md))

# Metrics Properties

| Property                           | Type          | Required | Nullable       | Defined by                                                                                                                |
| :--------------------------------- | :------------ | :------- | :------------- | :------------------------------------------------------------------------------------------------------------------------ |
| [configuration](#configuration)    | `object`      | Optional | cannot be null | [Metrics](metrics-defs-configuration.md "classpath:/schemas/metrics.schema.json#/properties/configuration")               |
| [connection](#connection)          | `object`      | Optional | cannot be null | [Metrics](metrics-properties-connection.md "classpath:/schemas/connection/connection.schema.json#/properties/connection") |
| [event\_template](#event_template) | `object`      | Optional | cannot be null | [Metrics](metrics-properties-event_template.md "classpath:/schemas/metrics.schema.json#/properties/event_template")       |
| [handler](#handler)                | `string`      | Optional | cannot be null | [Metrics](metrics-properties-handler.md "classpath:/schemas/metrics.schema.json#/properties/handler")                     |
| [metrics](#metrics)                | `array`       | Optional | cannot be null | [Metrics](metrics-properties-metrics.md "classpath:/schemas/metrics.schema.json#/properties/metrics")                     |
| [extensions](#extensions)          | `object`      | Optional | cannot be null | [Metrics](metrics-properties-extensions.md "classpath:/schemas/metrics.schema.json#/properties/extensions")               |
| [required](#required)              | Not specified | Optional | cannot be null | [Metrics](metrics-properties-required.md "classpath:/schemas/metrics.schema.json#/properties/required")                   |

## configuration



`configuration`

*   is optional

*   Type: `object` ([Details](metrics-defs-configuration.md))

*   cannot be null

*   defined in: [Metrics](metrics-defs-configuration.md "classpath:/schemas/metrics.schema.json#/properties/configuration")

### configuration Type

`object` ([Details](metrics-defs-configuration.md))

## connection

Connection types and attributes for connecting to a metrics source

`connection`

*   is optional

*   Type: `object` ([Connection](metrics-properties-connection.md))

*   cannot be null

*   defined in: [Metrics](metrics-properties-connection.md "classpath:/schemas/connection/connection.schema.json#/properties/connection")

### connection Type

`object` ([Connection](metrics-properties-connection.md))

## event\_template



`event_template`

*   is optional

*   Type: `object` ([Details](metrics-properties-event_template.md))

*   cannot be null

*   defined in: [Metrics](metrics-properties-event_template.md "classpath:/schemas/metrics.schema.json#/properties/event_template")

### event\_template Type

`object` ([Details](metrics-properties-event_template.md))

## handler



`handler`

*   is optional

*   Type: `string`

*   cannot be null

*   defined in: [Metrics](metrics-properties-handler.md "classpath:/schemas/metrics.schema.json#/properties/handler")

### handler Type

`string`

## metrics



`metrics`

*   is optional

*   Type: `array`

*   cannot be null

*   defined in: [Metrics](metrics-properties-metrics.md "classpath:/schemas/metrics.schema.json#/properties/metrics")

### metrics Type

`array`

## extensions



`extensions`

*   is optional

*   Type: `object` ([Details](metrics-properties-extensions.md))

*   cannot be null

*   defined in: [Metrics](metrics-properties-extensions.md "classpath:/schemas/metrics.schema.json#/properties/extensions")

### extensions Type

`object` ([Details](metrics-properties-extensions.md))

## required



`required`

*   is optional

*   Type: unknown

*   cannot be null

*   defined in: [Metrics](metrics-properties-required.md "classpath:/schemas/metrics.schema.json#/properties/required")

### required Type

unknown

# Metrics Definitions

## Definitions group configuration

Reference this group by using

```json
{"$ref":"classpath:/schemas/metrics.schema.json#/$defs/configuration"}
```

| Property                               | Type     | Required | Nullable       | Defined by                                                                                                                                                     |
| :------------------------------------- | :------- | :------- | :------------- | :------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| [mapping\_provider](#mapping_provider) | `string` | Optional | cannot be null | [Metrics](metrics-defs-configuration-properties-mapping_provider.md "classpath:/schemas/metrics.schema.json#/$defs/configuration/properties/mapping_provider") |
| [json\_provider](#json_provider)       | `string` | Optional | cannot be null | [Metrics](metrics-defs-configuration-properties-json_provider.md "classpath:/schemas/metrics.schema.json#/$defs/configuration/properties/json_provider")       |

### mapping\_provider



`mapping_provider`

*   is optional

*   Type: `string`

*   cannot be null

*   defined in: [Metrics](metrics-defs-configuration-properties-mapping_provider.md "classpath:/schemas/metrics.schema.json#/$defs/configuration/properties/mapping_provider")

#### mapping\_provider Type

`string`

### json\_provider



`json_provider`

*   is optional

*   Type: `string`

*   cannot be null

*   defined in: [Metrics](metrics-defs-configuration-properties-json_provider.md "classpath:/schemas/metrics.schema.json#/$defs/configuration/properties/json_provider")

#### json\_provider Type

`string`

## Definitions group metric\_structure

Reference this group by using

```json
{"$ref":"classpath:/schemas/metrics.schema.json#/$defs/metric_structure"}
```

| Property | Type | Required | Nullable | Defined by |
| :------- | :--- | :------- | :------- | :--------- |

## Definitions group primitive\_metric\_structure

Reference this group by using

```json
{"$ref":"classpath:/schemas/metrics.schema.json#/$defs/primitive_metric_structure"}
```

| Property      | Type     | Required | Nullable       | Defined by                                                                                                                                                       |
| :------------ | :------- | :------- | :------------- | :--------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| [type](#type) | `string` | Required | cannot be null | [Metrics](metrics-defs-primitive_metric_structure-properties-type.md "classpath:/schemas/metrics.schema.json#/$defs/primitive_metric_structure/properties/type") |

### type



`type`

*   is required

*   Type: `string`

*   cannot be null

*   defined in: [Metrics](metrics-defs-primitive_metric_structure-properties-type.md "classpath:/schemas/metrics.schema.json#/$defs/primitive_metric_structure/properties/type")

#### type Type

`string`

#### type Constraints

**enum**: the value of this property must be equal to one of the following values:

| Value       | Explanation |
| :---------- | :---------- |
| `"STRING"`  |             |
| `"FLOAT"`   |             |
| `"DOUBLE"`  |             |
| `"INTEGER"` |             |
| `"BOOLEAN"` |             |
| `"INFER"`   |             |

## Definitions group container\_metric\_structure

Reference this group by using

```json
{"$ref":"classpath:/schemas/metrics.schema.json#/$defs/container_metric_structure"}
```

| Property                | Type     | Required | Nullable       | Defined by                                                                                                                                                                 |
| :---------------------- | :------- | :------- | :------------- | :------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| [type](#type-1)         | `string` | Required | cannot be null | [Metrics](metrics-defs-container_metric_structure-properties-type.md "classpath:/schemas/metrics.schema.json#/$defs/container_metric_structure/properties/type")           |
| [structure](#structure) | Merged   | Required | cannot be null | [Metrics](metrics-defs-container_metric_structure-properties-structure.md "classpath:/schemas/metrics.schema.json#/$defs/container_metric_structure/properties/structure") |
| [suffixes](#suffixes)   | `array`  | Optional | cannot be null | [Metrics](metrics-defs-container_metric_structure-properties-suffixes.md "classpath:/schemas/metrics.schema.json#/$defs/container_metric_structure/properties/suffixes")   |

### type



`type`

*   is required

*   Type: `string`

*   cannot be null

*   defined in: [Metrics](metrics-defs-container_metric_structure-properties-type.md "classpath:/schemas/metrics.schema.json#/$defs/container_metric_structure/properties/type")

#### type Type

`string`

#### type Constraints

**enum**: the value of this property must be equal to one of the following values:

| Value    | Explanation |
| :------- | :---------- |
| `"LIST"` |             |
| `"MAP"`  |             |

### structure



`structure`

*   is required

*   Type: merged type ([Details](metrics-defs-container_metric_structure-properties-structure.md))

*   cannot be null

*   defined in: [Metrics](metrics-defs-container_metric_structure-properties-structure.md "classpath:/schemas/metrics.schema.json#/$defs/container_metric_structure/properties/structure")

#### structure Type

merged type ([Details](metrics-defs-container_metric_structure-properties-structure.md))

one (and only one) of

*   [Untitled object in Metrics](metrics-defs-container_metric_structure.md "check type definition")

*   [Untitled object in Metrics](metrics-defs-primitive_metric_structure.md "check type definition")

### suffixes



`suffixes`

*   is optional

*   Type: `array`

*   cannot be null

*   defined in: [Metrics](metrics-defs-container_metric_structure-properties-suffixes.md "classpath:/schemas/metrics.schema.json#/$defs/container_metric_structure/properties/suffixes")

#### suffixes Type

`array`

## Definitions group metric

Reference this group by using

```json
{"$ref":"classpath:/schemas/metrics.schema.json#/$defs/metric"}
```

| Property                    | Type     | Required | Nullable       | Defined by                                                                                                                           |
| :-------------------------- | :------- | :------- | :------------- | :----------------------------------------------------------------------------------------------------------------------------------- |
| [namespace](#namespace)     | `string` | Required | cannot be null | [Metrics](metrics-defs-metric-properties-namespace.md "classpath:/schemas/metrics.schema.json#/$defs/metric/properties/namespace")   |
| [path](#path)               | `string` | Required | cannot be null | [Metrics](metrics-defs-metric-properties-path.md "classpath:/schemas/metrics.schema.json#/$defs/metric/properties/path")             |
| [structure](#structure-1)   | Merged   | Required | cannot be null | [Metrics](metrics-defs-metric-properties-structure.md "classpath:/schemas/metrics.schema.json#/$defs/metric/properties/structure")   |
| [extensions](#extensions-1) | `object` | Optional | cannot be null | [Metrics](metrics-defs-metric-properties-extensions.md "classpath:/schemas/metrics.schema.json#/$defs/metric/properties/extensions") |

### namespace



`namespace`

*   is required

*   Type: `string`

*   cannot be null

*   defined in: [Metrics](metrics-defs-metric-properties-namespace.md "classpath:/schemas/metrics.schema.json#/$defs/metric/properties/namespace")

#### namespace Type

`string`

### path



`path`

*   is required

*   Type: `string`

*   cannot be null

*   defined in: [Metrics](metrics-defs-metric-properties-path.md "classpath:/schemas/metrics.schema.json#/$defs/metric/properties/path")

#### path Type

`string`

### structure



`structure`

*   is required

*   Type: merged type ([Details](metrics-defs-metric-properties-structure.md))

*   cannot be null

*   defined in: [Metrics](metrics-defs-metric-properties-structure.md "classpath:/schemas/metrics.schema.json#/$defs/metric/properties/structure")

#### structure Type

merged type ([Details](metrics-defs-metric-properties-structure.md))

one (and only one) of

*   [Untitled object in Metrics](metrics-defs-container_metric_structure.md "check type definition")

*   [Untitled object in Metrics](metrics-defs-primitive_metric_structure.md "check type definition")

### extensions



`extensions`

*   is optional

*   Type: `object` ([Details](metrics-defs-metric-properties-extensions.md))

*   cannot be null

*   defined in: [Metrics](metrics-defs-metric-properties-extensions.md "classpath:/schemas/metrics.schema.json#/$defs/metric/properties/extensions")

#### extensions Type

`object` ([Details](metrics-defs-metric-properties-extensions.md))

## Definitions group range\_suffix\_format

Reference this group by using

```json
{"$ref":"classpath:/schemas/metrics.schema.json#/$defs/range_suffix_format"}
```

| Property              | Type      | Required | Nullable       | Defined by                                                                                                                                                 |
| :-------------------- | :-------- | :------- | :------------- | :--------------------------------------------------------------------------------------------------------------------------------------------------------- |
| [template](#template) | `string`  | Required | cannot be null | [Metrics](metrics-defs-range_suffix_format-properties-template.md "classpath:/schemas/metrics.schema.json#/$defs/range_suffix_format/properties/template") |
| [from](#from)         | `integer` | Optional | cannot be null | [Metrics](metrics-defs-range_suffix_format-properties-from.md "classpath:/schemas/metrics.schema.json#/$defs/range_suffix_format/properties/from")         |
| [to](#to)             | `integer` | Optional | cannot be null | [Metrics](metrics-defs-range_suffix_format-properties-to.md "classpath:/schemas/metrics.schema.json#/$defs/range_suffix_format/properties/to")             |

### template



`template`

*   is required

*   Type: `string`

*   cannot be null

*   defined in: [Metrics](metrics-defs-range_suffix_format-properties-template.md "classpath:/schemas/metrics.schema.json#/$defs/range_suffix_format/properties/template")

#### template Type

`string`

### from



`from`

*   is optional

*   Type: `integer`

*   cannot be null

*   defined in: [Metrics](metrics-defs-range_suffix_format-properties-from.md "classpath:/schemas/metrics.schema.json#/$defs/range_suffix_format/properties/from")

#### from Type

`integer`

#### from Default Value

The default value is:

```json
-1
```

### to



`to`

*   is optional

*   Type: `integer`

*   cannot be null

*   defined in: [Metrics](metrics-defs-range_suffix_format-properties-to.md "classpath:/schemas/metrics.schema.json#/$defs/range_suffix_format/properties/to")

#### to Type

`integer`

#### to Default Value

The default value is:

```json
-1
```
