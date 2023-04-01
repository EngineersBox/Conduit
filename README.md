# Conduit
Structured metrics ingester based on JSON paths and destructuring patterns to produce Riemann events

## Schema

At the core of conduit is the `MetricsSchema` defintion, which outlines the format of ingestable metrics and how they
should be interpreted. This is simple in its structure, merely a wrapper to house the individual `PathBinding` definitions
that are parsed from ingested JSON blobs.

```java
struct MetricsSchema {
	implicit HashMap<String, PathBinding> bindings;
	Configuration jsonPathParserConfig;

	put(String, PathBinding): void;
	get(String): PathBinding;
	remove(String): PathBinding;
}

builder MetricsSchema {
	put(PathBinding): this;
	with(Configuration): this;
	build(): MetricsSchema;
}
```

## Path Binding
Metrics to ingest are defined by a path and binding to their ingested format.
This forms the basis for the pipeline to parse when it has recieved the ingestable blob from a given ingestion handler.

```java
struct PathBinding {
	// Required properties
	String jsonPath;
	String metricNamespace;
	TypeRef<?> dataType;
	// Optional properties
	int dimension = 1;
	OrderedMap<DimensionIndex, String> dimensionalNamespaceSuffix = Map.of();
	Function<Map<String, Object>, Boolean> inclusionHandler = (_ignored) -> true;
}
```

The jsonPath value should be a vaid JsonPath expression from Stefan Goesner's definition <https://goessner.net/articles/JsonPath/>.
Assertions inline for the path can be in any valid JsonPath format though the result data type must be ingestable into Riemann
via the protobuf `Proto.Event` abstraction. Metrics that provide stateful types such as enum results will be inferred and set as such.

### Multi-dimensional Data
For data types with array or list format, several metrics points will be created with an index suffix onto the namespace.

For example, a metric that supplies as a list of integers:

```java
String jsonPath = "$some.list.metric"; // E.g. yields: [1, 5, 3, 2]
String metricNamespace = "/service/list_metric";
TypeRef<?> dataType = new TypeRef<List<Integer>>(){};
```

will results in the following events being generated with service names:

```java
message Event {
	string service = "/service/list_metric" + "/<index>";
	sint64 metric_sint64 = JsonPath.parse(...).read(
		"$some.list.metric",
		new TypeRef<List<Integer>>(){}
	).get(<index>)
}
```

which produces:

```json
[
  {
    "service": "/service/list_metric/0",
	"metric_sint64": 1,
  },
  {
    "service": "/service/list_metric/1",
	"metric_sint64": 5,
  },
  {
    "service": "/service/list_metric/2",
	"metric_sint64": 3,
  },
  {
    "service": "/service/list_metric/3",
	"metric_sint64": 2,
  }
]
```

#### Suffix Format

For dimensions greater than 1 (singletons), the format of the suffixes appended  to the base namespace can be supplied,
in order to have control over what is actually appended. The `dimensionalNamespaceSuffix` map is indexed by the absolute
dimenion index with the value being a format string literal where the index int value is injected into the template value of
`{index}` if it is present in the suffix format. For example, a suffix template of `/stat_{index}` would generate the following,
when paired with the base namespace `/service/list_metric`:

```
/service/list_metric/stat_0
/service/list_metric/stat_1
etc...
```
#### Dimension Index

Suffix formats are indexed by the `DimensionIndex` structure which provides a generalised structure for n-dimensional indexing
with attributes for how it should apply to other dimenions adjacent to it or within it.

```java
struct DimensionIndex {
	// Required parameters
	int dimension;
	// Optional Parameters (conditional usage)
	int index; // Only valid for querying and only valid for an index if not using universalWithin
	// Optional parameters (used for matching only)
	boolean universalWithin = false; // All values in this dimension use this suffix
	boolean applyToHigherDimensions = false; // All higher dimensions use this suffix (stacks with universalWithin)
	boolean applyToLowerDimensions = false; // All lower dimensions use this suffix (stacks with universalWithin)
}
```

During processing of metrics with definitions using `dimension > 1` for values, the `dimensionalNamespaceSuffix` map will be
queried with a minimal `DimensionIndex` value of:

```java
final DimensionIndex queryIndex = new DimensionIndex(<dimension>, <index>);
final String suffixFormat = pathBinding.dimensionalNamespaceSuffix.get(queryIndex);
```

The matching behaviour will first inquire on the `dimension` directly to see if it matches for the query `dimension`. If it does,
then it will check if the `index` matches, if so the assosciated suffix format is returned. If the `index` does not match but the `dimension` does, then it will check if `universalWithin` is present, returning the suffix format if it is. Otherwise, it will not match. If
the `index` matches but the `dimension` does not, then the `applyToHigherDimensions` is checked if the query `dimension` is
greater than the current `dimension`, if both of these are true then the suffix is returned. A similar logic path is taken in
inverse conditions for the less than dimension case. Given neither the `dimension` nor `index` match, and the `universalWithin`
flag is set along with the necessary `applyTo<Higher|Lower>Dimensions` flag for the given scenario, then the suffix is returned.
If none of these cases match, then there is no associated custom index format, so the default (`/{index}`) will be used.

Note that the `dimensionalNamespaceSuffix` map is ordered. This is so that the user can specify how the dimensional checks will
be preformed in terms of ordering. This allows for implicit fallback logic to be created purely on the basis of the matching
conditions.

## Metric Namespaces
Namespaces are delimited by `/` (forward slash) characters in the same style as Unix path notation. Paths are considered
unique on a global scale so for example, duplicate leaf names are allowed under the pretense that the path is unique:

```
/service/cpu/load
/service/mem/load
```

## Pipelines

Separate from the metrics schema definitions, piplines are the services that take a particular `MetricsSchema` with some extra
Riemann metadata to parse JSON blobs from an ingestion point to generate Riemann events.

```java
struct Pipeline {
	MetricsSchema schema;
	Proto.Event eventTemplate;
	IngestionSource ingestionPoint;
	BatchingConfiguration batchConfig;
}
```

TODO: Finish this

### Ingestion Source

TODO: Finish this. Essentially the following:
```java
@FunctionalInterface
interface IngestionSource {
	Object ingest(final IngestionContext ctx);
}

abstract class IngestionContext implements Closeable {
	private Map<String, Object> attributes;
	private long timeout;

	putAttribute(String, Object): void;
	getAttribute(String): Object;
	setTimeout(long): void;
	getTimeout(long): void;
	
	default cancel() throws Exception: void { ... }
	@Override
	default close() throws Exception: void { ... };
}
```

### Batching Configuration

TODO: Finish this. Essentiall thread pooling and execution configuration. Can generate a thread pool.

```java
struct BatchingConfiguration {
	int threads;
	int bulkSize;

	generateThreadPool(): ExecutorService;
	splitWorkload(List<?>): List<List<?>>;
}
```

### Notes

Currently can do all of the following in less than 500 ms:
* Start up conduit
* Pull the schema
* Parse schema into internal format
* Create a pipeline with the schema and configs
* Auth to the remote server providing the raw metrics JSON
* Connect to Riemann instance
* Pull the raw metrics json
* Parse the metrics json into invidual metrics
* Create reimann event objects for metrics
* Send metrics to riemann
* Stop pipeline
* Stop conduit