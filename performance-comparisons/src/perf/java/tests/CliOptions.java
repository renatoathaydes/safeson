package tests;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;
import static tests.SafeSONPerfTest.loadAllParsers;

final class CliOptions {
    final int totalRuns;
    final int warmupRuns;
    final Map<String, Parser<?, ?>> parsers;
    final Set<TestType> testTypes;

    public CliOptions(int totalRuns, int warmupRuns, Map<String, Parser<?, ?>> parsers, Set<TestType> testTypes) {
        this.totalRuns = totalRuns;
        this.warmupRuns = warmupRuns;
        this.parsers = parsers;
        this.testTypes = testTypes;
    }

    static CliOptions of(String[] args) {
        int totalRuns = 1100;
        int warmupRuns = 100;
        Set<TestType> testTypes = Arrays.stream(TestType.values()).collect(Collectors.toSet());
        Map<String, Parser<?, ?>> parsers = loadAllParsers();
        for (int i = 0; i < args.length; i++) {
            var opt = args[i];
            switch (opt) {
                case "runs":
                    if (i + 1 < args.length) {
                        totalRuns = Integer.parseInt(args[++i]);
                    } else {
                        return usage("Missing argument for runs", parsers.keySet());
                    }
                    break;
                case "warmups":
                    if (i + 1 < args.length) {
                        warmupRuns = Integer.parseInt(args[++i]);
                    } else {
                        return usage("Missing argument for warmup", parsers.keySet());
                    }
                    break;
                case "parsers":
                    if (i + 1 < args.length) {
                        var selectedParsers = Arrays.stream(args[++i].split(",")).map(String::trim)
                                .collect(Collectors.toSet());
                        if (!parsers.keySet().containsAll(selectedParsers)) {
                            return usage("Invalid parsers argument, valid parsers are " + parsers, parsers.keySet());
                        } else {
                            parsers.keySet().removeIf(p -> !selectedParsers.contains(p));
                            if (parsers.isEmpty()) {
                                System.err.println("No parsers selected!");
                                System.exit(1);
                            }
                        }
                    } else {
                        return usage("Missing argument for tests", parsers.keySet());
                    }
                    break;
                case "tests":
                    if (i + 1 < args.length) {
                        testTypes = Arrays.stream(args[++i].split(",")).map(String::trim)
                                .map(String::toUpperCase)
                                .map(TestType::valueOf)
                                .collect(Collectors.toSet());
                    } else {
                        return usage("Missing argument for tests", parsers.keySet());
                    }
                    break;
                default:
                    return usage("Bad option: " + opt, parsers.keySet());
            }
        }
        if (warmupRuns >= totalRuns) {
            return usage("Bad arguments: warmups must be less than (total) runs", parsers.keySet());
        }
        return new CliOptions(totalRuns, warmupRuns, parsers, testTypes);
    }

    private static CliOptions usage(String error, Set<String> parsers) {
        var parserItems = parsers.stream().sorted().map(s -> "    " + s + "\n").collect(joining());
        System.out.println("ERROR - " + error);
        System.out.println("\nSafeSON Performance Tests\n\n" +
                "Usage:\n" +
                "  java -jar perf-test-all.jar <options>\n" +
                "\n" +
                "Options:\n\n" +
                "    runs <n>       - number of total runs\n" +
                "    warmups <n>    - numberof warmup runs (not measured)\n" +
                "    parsers <name> - run only named parsers (comma-separated)\n" +
                "    tests <values> - tests to run (comma-separated)\n\n" +
                "Available parsers:\n\n" +
                parserItems + "\n" +
                "Available tests:\n\n" +
                "    ints   - array of 1 million random integers\n" +
                "    pojo   - random object (to POJO) from memory\n" +
                "    rand   - random object (to Map) from memory\n" +
                "    rap    - static object from jar resource (from Eclipse RAP)\n");
        System.exit(1);
        return null;
    }
}

enum TestType {
    INTS, RAP, RAND, POJO
}
