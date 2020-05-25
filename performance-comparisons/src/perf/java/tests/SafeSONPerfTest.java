package tests;

import data.TestObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

public final class SafeSONPerfTest {
    static final Map<TestType, Map<String, Times>> times = new HashMap<>();

    public static void main(String[] args) throws Exception {
        var options = CliOptions.of(args);
        var parsers = new ArrayList<>(options.parsers.values());
        for (int i = 0; i < options.totalRuns; i++) {
            var collectTime = i >= options.warmupRuns;
            Collections.shuffle(parsers);
            for (Parser<?, ?> parser : parsers) {
                runParser(parser, collectTime, options);
            }
        }
        printResultsAsCsv(options);
    }

    public static Map<String, Parser<?, ?>> loadAllParsers() {
        var parsers = new HashMap<String, Parser<?, ?>>();
        for (Parser<?, ?> parser : ServiceLoader.load(Parser.class)) {
            parsers.put(parser.cliId(), parser);
        }
        System.out.println("PARSERS" + parsers);
        return parsers;
    }

    private static <Obj, Arr> void runParser(Parser<Obj, Arr> parser,
                                             boolean collectTime,
                                             CliOptions options) throws Exception {
        if (options.testTypes.contains(TestType.INTS)) {
            try (var json = new BufferedInputStream(
                    SafeSONPerfTest.class.getResourceAsStream("/integers.json"))) {
                var start = System.nanoTime();
                Arr array = parser.parseArray(json);
                var total = System.nanoTime() - start;
                parser.verifyArraySize(array, 100_000);
                if (collectTime) {
                    collectTime(parser, options, total, TestType.INTS);
                }
            }
        }
        if (options.testTypes.contains(TestType.RAP)) {
            try (var json = new BufferedInputStream(
                    SafeSONPerfTest.class.getResourceAsStream("/rap.json"))) {
                var start = System.nanoTime();
                Obj object = parser.parseObject(json);
                var total = System.nanoTime() - start;
                parser.verifyObjectSize(object, 2);
                if (collectTime) {
                    collectTime(parser, options, total, TestType.RAP);
                }
            }
        }
        if (options.testTypes.contains(TestType.RAND)) {
            var json = new ByteArrayInputStream(RandomObjectGenerator.generateRandom().getBytes(StandardCharsets.UTF_8));
            var start = System.nanoTime();
            Obj object = parser.parseObject(json);
            var total = System.nanoTime() - start;
            parser.verifyObjectSize(object, 6);
            if (collectTime) {
                collectTime(parser, options, total, TestType.RAND);
            }
        }
        if (options.testTypes.contains(TestType.POJO)) {
            var json = new ByteArrayInputStream(RandomObjectGenerator.generateRandom().getBytes(StandardCharsets.UTF_8));
            var start = System.nanoTime();
            try {
                TestObject object = parser.parsePojo(json);
                var total = System.nanoTime() - start;
                parser.verifyPojo(object);
                if (collectTime) {
                    collectTime(parser, options, total, TestType.POJO);
                }
            } catch (Exception e) {
                if (collectTime) {
                    collectTime(parser, options, -1L, TestType.POJO);
                }
            }
        }
    }

    private static void collectTime(Parser<?, ?> parser, CliOptions options,
                                    long total, TestType testType) {
        times.computeIfAbsent(testType, k -> new HashMap<>())
                .computeIfAbsent(parser.name(),
                        (ignore) -> new Times(options.totalRuns - options.warmupRuns)).add(total);
    }

    private static void printResultsAsCsv(CliOptions options) {
        var measuredRuns = options.totalRuns - options.warmupRuns;
        List<TestResult> results = new ArrayList<>();
        if (times.containsKey(TestType.INTS)) {
            results.add(new TestResult("Int Array", times.get(TestType.INTS)));
        }
        if (times.containsKey(TestType.RAP)) {
            results.add(new TestResult("Static Object", times.get(TestType.RAP)));
        }
        if (times.containsKey(TestType.RAND)) {
            results.add(new TestResult("Random Object", times.get(TestType.RAND)));
        }
        if (times.containsKey(TestType.POJO)) {
            results.add(new TestResult("Random POJO", times.get(TestType.POJO)));
        }
        CsvReporter.printReport(results, measuredRuns);
    }
}

final class Times {
    private int index;
    private final long[] all;

    public Times(int runs) {
        all = new long[runs];
    }

    void add(long time) {
        all[index++] = time;
    }

    String get(int i) {
        return Long.toString(all[i]);
    }
}

final class TestResult {
    final String name;
    final Map<String, Times> timesPerParser;

    public TestResult(String name, Map<String, Times> timesPerParser) {
        this.name = name;
        this.timesPerParser = timesPerParser;
    }
}