package com.engineersbox.conduit_v2;

import com.engineersbox.conduit.handler.ContextTransformer;
import com.engineersbox.conduit_v2.processing.schema.MetricsSchemaProvider;
import com.engineersbox.conduit_v2.config.ConfigFactory;
import com.engineersbox.conduit_v2.processing.Conduit;
import com.engineersbox.conduit_v2.processing.generation.TaskBatchGeneratorFactory;
import com.engineersbox.conduit_v2.processing.schema.json.path.PathFunctionProvider;
import com.engineersbox.conduit_v2.processing.task.worker.client.QueueSuppliedClientPool;
import com.engineersbox.conduit_v2.retrieval.caching.LRUCache;
import com.engineersbox.conduit_v2.retrieval.ingest.Source;
import com.jayway.jsonpath.internal.EvaluationContext;
import com.jayway.jsonpath.internal.PathRef;
import com.jayway.jsonpath.internal.function.Parameter;
import com.jayway.jsonpath.internal.function.PathFunction;
import com.jayway.jsonpath.spi.cache.CacheProvider;
import io.riemann.riemann.client.RiemannClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class Main {

	private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

	public static class SomeFunc implements PathFunction {

		@Override
		public Object invoke(final String currentPath,
							 final PathRef parent,
							 final Object model,
							 final EvaluationContext context,
							 final List<Parameter> parameters) {
			return "testresult!";
		}
	}

    public static void main(final String[] args) throws IOException {
//		final Schema schema = Schema.from(new File(Path.of("./example/test.json").toUri()));
		CacheProvider.setCache(new LRUCache(10));
		PathFunctionProvider.bindFunction("someFunc", SomeFunc.class);
		try (final RiemannClient client = RiemannClient.tcp("localhost", 5555)) {
			client.connect();
			final Conduit conduit = new Conduit(
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
