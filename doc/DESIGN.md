# Design

## Requirements

TODO

## Overview

TODO

## Architecture

TODO

## Structure

### Ingestion

#### Source

TODO

##### Connectors

Depending on how the raw metrics should be retrieved, a connector will be provided to determine this behaviour. In particular
connectors handle network connections and an action wrapper to invoke a retrieval request. This includes authentication
that may be required. It is up to the discretion of the implementation to handle the connection itself including any behaviour
for re-using connections, closing them, retries, etc.

#### Provider

The JsonPath library provides and set of SPI abstractions for each of the core elements of data handling:

* JSON providers to creating/removing/updating elements of the data
* Mapper providers to convert between data objects and defined Java types
* Cache providers for cache policies to be used for querying the data

Supporting a new data type (such as MongoDB's BSON) will require the user to implement both the JSON and mapper providers.
In terms of the parsed objects and their structure, this is left up to the user which allows for arbitrary implementation so
long as it abides the interfaces required in these provider structures.

Caching SPIs need only be implemented if certain behaviour is desired when querying data, in terms of what is stored between
queries. 

TODO

### Schema

TODO

#### Provider

Depending on how a schema should be handled for updates and retrieving a new instance, this provider will determine that
behaviour. Implementations of this provider are only required to supply an instance of MetricsSchema upon invocation.
There are two standard options provided that can be used:

* Singleton, a new instance on every invocation
* Checksum refreshed, maintains a cached version of the schema which is returned when invoked. The cached entry is checked
against the file on disk to determine if there are any updates. If there are, the entry is updated on next invocation.



### Pipeline

TODO

### Lua Handlers

TODO