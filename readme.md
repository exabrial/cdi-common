# cdi-common

## Description

A grab-bag of reusable CDI producers, qualifiers, interceptors, and a small SPI-style
async-initialization framework. Targets Jakarta EE 10 (`jakarta.*`), JDK 25, and is tested
against Apache OpenWebBeans SE.

## Maven Coordinates

```xml
<dependency>
	<groupId>com.github.exabrial.cdi</groupId>
	<artifactId>cdi-common</artifactId>
	<version>1.0.0-SNAPSHOT</version>
	<scope>compile</scope>
</dependency>
```

The CDI portable extension is registered via
`META-INF/services/jakarta.enterprise.inject.spi.Extension`, so it activates as soon as the
jar is on the classpath.

---

## Feature: SLF4J `Logger` Injection

Inject a `Logger` and get one named for the class it is injected into. No `LoggerFactory`
boilerplate, no `@Inject`-of-a-field-name mismatch.

```java
@ApplicationScoped
public class WidgetService {
	@Inject
	private Logger log;
}
```

The produced `Logger` is `@Dependent` and named `LoggerFactory.getLogger(WidgetService.class)`.

---

## Feature: `@Config` Property Injection

Inject externalized configuration, type-converted, from a merged property source. In this
module the classpath files are loaded in this order (later wins): `cdi-common.maven.properties`,
`cdi-common.externalized-strings.properties`, `cdi-common.config.properties`,
`cdi-common.local.properties`, `cdi-common.test.properties`, then JVM system properties, then
environment variables. The `cdi-common` filename prefix is hardcoded in `PropertyProducer`.

```java
@Inject
@Config("smtp.host")
private String smtpHost;

@Inject
@Config(value = "smtp.port", defaultValue = "25")
private Integer smtpPort;

@Inject
@Config("feature.enabled")
private Boolean featureEnabled;
```

If `value` is omitted, the property name defaults to `SimpleClassName.fieldName`.

### Supported target types

| Target type | Notes |
|---|---|
| `String` | raw value |
| `Boolean` | accepts `true/yes/y/on/1` and `false/no/n/off/0` |
| `Integer`, `Long`, `BigDecimal` | parsed |
| `List<String>` | comma-split |
| `Set<String>` | comma-split, deduplicated |
| `Map<String, String>` | comma-split entries, each `key|value` |
| `byte[]` | Base64-decoded, or file contents (see below) |

### Environment variable mapping

Env var names are normalized so `SMTP__HOST` resolves to both `smtp.host` and `smtp-host`
(`__` becomes `.`, and a `-` variant is also registered).

### File contents

Add `@FileContents` alongside `@Config` to treat the resolved property as a path and inject the
file's contents rather than the literal value.

```java
@Inject
@Config("tls.cert.path")
@FileContents(isAbsoluteFileSystemPath = true)
private String pemContents;

@Inject
@Config("keystore.path")
@FileContents(isAbsoluteFileSystemPath = false)
private byte[] keystoreBytes;
```

`isAbsoluteFileSystemPath = false` reads from the classpath; `true` reads from the filesystem.

### Overriding resolved values

Provide a bean implementing `PropertyProducerOverrider` to intercept every resolved value
(for example, to pull from a secrets manager). If present, it is consulted after the property
source lookup; a thrown exception is swallowed and the original value is used.

```java
public interface PropertyProducerOverrider {
	String override(String configPropertyName, InjectionPoint injectionPoint, String originalValue);
}
```

### Reading a default value programmatically

`ConfigUtils.readDefaultValue(Class<?>, String fieldName)` returns the `defaultValue` declared
on a field's `@Config` annotation without instantiating anything.

---

## Feature: JSON-B `Jsonb` Producer

Produces a configured Johnzon-backed `Jsonb`. Configuration uses `IDENTITY` naming,
`InheritedPropertyVisibilityStrategy` (so inherited fields serialize), and disables the
Johnzon BigDecimal-as-string adapter.

Two flavors are produced, selected by qualifier:

```java
@Inject
@Transacted
private Jsonb jsonb;        // @Default, @TransactionScoped, serializable wrapper

@Inject
@NonTransacted
private Jsonb jsonb;        // @BoundaryScoped
```

`@Transacted` is also `@Default`, so a bare `@Inject Jsonb` resolves to the transacted one. The
instance is closed automatically by a disposer.

---

## Feature: JAX-RS `Client` Producer

Same qualifier pattern as the `Jsonb` producer, for a JAX-RS `Client`. Requires a JAX-RS client
implementation on the runtime classpath (CXF is provided as an optional runtime dependency).

```java
@Inject
@Transacted
private Client client;      // @Default, @TransactionScoped, serializable wrapper

@Inject
@NonTransacted
private Client client;      // @BoundaryScoped
```

The client is closed automatically by a disposer. The `@Transacted` variant is a serializable
wrapper that re-resolves a live `Client` after deserialization, so it survives passivation.

---

## Feature: `@AllImplementations` Injection

Inject every `@ApplicationScoped` implementation of an interface as a `Set`. Useful for
plugin/strategy fan-out.

```java
@Inject
@AllImplementations
private Set<OutputService> outputServices;
```

Rules enforced at injection time:

- The injection target must be a `Set`, or a `NotASetException` is thrown.
- The `Set`'s generic type must be an interface, or a `NotAnInterfaceException` is thrown.
- Every implementation must be `@ApplicationScoped`, or a `NotApplicationScopedException` is thrown.

---

## Feature: `InstanceUtil` Programmatic Lookup

A thin, `try-with-resources`-friendly wrapper over `BeanManager` bean resolution. `locate`
never returns `null`; it returns an `InstanceHandle` that reports whether the bean was
resolvable and releases any `@Dependent` `CreationalContext` on `close()`.

```java
@Inject
private InstanceUtil instanceUtil;

try (InstanceHandle<OptionalService> handle = instanceUtil.locate(OptionalService.class)) {
	if (handle.isResolvable()) {
		handle.get().doWork();
	}
}
```

`InstanceHandle<T>` implements both `Provider<T>` and `AutoCloseable`.

---

## Feature: Top-Level Error Handler (`TlehInterceptor`)

An interceptor that catches, logs, and rethrows exceptions at an application boundary. Throw
low, catch and log high. It logs Bean Validation `ConstraintViolationException`s with their
property paths and messages, logs an unauthorized `WebApplicationException` at `WARN`, and
everything else at `ERROR`.

```java
@Path("/widgets")
@Interceptors({ XaUuidInterceptor.class, TlehInterceptor.class })
public class WidgetInitiator {
	// ...
}
```

Place it on your initiators (REST resources, timers, message listeners).

---

## Feature: XA UUID Correlation (`XaUuidInterceptor`)

Puts a short random correlation id into the SLF4J `MDC` under `xa-uuid` for the duration of the
call, and clears it afterward if this call is the one that set it. Pair it with `TlehInterceptor`
on initiators so every log line in a transaction shares a traceable id.

```java
@Interceptors({ XaUuidInterceptor.class, TlehInterceptor.class })
```

Static helpers `getMdc()`, `startMdc()`, `randomUuid()`, and `clear()` are exposed for manual use.

---

## Feature: `@Transacted` / `@NonTransacted` Qualifiers

Two shared qualifiers used by the `Jsonb` and `Client` producers, available for your own
producers as well. `@Transacted` is intended for the `@Default`, transaction-scoped variant;
`@NonTransacted` for a boundary-scoped variant. Both expose a `LITERAL` for programmatic
selection.

```java
CDI.current().select(Jsonb.class, Transacted.LITERAL).get();
```

---

## Feature: Async Initialization Framework

For beans that need slow, one-time startup work (warming a cache, opening a pool) to run
asynchronously, off the main deployment thread, and gated so callers block only if they touch
the bean before it is ready.

### 1. Extend `AsyncInitializable`

```java
@ApplicationScoped
public class ModelWarmupService extends AsyncInitializable {
	@Override
	public Class<?> getTriggerEventType() {
		return AppReadyEvent.class;
	}

	@Override
	public Annotation getTriggerEventQualifier() {
		return Started.LITERAL;
	}

	@Override
	protected Object initialize() {
		// slow work; runs asynchronously when the trigger event fires
		return null;
	}
}
```

### 2. Fire the trigger event

When an event of `getTriggerEventType()` with `getTriggerEventQualifier()` is fired, the
framework's synthetic async observer runs `initialize()` on that bean. On completion it fires an
`AsyncInitializableReady` event (both sync and async) carrying the concrete type.

### 3. Gating

`AsyncInitializable` is annotated `@AwaitInitialized`, so `AwaitInitializedInterceptor` guards
its business methods: a call from another thread blocks until initialization completes (up to a
configurable timeout), while the initializing thread itself is never blocked. A timeout or
initialization failure surfaces as an `AsyncInitializationException`.

Annotate methods that must remain callable before initialization (accessors, static info) with
`@SkipInitializationGuard` to bypass the wait.

The timeout is injected via `@Config Long initializationTimeout` and can be set through any
`@Config` property source.

---

## License

EUPL-1.2. See `license.txt`.
