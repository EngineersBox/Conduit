package com.engineersbox.conduit_v2;

import com.engineersbox.conduit.handler.ContextTransformer;
import com.engineersbox.conduit.schema.MetricsSchemaProvider;
import com.engineersbox.conduit.schema.provider.LRUCache;
import com.engineersbox.conduit_v2.config.ConfigFactory;
import com.engineersbox.conduit_v2.processing.Conduit;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.spi.cache.CacheProvider;
import io.riemann.riemann.client.RiemannClient;
import org.apache.logging.log4j.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

	private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
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

    public static void main (final String[] args) {
		CacheProvider.setCache(new LRUCache(10));
		try (final RiemannClient client = RiemannClient.tcp("localhost", 5555)) {
			client.connect();
			Conduit conduit = new Conduit(
					MetricsSchemaProvider.checksumRefreshed("./example/test.json", false),
					() -> client,
					(final ContextTransformer.Builder builder) -> {},
					ConfigFactory.create("./example/config.conf")
			);
			conduit.execute();
		} catch (final Exception e) {
			LOGGER.error("EXCEPTION IN MAIN:", e);
		}
    }

}
