# JSON Parser Performance Comparisons

This project performs a Performance Test Comparison between the SafeSON Parser and
several popular Java JSON Parsers:

* GSON
* Jackson
* minimal-json
* DSL-JSON

## Building

Run:

```bash
./gradlew jar
```

## Running tests

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
