package com.engineersbox.conduit_v2;

import com.engineersbox.conduit.handler.ContextTransformer;
import com.engineersbox.conduit.schema.MetricsSchemaProvider;
import com.engineersbox.conduit.schema.provider.LRUCache;
import com.engineersbox.conduit_v2.config.ConfigFactory;
import com.engineersbox.conduit_v2.processing.Conduit;
import com.engineersbox.conduit_v2.processing.task.worker.client.DirectSupplierClientPool;
import com.engineersbox.conduit_v2.processing.task.worker.client.QueueSuppliedClientPool;
import com.jayway.jsonpath.spi.cache.CacheProvider;
import io.riemann.riemann.client.RiemannClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

	private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main (final String[] args) {
		CacheProvider.setCache(new LRUCache(10));
		try (final RiemannClient client = RiemannClient.tcp("localhost", 5555)) {
			client.connect();
			Conduit conduit = new Conduit(
					MetricsSchemaProvider.checksumRefreshed("./example/test.json", false),
//					new DirectSupplierClientPool(() -> client),
					new QueueSuppliedClientPool(() -> client, 5),
					(final ContextTransformer.Builder builder) -> {},
					ConfigFactory.create("./example/config.conf")
			);
			conduit.execute();
		} catch (final Exception e) {
			LOGGER.error("EXCEPTION IN MAIN:", e);
		}
    }

}
