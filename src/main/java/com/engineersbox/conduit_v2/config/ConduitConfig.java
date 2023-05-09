package com.engineersbox.conduit_v2.config;

public class ConduitConfig {
  public final ConduitConfig.Executor executor;
  public final ConduitConfig.Ingest ingest;

  public static class Executor {
    public final int batch_size;
    public final boolean parallel_batching;
    public final java.util.Optional<java.lang.Integer> thread_pool_size;

    public Executor(
        com.typesafe.config.Config c,
        java.lang.String parentPath,
        $TsCfgValidator $tsCfgValidator) {
      this.batch_size = c.hasPathOrNull("batch_size") ? c.getInt("batch_size") : 10;
      this.parallel_batching =
          c.hasPathOrNull("parallel_batching") && c.getBoolean("parallel_batching");
      this.thread_pool_size =
          c.hasPathOrNull("thread_pool_size")
              ? java.util.Optional.of(c.getInt("thread_pool_size"))
              : java.util.Optional.empty();
    }
  }

  public static class Ingest {
    public final boolean async;
    public final boolean schema_provider_locking;

    public Ingest(
        com.typesafe.config.Config c,
        java.lang.String parentPath,
        $TsCfgValidator $tsCfgValidator) {
      this.async = c.hasPathOrNull("async") && c.getBoolean("async");
      this.schema_provider_locking =
          !c.hasPathOrNull("schema_provider_locking") || c.getBoolean("schema_provider_locking");
    }
  }

  public ConduitConfig(com.typesafe.config.Config c) {
    final $TsCfgValidator $tsCfgValidator = new $TsCfgValidator();
    final java.lang.String parentPath = "";
    this.executor =
        c.hasPathOrNull("executor")
            ? new ConduitConfig.Executor(
                c.getConfig("executor"), parentPath + "executor.", $tsCfgValidator)
            : new ConduitConfig.Executor(
                com.typesafe.config.ConfigFactory.parseString("executor{}"),
                parentPath + "executor.",
                $tsCfgValidator);
    this.ingest =
        c.hasPathOrNull("ingest")
            ? new ConduitConfig.Ingest(
                c.getConfig("ingest"), parentPath + "ingest.", $tsCfgValidator)
            : new ConduitConfig.Ingest(
                com.typesafe.config.ConfigFactory.parseString("ingest{}"),
                parentPath + "ingest.",
                $tsCfgValidator);
    $tsCfgValidator.validate();
  }

  private static final class $TsCfgValidator {
    private final java.util.List<java.lang.String> badPaths = new java.util.ArrayList<>();

    void addBadPath(java.lang.String path, com.typesafe.config.ConfigException e) {
      badPaths.add("'" + path + "': " + e.getClass().getName() + "(" + e.getMessage() + ")");
    }

    void validate() {
      if (!badPaths.isEmpty()) {
        java.lang.StringBuilder sb = new java.lang.StringBuilder("Invalid configuration:");
        for (java.lang.String path : badPaths) {
          sb.append("\n    ").append(path);
        }
        throw new com.typesafe.config.ConfigException(sb.toString()) {};
      }
    }
  }
}