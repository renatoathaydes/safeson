# JSON Parser Performance Comparisons

This project performs a Performance Test Comparison between the SafeSON Parser and
several popular Java JSON Parsers:

* GSON
* Jackson
* minimal-json
* DSL-JSON

There are two types of benchmarks:

* Tests that collect times for each run, generating CSV results. Located at [src/perf/java](src/perf/java).
* [JMH](http://openjdk.java.net/projects/code-tools/jmh/) benchmarks. Located at [src/jmh/java](src/jmh/java).

## Collecting times for each run

### Building

Run:

```bash
./gradlew jar
```

### Running tests

To execute the tests, use the runnable jar created by Gradle:

```bash
java -jar build/libs/safeson-perf.jar
```

This command will run all tests using all parsers.

To control which tests and parsers should run, and how many times, the following options are supported:

```
Options:

    runs <n>       - number of total runs
    warmups <n>    - numberof warmup runs (not measured)
    parsers <name> - run only named parsers (comma-separated)
    tests <values> - tests to run (comma-separated)

Available parsers:

    dsl-json
    gson
    jackson
    min-json
    safeson

Available tests:

    ints   - array of 1 million random integers
    pojo   - random object (to POJO) from memory
    rand   - random object from memory
    rap    - static object from jar resource (from Eclipse RAP)
```

## JMH Benchmarks

### Building

Run:

```bash
./gradlew jmhJar
```

### Running tests

To execute the benchmarks, use the runnable jar created by Gradle:

```bash
java -jar build/libs/jmh-run.jar
```
