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

### Pipeline

TODO

### Lua Handlers

TODO