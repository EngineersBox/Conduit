package com.engineersbox.conduit;

import com.engineersbox.conduit.pipeline.BatchingConfiguration;
import com.engineersbox.conduit.pipeline.Pipeline;
import com.engineersbox.conduit.pipeline.ingestion.IngestionContext;
import com.engineersbox.conduit.schema.provider.LRUCache;
import com.engineersbox.conduit.schema.type.Functional;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.spi.cache.CacheProvider;
import io.riemann.riemann.Proto;
import io.riemann.riemann.client.RiemannClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Main {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
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

	public static void main (final String[] args) throws IOException {
		CacheProvider.setCache(new LRUCache(10));
		final Pipeline pipeline = new Pipeline(
				"./example/test.json",
				(final IngestionContext ctx) -> TEST_JSON_BLOB,
				new BatchingConfiguration(1, 3)
		);
		try (final RiemannClient client = RiemannClient.tcp("localhost", 5555)) {
			client.connect();
			pipeline.executeHandled(Functional.uncheckedConsumer((final List<Proto.Event> events) -> {
				LOGGER.info("Sending events: \n" + events.stream().map((final Proto.Event event) -> String.format(
						" - [Host: %s] [Service: %s] [State: '%s'] [Float: %f] [Double: %f] [Int: %d]%n",
						event.getHost(),
						event.getService(),
						event.getState(),
						event.getMetricF(),
						event.getMetricD(),
						event.getMetricSint64()
				)).collect(Collectors.joining()));
				client.sendEvents(events.toArray(Proto.Event[]::new))
						.deref(1, TimeUnit.SECONDS);
			}));
		}
	}

}
