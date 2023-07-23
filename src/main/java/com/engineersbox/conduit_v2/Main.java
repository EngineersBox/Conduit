package com.engineersbox.conduit_v2;

import com.engineersbox.conduit.handler.ContextBuiltins;
import com.engineersbox.conduit.handler.ContextTransformer;
import com.engineersbox.conduit.schema.MetricsSchemaProvider;
import com.engineersbox.conduit.schema.provider.LRUCache;
import com.engineersbox.conduit_v2.config.ConfigFactory;
import com.engineersbox.conduit_v2.processing.Conduit;
import com.engineersbox.conduit_v2.processing.generation.TaskBatchGeneratorFactory;
import com.engineersbox.conduit_v2.processing.task.worker.client.DirectSupplierClientPool;
import com.engineersbox.conduit_v2.processing.task.worker.client.QueueSuppliedClientPool;
import com.engineersbox.conduit_v2.retrieval.ingest.Source;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hubspot.jackson.datatype.protobuf.ProtobufModule;
import com.jayway.jsonpath.spi.cache.CacheProvider;
import io.riemann.riemann.Proto;
import io.riemann.riemann.client.RiemannClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

	private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

	private static final String test = """
{
	"host": "test service",
	"description": "some metric description",
	"metricD": 123.45
}
""";

    public static void main (final String[] args) throws JsonProcessingException {
//		final ObjectMapper mapper = new ObjectMapper();
//		mapper.registerModule(new ProtobufModule());
//		final Proto.Event event = mapper.readValue(test, Proto.Event.class);
//		LOGGER.info(
//				"EVENT: [host: {}] [description: {}] [metric_d: {}]",
//				event.getHost(),
//				event.getDescription(),
//				event.getMetricD()
//		);
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
