package com.engineersbox.conduit.core.config;

public class ConduitConfig {
  public final ConduitConfig.Executor executor;
  public final ConduitConfig.Handler handler;
  public final ConduitConfig.Ingest ingest;
  // NOTE: incomplete #62 implementation
  public enum LogLevel {
    ERROR,
    WARN,
    INFO,
    DEBUG,
    TRACE;
  }

  public static class Executor {
    public final boolean lazy;
    public final int task_batch_size;
    public final java.util.Optional<java.lang.Integer> task_pool_size;

    public Executor(
        com.typesafe.config.Config c,
        java.lang.String parentPath,
        $TsCfgValidator $tsCfgValidator) {
      this.lazy = c.hasPathOrNull("lazy") && c.getBoolean("lazy");
      this.task_batch_size = c.hasPathOrNull("task_batch_size") ? c.getInt("task_batch_size") : 10;
      this.task_pool_size =
          c.hasPathOrNull("task_pool_size")
              ? java.util.Optional.of(c.getInt("task_pool_size"))
              : java.util.Optional.empty();
    }
  }

  public static class Handler {
    public final LogLevel level;
    public final java.lang.String name;

    public Handler(
        com.typesafe.config.Config c,
        java.lang.String parentPath,
        $TsCfgValidator $tsCfgValidator) {
      this.level = LogLevel.valueOf(c.getString("level"));
      this.name = c.hasPathOrNull("name") ? c.getString("name") : "Lua Handler";
    }
  }

  public static class Ingest {
    public final boolean async;
    public final Ingest.Connector_cache connector_cache;
    public final Ingest.Event_transformer event_transformer;
    public final boolean schema_provider_locking;

    public static class Connector_cache {
      public final int concurrency_level;
      public final boolean record_stats;

      public Connector_cache(
          com.typesafe.config.Config c,
          java.lang.String parentPath,
          $TsCfgValidator $tsCfgValidator) {
        this.concurrency_level =
            c.hasPathOrNull("concurrency_level") ? c.getInt("concurrency_level") : 1;
        this.record_stats = c.hasPathOrNull("record_stats") && c.getBoolean("record_stats");
      }
    }

    public static class Event_transformer {
      public final boolean skip_on_infer_failure;

      public Event_transformer(
          com.typesafe.config.Config c,
          java.lang.String parentPath,
          $TsCfgValidator $tsCfgValidator) {
        this.skip_on_infer_failure =
            !c.hasPathOrNull("skip_on_infer_failure") || c.getBoolean("skip_on_infer_failure");
      }
    }

    public Ingest(
        com.typesafe.config.Config c,
        java.lang.String parentPath,
        $TsCfgValidator $tsCfgValidator) {
      this.async = c.hasPathOrNull("async") && c.getBoolean("async");
      this.connector_cache =
          c.hasPathOrNull("connector_cache")
              ? new Ingest.Connector_cache(
                  c.getConfig("connector_cache"), parentPath + "connector_cache.", $tsCfgValidator)
              : new Ingest.Connector_cache(
                  com.typesafe.config.ConfigFactory.parseString("connector_cache{}"),
                  parentPath + "connector_cache.",
                  $tsCfgValidator);
      this.event_transformer =
          c.hasPathOrNull("event_transformer")
              ? new Ingest.Event_transformer(
                  c.getConfig("event_transformer"),
                  parentPath + "event_transformer.",
                  $tsCfgValidator)
              : new Ingest.Event_transformer(
                  com.typesafe.config.ConfigFactory.parseString("event_transformer{}"),
                  parentPath + "event_transformer.",
                  $tsCfgValidator);
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
    this.handler =
        c.hasPathOrNull("handler")
            ? new ConduitConfig.Handler(
                c.getConfig("handler"), parentPath + "handler.", $tsCfgValidator)
            : new ConduitConfig.Handler(
                com.typesafe.config.ConfigFactory.parseString("handler{}"),
                parentPath + "handler.",
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