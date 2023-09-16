# Untitled object in Metrics Schema

```txt
classpath:/schemas/metrics.schema.json#/$defs/range_suffix_format
```



| Abstract            | Extensible | Status         | Identifiable | Custom Properties | Additional Properties | Access Restrictions | Defined In                                                                    |
| :------------------ | :--------- | :------------- | :----------- | :---------------- | :-------------------- | :------------------ | :---------------------------------------------------------------------------- |
| Can be instantiated | No         | Unknown status | No           | Forbidden         | Forbidden             | none                | [metrics.schema.json\*](../../out/metrics.schema.json "open original schema") |

## range\_suffix\_format Type

`object` ([Details](metrics-defs-range_suffix_format.md))

# range\_suffix\_format Properties

| Property              | Type      | Required | Nullable       | Defined by                                                                                                                                                 |
| :-------------------- | :-------- | :------- | :------------- | :--------------------------------------------------------------------------------------------------------------------------------------------------------- |
| [template](#template) | `string`  | Required | cannot be null | [Metrics](metrics-defs-range_suffix_format-properties-template.md "classpath:/schemas/metrics.schema.json#/$defs/range_suffix_format/properties/template") |
| [from](#from)         | `integer` | Optional | cannot be null | [Metrics](metrics-defs-range_suffix_format-properties-from.md "classpath:/schemas/metrics.schema.json#/$defs/range_suffix_format/properties/from")         |
| [to](#to)             | `integer` | Optional | cannot be null | [Metrics](metrics-defs-range_suffix_format-properties-to.md "classpath:/schemas/metrics.schema.json#/$defs/range_suffix_format/properties/to")             |

## template



`template`

*   is required

*   Type: `string`

*   cannot be null

*   defined in: [Metrics](metrics-defs-range_suffix_format-properties-template.md "classpath:/schemas/metrics.schema.json#/$defs/range_suffix_format/properties/template")

### template Type

`string`

## from



`from`

*   is optional

*   Type: `integer`

*   cannot be null

*   defined in: [Metrics](metrics-defs-range_suffix_format-properties-from.md "classpath:/schemas/metrics.schema.json#/$defs/range_suffix_format/properties/from")

### from Type

`integer`

### from Default Value

The default value is:

```json
-1
```

## to



`to`

*   is optional

*   Type: `integer`

*   cannot be null

*   defined in: [Metrics](metrics-defs-range_suffix_format-properties-to.md "classpath:/schemas/metrics.schema.json#/$defs/range_suffix_format/properties/to")

### to Type

`integer`

### to Default Value

The default value is:

```json
-1
```
