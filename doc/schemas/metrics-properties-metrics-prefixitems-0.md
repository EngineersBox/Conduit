# Untitled object in Metrics Schema

```txt
classpath:/schemas/metrics.schema.json#/properties/metrics/prefixItems/0
```



| Abstract            | Extensible | Status         | Identifiable | Custom Properties | Additional Properties | Access Restrictions | Defined In                                                                    |
| :------------------ | :--------- | :------------- | :----------- | :---------------- | :-------------------- | :------------------ | :---------------------------------------------------------------------------- |
| Can be instantiated | No         | Unknown status | No           | Forbidden         | Forbidden             | none                | [metrics.schema.json\*](../../out/metrics.schema.json "open original schema") |

## 0 Type

`object` ([Details](metrics-properties-metrics-prefixitems-0.md))

# 0 Properties

| Property                  | Type     | Required | Nullable       | Defined by                                                                                                                           |
| :------------------------ | :------- | :------- | :------------- | :----------------------------------------------------------------------------------------------------------------------------------- |
| [namespace](#namespace)   | `string` | Required | cannot be null | [Metrics](metrics-defs-metric-properties-namespace.md "classpath:/schemas/metrics.schema.json#/$defs/metric/properties/namespace")   |
| [path](#path)             | `string` | Required | cannot be null | [Metrics](metrics-defs-metric-properties-path.md "classpath:/schemas/metrics.schema.json#/$defs/metric/properties/path")             |
| [structure](#structure)   | Merged   | Required | cannot be null | [Metrics](metrics-defs-metric-properties-structure.md "classpath:/schemas/metrics.schema.json#/$defs/metric/properties/structure")   |
| [extensions](#extensions) | `object` | Optional | cannot be null | [Metrics](metrics-defs-metric-properties-extensions.md "classpath:/schemas/metrics.schema.json#/$defs/metric/properties/extensions") |

## namespace



`namespace`

*   is required

*   Type: `string`

*   cannot be null

*   defined in: [Metrics](metrics-defs-metric-properties-namespace.md "classpath:/schemas/metrics.schema.json#/$defs/metric/properties/namespace")

### namespace Type

`string`

## path



`path`

*   is required

*   Type: `string`

*   cannot be null

*   defined in: [Metrics](metrics-defs-metric-properties-path.md "classpath:/schemas/metrics.schema.json#/$defs/metric/properties/path")

### path Type

`string`

## structure



`structure`

*   is required

*   Type: merged type ([Details](metrics-defs-metric-properties-structure.md))

*   cannot be null

*   defined in: [Metrics](metrics-defs-metric-properties-structure.md "classpath:/schemas/metrics.schema.json#/$defs/metric/properties/structure")

### structure Type

merged type ([Details](metrics-defs-metric-properties-structure.md))

one (and only one) of

*   [Untitled object in Metrics](metrics-defs-container_metric_structure.md "check type definition")

*   [Untitled object in Metrics](metrics-defs-primitive_metric_structure.md "check type definition")

## extensions



`extensions`

*   is optional

*   Type: `object` ([Details](metrics-defs-metric-properties-extensions.md))

*   cannot be null

*   defined in: [Metrics](metrics-defs-metric-properties-extensions.md "classpath:/schemas/metrics.schema.json#/$defs/metric/properties/extensions")

### extensions Type

`object` ([Details](metrics-defs-metric-properties-extensions.md))
