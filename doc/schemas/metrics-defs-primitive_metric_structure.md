# Untitled object in Metrics Schema

```txt
classpath:/schemas/metrics.schema.json#/$defs/primitive_metric_structure
```



| Abstract            | Extensible | Status         | Identifiable | Custom Properties | Additional Properties | Access Restrictions | Defined In                                                                    |
| :------------------ | :--------- | :------------- | :----------- | :---------------- | :-------------------- | :------------------ | :---------------------------------------------------------------------------- |
| Can be instantiated | No         | Unknown status | No           | Forbidden         | Forbidden             | none                | [metrics.schema.json\*](../../out/metrics.schema.json "open original schema") |

## primitive\_metric\_structure Type

`object` ([Details](metrics-defs-primitive_metric_structure.md))

# primitive\_metric\_structure Properties

| Property      | Type     | Required | Nullable       | Defined by                                                                                                                                                       |
| :------------ | :------- | :------- | :------------- | :--------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| [type](#type) | `string` | Required | cannot be null | [Metrics](metrics-defs-primitive_metric_structure-properties-type.md "classpath:/schemas/metrics.schema.json#/$defs/primitive_metric_structure/properties/type") |

## type



`type`

*   is required

*   Type: `string`

*   cannot be null

*   defined in: [Metrics](metrics-defs-primitive_metric_structure-properties-type.md "classpath:/schemas/metrics.schema.json#/$defs/primitive_metric_structure/properties/type")

### type Type

`string`

### type Constraints

**enum**: the value of this property must be equal to one of the following values:

| Value       | Explanation |
| :---------- | :---------- |
| `"STRING"`  |             |
| `"FLOAT"`   |             |
| `"DOUBLE"`  |             |
| `"INTEGER"` |             |
| `"BOOLEAN"` |             |
| `"INFER"`   |             |
