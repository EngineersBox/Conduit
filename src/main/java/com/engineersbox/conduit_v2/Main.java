package com.engineersbox.conduit_v2;

import com.engineersbox.conduit.handler.ContextTransformer;
import com.engineersbox.conduit.schema.MetricsSchemaProvider;
import com.engineersbox.conduit_v2.config.ConfigFactory;
import com.engineersbox.conduit_v2.processing.Conduit;
import com.engineersbox.conduit_v2.processing.generation.TaskBatchGeneratorFactory;
import com.engineersbox.conduit_v2.processing.task.worker.client.QueueSuppliedClientPool;
import com.engineersbox.conduit_v2.retrieval.caching.LRUCache;
import com.engineersbox.conduit_v2.retrieval.ingest.Source;
import com.jayway.jsonpath.spi.cache.CacheProvider;
import io.riemann.riemann.client.RiemannClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Main {

	private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main (final String[] args) throws IOException {
//		final Schema schema = Schema.from(new File(Path.of("./example/test.json").toUri()));
		CacheProvider.setCache(new LRUCache(10));
		try (final RiemannClient client = RiemannClient.tcp("localhost", 5555)) {
			client.connect();
			Conduit conduit = new Conduit(
					MetricsSchemaProvider.checksumRefreshed("./example/test.json", true),
//					new DirectSupplierClientPool(() -> client),
					new QueueSuppliedClientPool(() -> client, 5),
					TaskBatchGeneratorFactory.defaultGenerator(),
					(final ContextTransformer.Builder builder) -> builder.withReadOnly("service_version", 3),
					ConfigFactory.create("./example/config.conf")
			);
			conduit.execute(null, Source.singleConfigurable());
		} catch (final Exception e) {
			LOGGER.error("EXCEPTION IN MAIN:", e);
		}
    }

}
