# Untitled object in Metrics Schema

```txt
classpath:/schemas/metrics.schema.json#/$defs/container_metric_structure
```



| Abstract            | Extensible | Status         | Identifiable | Custom Properties | Additional Properties | Access Restrictions | Defined In                                                                    |
| :------------------ | :--------- | :------------- | :----------- | :---------------- | :-------------------- | :------------------ | :---------------------------------------------------------------------------- |
| Can be instantiated | No         | Unknown status | No           | Forbidden         | Forbidden             | none                | [metrics.schema.json\*](../../out/metrics.schema.json "open original schema") |

## container\_metric\_structure Type

`object` ([Details](metrics-defs-container_metric_structure.md))

# container\_metric\_structure Properties

| Property                | Type     | Required | Nullable       | Defined by                                                                                                                                                               |
| :---------------------- | :------- | :------- | :------------- | :----------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| [type](#type)           | `string` | Required | cannot be null | [Metrics](metrics-defs-container_metric_structure-properties-type.md "classpath:/schemas/metrics.schema.json#/$defs/container_metric_structure/properties/type")         |
| [structure](#structure) | Merged   | Required | cannot be null | [Metrics](metrics-defs-metric_structure.md "classpath:/schemas/metrics.schema.json#/$defs/container_metric_structure/properties/structure")                              |
| [suffixes](#suffixes)   | `array`  | Optional | cannot be null | [Metrics](metrics-defs-container_metric_structure-properties-suffixes.md "classpath:/schemas/metrics.schema.json#/$defs/container_metric_structure/properties/suffixes") |

## type



`type`

*   is required

*   Type: `string`

*   cannot be null

*   defined in: [Metrics](metrics-defs-container_metric_structure-properties-type.md "classpath:/schemas/metrics.schema.json#/$defs/container_metric_structure/properties/type")

### type Type

`string`

### type Constraints

**enum**: the value of this property must be equal to one of the following values:

| Value    | Explanation |
| :------- | :---------- |
| `"LIST"` |             |
| `"MAP"`  |             |

## structure



`structure`

*   is required

*   Type: merged type ([Details](metrics-defs-metric_structure.md))

*   cannot be null

*   defined in: [Metrics](metrics-defs-metric_structure.md "classpath:/schemas/metrics.schema.json#/$defs/container_metric_structure/properties/structure")

### structure Type

merged type ([Details](metrics-defs-metric_structure.md))

one (and only one) of

*   [Untitled object in Metrics](metrics-defs-container_metric_structure.md "check type definition")

*   [Untitled object in Metrics](metrics-defs-primitive_metric_structure.md "check type definition")

## suffixes



`suffixes`

*   is optional

*   Type: `array`

*   cannot be null

*   defined in: [Metrics](metrics-defs-container_metric_structure-properties-suffixes.md "classpath:/schemas/metrics.schema.json#/$defs/container_metric_structure/properties/suffixes")

### suffixes Type

`array`
