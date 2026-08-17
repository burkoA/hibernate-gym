# Memory usage report for `hibernate-gym`

## 1. How the application uses memory for storing domain objects

This application stores its main business objects — `Trainee`, `Trainer`, `Training`, and related entities such as `TrainingType` — primarily in the **database** through Spring Data JPA and Hibernate, not in custom long-lived in-memory repositories. The repositories (`TrainerRepository`, `TraineeRepository`, `TrainingRepository`) are interfaces over JPA, so application memory is mainly used for **temporary object graphs** while requests are being processed.

In practice, memory is used in several layers:

- **HTTP/request layer**: request DTOs, response DTOs, validation objects, and controller/service method parameters live in memory for the duration of the request.
- **Hibernate persistence context**: when entities are loaded, Hibernate keeps managed instances in the current session/transaction. This acts like a first-level cache, so repeated access to the same entity in one transaction reuses the same Java object.
- **Entity relationships**: domain objects reference each other in memory. For example:
  - `Trainee` holds `List<Training>` and `Set<Trainer>`
  - `Trainer` holds `List<Training>` and `Set<Trainee>`
  - `Training` references one `Trainee`, one `Trainer`, and one `TrainingType`
- **Collections used in service logic**: temporary collections such as `HashMap<String, Trainer>` in `TraineeService.updateTrainers(...)` are created to make lookups faster.
- **Metrics objects**: Micrometer counters, gauges, and timers also use some memory, but this is usually small and stable compared with entity data.

So the most important point is: this application does **not** keep all domain objects in custom memory structures forever. Instead, memory usage mainly comes from:
1. currently processed requests,
2. currently loaded Hibernate entities,
3. temporary collections created by service methods,
4. framework/monitoring infrastructure.

## 2. Where memory leaks could occur and how to prevent them

There is no obvious classic leak such as a global `static List` or `Map` that keeps every created `Trainer`, `Trainee`, or `Training`. That is good. However, memory retention or leak-like behavior can still happen in a few places.

### A. Stale references in bidirectional entity associations
The application uses bidirectional relationships such as `Trainee.trainers` and `Trainer.trainees`. If only one side is updated in Java memory, already-loaded entities can hold outdated references longer than necessary.

Example risks:
- `TraineeService.updateTrainers(...)` replaces `trainee.trainers`, but does not fully synchronize the inverse `trainer.trainees` side in memory.
- `TrainingService.add(...)` and `addTraining(...)` save a `Training`, but do not add it to already-loaded parent collections.

This is not always a permanent leak, but it can increase memory retention during the current session and cause inconsistent in-memory graphs.

**Prevention:**
- update both sides of bidirectional associations consistently;
- keep transactions reasonably small;
- avoid loading more related entities than necessary.

### B. Large Hibernate persistence contexts
If a transaction loads many entities and keeps them managed for too long, the persistence context grows and consumes heap memory.

**Prevention:**
- keep service methods short and focused;
- prefer paginated queries for large result sets;
- avoid unnecessarily eager loading of relationships;
- clear or separate work into smaller transactions for batch-style processing.

### C. Metrics or counters that only grow
The project uses Micrometer with custom counters and gauges in `GymMetrics`. Counters are expected to grow, which is normal, but custom gauges backed by large in-memory structures would be dangerous.

In this codebase, gauges use `AtomicInteger` and `AtomicLong`, which is safe and lightweight.

**Prevention:**
- keep custom metrics backed by primitive/atomic values, not large collections;
- avoid storing historical business data in metric helper classes.

### D. Caches or static references added later
The production profile mentions `cache-enabled: true`, so future caching should be reviewed carefully. Unbounded caches are a common source of real memory leaks.

**Prevention:**
- use bounded caches with eviction policies and TTL;
- monitor cache size and hit/miss ratios;
- avoid storing full object graphs when small projections are enough.

### E. Listener/thread/resource leaks
Not all memory issues come from entities. Background executors, scheduled jobs, listeners, or streams that are never closed can also retain memory.

**Prevention:**
- close resources properly;
- shut down executors cleanly;
- avoid long-lived references from singleton beans unless truly needed.

## 3. How to monitor memory usage in a running Java application

This project already includes:
- `spring-boot-starter-actuator`
- `micrometer-registry-prometheus`

It also enables Actuator endpoints and JVM metrics in configuration.

### A. Monitoring with Spring Boot Actuator
The base configuration exposes:
- `health`
- `metrics`
- `prometheus`
- `info`

In the local profile, the Actuator base path is `/actuator`, and the application context path is `/api`, so typical local endpoints are:
- `/api/actuator/health`
- `/api/actuator/metrics`
- `/api/actuator/metrics/jvm.memory.used`
- `/api/actuator/metrics/jvm.memory.max`
- `/api/actuator/prometheus`

The production profile uses the base path `/management`, so production URLs may differ depending on deployment and security configuration.

Useful memory-related metrics to inspect:
- `jvm.memory.used`
- `jvm.memory.max`
- `jvm.memory.committed`
- `jvm.gc.pause`
- `jvm.threads.live`
- `process.uptime`

These can help answer questions such as:
- Is heap usage steadily rising?
- Does GC free memory after load drops?
- Are thread counts growing unexpectedly?

The application also exposes custom endpoints in `ActuatorInfoController`:
- `/api/actuator-info/metrics-summary`
- `/api/actuator-info/health-status`

These are not low-level heap profilers, but they are useful for a quick operational view.

### B. Monitoring with Prometheus and Grafana
Because Prometheus export is enabled, `/api/actuator/prometheus` can be scraped by Prometheus. This is a strong option for long-term monitoring.

Recommended dashboards/alerts:
- heap used vs max heap;
- old generation memory usage;
- GC pause time and frequency;
- live thread count;
- request rate vs memory growth.

If memory keeps rising over time and does not drop after garbage collection, that is a warning sign of a leak or excessive retention.

### C. Optional external tools
For deeper analysis, external JVM tools are very useful:

- **JVisualVM**: easy heap and thread monitoring, useful for inspecting object counts.
- **JConsole**: basic JVM memory and MBean monitoring.
- **Java Flight Recorder (JFR)**: low-overhead production-friendly profiling.
- **Eclipse MAT (Memory Analyzer Tool)**: best for analyzing heap dumps and finding dominator trees / retained objects.

A practical workflow is:
1. watch Actuator or Prometheus metrics during load;
2. if heap usage keeps climbing, capture a heap dump or JFR recording;
3. inspect which object types retain the most memory;
4. trace those objects back to entity graphs, caches, threads, or static references.

## Conclusion
This application does not appear to have a classic in-memory repository leak. Its memory usage is driven mostly by Hibernate-managed entities, request processing, temporary collections, and monitoring infrastructure. The main risks are stale references in bidirectional relationships, overly large persistence contexts, and any future unbounded caches or long-lived references. The best prevention strategy is to keep object graphs synchronized, transactions small, queries paginated, and memory usage continuously observed through Actuator, Prometheus, and JVM diagnostic tools.

