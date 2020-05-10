import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

final class CliOptions {
    final int totalRuns;
    final int warmupRuns;
    final boolean runOnlySafeSON;
    final Set<TestType> testTypes;

    public CliOptions(int totalRuns, int warmupRuns, boolean runOnlySafeSON, Set<TestType> testTypes) {
        this.totalRuns = totalRuns;
        this.warmupRuns = warmupRuns;
        this.runOnlySafeSON = runOnlySafeSON;
        this.testTypes = testTypes;
    }

    static CliOptions of(String[] args) {
        int totalRuns = 1100;
        int warmupRuns = 100;
        boolean runOnlySafeSON = false;
        Set<TestType> testTypes = Set.of(TestType.INTS, TestType.RAP);
        for (int i = 0; i < args.length; i++) {
            var opt = args[i];
            switch (opt) {
                case "runs":
                    if (i + 1 < args.length) {
                        totalRuns = Integer.parseInt(args[++i]);
                    } else {
                        return usage("Missing argument for runs");
                    }
                    break;
                case "warmups":
                    if (i + 1 < args.length) {
                        warmupRuns = Integer.parseInt(args[++i]);
                    } else {
                        return usage("Missing argument for warmup");
                    }
                    break;
                case "safeson":
                    runOnlySafeSON = true;
                    break;
                case "tests":
                    if (i + 1 < args.length) {
                        testTypes = Arrays.stream(args[++i].split(",")).map(String::trim)
                                .map(String::toUpperCase)
                                .map(TestType::valueOf)
                                .collect(Collectors.toSet());
                    } else {
                        return usage("Missing argument for tests");
                    }
                    break;
                default:
                    return usage("Bad option: " + opt);
            }
        }
        return new CliOptions(totalRuns, warmupRuns, runOnlySafeSON, testTypes);
    }

    private static CliOptions usage(String error) {
        System.out.println("ERROR - " + error);
        System.out.println("\nSafeSON Performance Tests\n\n" +
                "Usage:\n" +
                "  java -jar perf-test-all.jar <options>\n" +
                "\n" +
                "Options:\n\n" +
                "    runs <n>       - number of total runs\n" +
                "    warmups <n>    - numberof warmup runs (not measured)\n" +
                "    safeson        - run only SafeSON\n" +
                "    tests <values> - tests to run\n\n" +
                "Available tests:\n\n" +
                "    ints   - array of 1 million random integers\n" +
                "    rand   - random object from memory\n" +
                "    rap    - static object from jar resource (from Eclipse RAP)\n");
        System.exit(1);
        return null;
    }
}

enum TestType {
    INTS, RAP, RAND
}
