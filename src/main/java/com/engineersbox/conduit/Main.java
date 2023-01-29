package com.engineersbox.conduit;

import com.engineersbox.conduit.pipeline.ingestion.IngestionContext;
import com.engineersbox.conduit.pipeline.Pipeline;
import com.engineersbox.conduit.pipeline.TypedMetricValue;
import com.engineersbox.conduit.schema.MetricsSchema;
import com.engineersbox.conduit.schema.PathBinding;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.TypeRef;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import io.riemann.riemann.Proto;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;

public class Main {

	private static final String TEST_JSON_BLOB = """
			{
			    "store": {
			        "book": [
			            {
			                "category": "reference",
			                "author": "Nigel Rees",
			                "title": "Sayings of the Century",
			                "price": 8.95
			            },
			            {
			                "category": "fiction",
			                "author": "Evelyn Waugh",
			                "title": "Sword of Honour",
			                "price": 12.99
			            },
			            {
			                "category": "fiction",
			                "author": "Herman Melville",
			                "title": "Moby Dick",
			                "isbn": "0-553-21311-3",
			                "price": 8.99
			            },
			            {
			                "category": "fiction",
			                "author": "J. R. R. Tolkien",
			                "title": "The Lord of the Rings",
			                "isbn": "0-395-19395-8",
			                "price": 22.99
			            }
			        ],
			        "bicycle": {
			            "color": "red",
			            "price": 19.95
			        }
			    },
			    "expensive": 10
			}""";

	private static final Logger LOGGER = LogManager.getLogger(Main.class);

	public static void main (final String[] args) {
		final Configuration configuration = Configuration.builder()
						.jsonProvider(new JacksonJsonProvider())
						.mappingProvider(new JacksonMappingProvider())
						.build();
		final MetricsSchema schema = MetricsSchema.builder()
				.put(PathBinding.path("$..book[?(@.price <= $['expensive'])].price")
						.name("non_expensive_prices")
						.type(new TypeRef<List<Double>>(){})
						.complete()
				).put(PathBinding.path("$.store.book[*].author")
						.name("authors_names")
						.type(new TypeRef<List<String>>(){})
						.complete()
				).withJsonPathConfig(configuration)
				.build();
		final Pipeline pipeline = new Pipeline(
				schema,
				Proto.Event.getDefaultInstance(),
				(final IngestionContext ctx) -> TEST_JSON_BLOB
		);
		final Map<String, TypedMetricValue<?>> results = pipeline.executeGrouped();
		LOGGER.info("==== GROUPED ====");
		results.forEach((final String metricName, final TypedMetricValue<?> value) -> LOGGER.info(
					"[Metric: {}] [Value: {}]",
					metricName,
					value.getValue()
		));
		results.clear();
		LOGGER.info("==== YIELDED ====");
		pipeline.executeYielding((final String metricName, final TypedMetricValue<?> value) -> LOGGER.info(
				"[Metric: {}] [Value: {}]",
				metricName,
				value.getValue()
		));
	}

}
