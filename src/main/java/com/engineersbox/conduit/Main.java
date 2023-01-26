package com.engineersbox.conduit;

import com.engineersbox.conduit.pipeline.Pipeline;
import com.engineersbox.conduit.pipeline.TypedMetricValue;
import com.engineersbox.conduit.schema.MetricsSchema;
import com.engineersbox.conduit.schema.PathBinding;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.TypeRef;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Main {

	private static final String TEST_JSON_BLOB = "{\n" +
			"    \"store\": {\n" +
			"        \"book\": [\n" +
			"            {\n" +
			"                \"category\": \"reference\",\n" +
			"                \"author\": \"Nigel Rees\",\n" +
			"                \"title\": \"Sayings of the Century\",\n" +
			"                \"price\": 8.95\n" +
			"            },\n" +
			"            {\n" +
			"                \"category\": \"fiction\",\n" +
			"                \"author\": \"Evelyn Waugh\",\n" +
			"                \"title\": \"Sword of Honour\",\n" +
			"                \"price\": 12.99\n" +
			"            },\n" +
			"            {\n" +
			"                \"category\": \"fiction\",\n" +
			"                \"author\": \"Herman Melville\",\n" +
			"                \"title\": \"Moby Dick\",\n" +
			"                \"isbn\": \"0-553-21311-3\",\n" +
			"                \"price\": 8.99\n" +
			"            },\n" +
			"            {\n" +
			"                \"category\": \"fiction\",\n" +
			"                \"author\": \"J. R. R. Tolkien\",\n" +
			"                \"title\": \"The Lord of the Rings\",\n" +
			"                \"isbn\": \"0-395-19395-8\",\n" +
			"                \"price\": 22.99\n" +
			"            }\n" +
			"        ],\n" +
			"        \"bicycle\": {\n" +
			"            \"color\": \"red\",\n" +
			"            \"price\": 19.95\n" +
			"        }\n" +
			"    },\n" +
			"    \"expensive\": 10\n" +
			"}";

	private static final Logger LOGGER = LogManager.getLogger(Main.class);

	public static void main (final String[] args) {
		Configuration.setDefaults(new Configuration.Defaults() {

			private final JsonProvider jsonProvider = new JacksonJsonProvider();
			private final MappingProvider mappingProvider = new JacksonMappingProvider();

			@Override
			public JsonProvider jsonProvider() {
				return jsonProvider;
			}

			@Override
			public MappingProvider mappingProvider() {
				return mappingProvider;
			}

			@Override
			public Set<Option> options() {
				return EnumSet.noneOf(Option.class);
			}
		});
		final MetricsSchema schema = MetricsSchema.builder()
				.put(PathBinding.path("$..book[?(@.price <= $['expensive'])].price")
						.name("non_expensive_prices")
						.type(new TypeRef<List<Double>>(){})
						.complete()
				).put(PathBinding.path("$.store.book[*].author")
						.name("authors_names")
						.type(new TypeRef<List<String>>(){})
						.complete()
				).build();
		final Pipeline pipeline = new Pipeline(schema, () -> TEST_JSON_BLOB);
		final Map<String, TypedMetricValue<?>> results = pipeline.executeGrouped();
		LOGGER.info("==== GROUPED ====");
		results.forEach((final String metricName, final TypedMetricValue<?> value) -> LOGGER.info(
					"[Metric: {}] [Value: {}] [Type: {}]",
					metricName,
					value.getValue(),
					value.getTypeRef().getType().getTypeName()
		));
		results.clear();
		LOGGER.info("==== YIELDED ====");
		pipeline.executeYielding((final String metricName, final TypedMetricValue<?> value) -> LOGGER.info(
				"[Metric: {}] [Value: {}] [Type: {}]",
				metricName,
				value.getValue(),
				value.getTypeRef().getType().getTypeName()
		));
	}

}
