package com.engineersbox.conduit_v2;

import com.engineersbox.conduit.handler.ContextTransformer;
import com.engineersbox.conduit.pipeline.BatchingConfiguration;
import com.engineersbox.conduit.schema.MetricsSchemaProvider;
import com.engineersbox.conduit.schema.provider.LRUCache;
import com.engineersbox.conduit.schema.type.Functional;
import com.engineersbox.conduit_v2.config.ConduitConfig;
import com.engineersbox.conduit_v2.config.ConfigFactory;
import com.engineersbox.conduit_v2.processing.Conduit;
import com.engineersbox.conduit_v2.processing.pipeline.Pipeline;
import com.engineersbox.conduit_v2.retrieval.ingest.IngestionContext;
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

    public static void main (final String[] args) throws Exception {
		CacheProvider.setCache(new LRUCache(10));
		try (final RiemannClient client = RiemannClient.tcp("localhost", 5555)) {
			client.connect();
			Conduit conduit = new Conduit(
					MetricsSchemaProvider.checksumRefreshed("./example/test.json", false),
					() -> client,
					(final ContextTransformer.Builder builder) -> {
					},
					ConfigFactory.create("./example/config.conf")
			);
			conduit.execute();
		}
    }

}
