package com.engineersbox.conduit;

import com.engineersbox.conduit.schema.extension.handler.ContextTransformer;
import com.engineersbox.conduit.config.ConfigFactory;
import com.engineersbox.conduit.processing.Conduit;
import com.engineersbox.conduit.processing.generation.TaskBatchGeneratorFactory;
import com.engineersbox.conduit.processing.task.worker.executor.DirectSupplierJobExecutorPool;
import com.engineersbox.conduit.schema.factory.MetricsSchemaFactory;
import com.engineersbox.conduit.schema.extension.ExtensionProvider;
import com.engineersbox.conduit.schema.extension.LuaHandlerExtension;
import com.engineersbox.conduit.schema.json.path.PathFunctionProvider;
import com.engineersbox.conduit.processing.task.worker.client.QueueSuppliedClientPool;
import com.engineersbox.conduit.retrieval.caching.LRUCache;
import com.engineersbox.conduit.retrieval.content.batch.WorkloadBatcher;
import com.engineersbox.conduit.retrieval.ingest.Source;
import com.jayway.jsonpath.internal.EvaluationContext;
import com.jayway.jsonpath.internal.PathRef;
import com.jayway.jsonpath.internal.function.Parameter;
import com.jayway.jsonpath.internal.function.PathFunction;
import com.jayway.jsonpath.spi.cache.CacheProvider;
import io.riemann.riemann.client.RiemannClient;
import org.jeasy.batch.core.job.DefaultJobReportMerger;
import org.jeasy.batch.core.job.JobExecutor;
import org.jeasy.batch.core.job.JobReport;
import org.jeasy.batch.core.job.JobReportMerger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.Future;

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

    public static void main(final String[] args) throws Exception {
//		final Schema schema = Schema.from(new File(Path.of("./example/test.json").toUri()));
		CacheProvider.setCache(new LRUCache(10));
		PathFunctionProvider.bindFunction("someFunc", SomeFunc.class);
		ExtensionProvider.registerExtension(LuaHandlerExtension.getExtensionMetadata());
		try (final RiemannClient client = RiemannClient.tcp("localhost", 5555);
			 final JobExecutor jobExecutor = new JobExecutor(5)) {
			client.connect();
			final Conduit.Parameters<List<Future<JobReport>>, JobExecutor> params = new Conduit.Parameters<List<Future<JobReport>>, JobExecutor>()
					.setSchemaProvider(MetricsSchemaFactory.checksumRefreshed("./example/test.json", true))
					.setExecutor(
							new QueueSuppliedClientPool(
									() -> client,
									5
							),
							new DirectSupplierJobExecutorPool<>(
									() -> jobExecutor
							)
					).setWorkerTaskGenerator(TaskBatchGeneratorFactory.defaultGenerator())
					.setBatcher(WorkloadBatcher.defaultBatcher())
					.setContextInjector((final ContextTransformer.Builder builder) -> builder.withReadOnly("service_version", 3));
			final Conduit<List<Future<JobReport>>, JobExecutor> conduit = new Conduit<>(
                    params,
                    ConfigFactory.load(Path.of("./example/config.conf"))
            );
			final JobReport[] reports = conduit.execute(null, Source.singleConfigurable())
					.flatCollect((final ForkJoinTask<List<Future<JobReport>>> task) -> {
				try {
					return task.get().stream()
							.map((final Future<JobReport> report) -> {
								try {
									final JobReport jobReport = report.get();
									LOGGER.info(
											"[JOB REPORT]: {}",
											jobReport
									);
									return jobReport;
								} catch (final InterruptedException | ExecutionException e) {
									throw new RuntimeException(e);
								}
							}).toList();
				} catch (final InterruptedException | ExecutionException e) {
					throw new RuntimeException(e);
				}
			}).toArray(new JobReport[]{});
			final JobReportMerger merger = new DefaultJobReportMerger();
			final JobReport mergedReport = merger.mergerReports(reports);
			LOGGER.info("[MERGED JOB REPORT]: {}", mergedReport);
		}
    }

}
