import com.athaydes.json.JSON;
import com.dslplatform.json.DslJson;
import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SafeSONPerfTest {
    static final Map<String, Times> arrayTimesPerParser = new HashMap<>();
    static final Map<String, Times> objectTimesPerParser = new HashMap<>();

    public static void main(String[] args) throws Exception {
        var options = CliOptions.of(args);
        var parsers = new ArrayList<>(options.runOnlySafeSON ? List.of(new SafeSONParser()) : List.of(
                new SafeSONParser(),
                new JacksonParser(),
                new GsonParser(),
                new MinimalJsonParser(),
                new DslJsonParser()
        ));
        for (int i = 0; i < options.totalRuns; i++) {
            var collectTime = i >= options.warmupRuns;
            Collections.shuffle(parsers);
            for (Parser<?, ?> parser : parsers) {
                runParser(parser, collectTime, options);
            }
        }
        printResultsAsCsv(options);
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
                parser.verifyArraySize(array, 1_000_000);
                if (collectTime) {
                    arrayTimesPerParser.computeIfAbsent(parser.name(),
                            (ignore) -> new Times(options.totalRuns - options.warmupRuns)).add(total);
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
                    objectTimesPerParser.computeIfAbsent(parser.name(),
                            (ignore) -> new Times(options.totalRuns - options.warmupRuns)).add(total);
                }
            }
        }
    }

    private static void printResultsAsCsv(CliOptions options) {
        var measuredRuns = options.totalRuns - options.warmupRuns;
        List<TestResult> results = new ArrayList<>();
        if (options.testTypes.contains(TestType.INTS)) {
            results.add(new TestResult("Int Array", arrayTimesPerParser));
        }
        if (options.testTypes.contains(TestType.RAP)) {
            results.add(new TestResult("Real Object", objectTimesPerParser));
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

interface Parser<Obj, Arr> {
    String name();

    Obj parseObject(InputStream stream) throws Exception;

    Arr parseArray(InputStream stream) throws Exception;

    void verifyObjectSize(Obj object, int expectedSize);

    void verifyArraySize(Arr object, int expectedSize);

    default void assertSize(int expectedSize, int actualSize) {
        if (expectedSize != actualSize) {
            throw new AssertionError(name() + " FAILED: expected size" + expectedSize + " but was " + actualSize);
        }
    }
}

final class JacksonParser implements Parser<JsonNode, JsonNode> {
    final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String name() {
        return "Jackson 2.11.0";
    }

    @Override
    public JsonNode parseObject(InputStream stream) throws Exception {
        return mapper.readTree(stream);
    }

    @Override
    public JsonNode parseArray(InputStream stream) throws Exception {
        return mapper.readTree(stream);
    }

    @Override
    public void verifyObjectSize(JsonNode rootNode, int expectedSize) {
        assertSize(expectedSize, rootNode.size());
    }

    @Override
    public void verifyArraySize(JsonNode rootNode, int expectedSize) {
        assertSize(expectedSize, rootNode.size());
    }
}

final class GsonParser implements Parser<Map<?, ?>, List<?>> {
    final Gson gson = new Gson();

    @Override
    public String name() {
        return "GSON 2.8.6";
    }

    @Override
    public Map<?, ?> parseObject(InputStream stream) throws Exception {
        return gson.fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), Map.class);
    }

    @Override
    public List<?> parseArray(InputStream stream) throws Exception {
        return gson.fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), List.class);
    }

    @Override
    public void verifyObjectSize(Map<?, ?> object, int expectedSize) {
        assertSize(expectedSize, object.size());
    }

    @Override
    public void verifyArraySize(List<?> object, int expectedSize) {
        assertSize(expectedSize, object.size());
    }
}

final class MinimalJsonParser implements Parser<JsonObject, JsonArray> {

    @Override
    public String name() {
        return "min-json 0.9.5";
    }

    @Override
    public JsonObject parseObject(InputStream stream) throws Exception {
        return Json.parse(new InputStreamReader(stream, StandardCharsets.UTF_8)).asObject();
    }

    @Override
    public JsonArray parseArray(InputStream stream) throws Exception {
        return Json.parse(new InputStreamReader(stream, StandardCharsets.UTF_8)).asArray();
    }

    @Override
    public void verifyObjectSize(JsonObject object, int expectedSize) {
        assertSize(expectedSize, object.size());
    }

    @Override
    public void verifyArraySize(JsonArray object, int expectedSize) {
        assertSize(expectedSize, object.size());
    }
}

final class SafeSONParser implements Parser<Map<?, ?>, List<?>> {

    final JSON parser = new JSON();

    @Override
    public String name() {
        return "SafeSON 1.0";
    }

    @Override
    public Map<?, ?> parseObject(InputStream stream) throws Exception {
        return parser.parse(stream, Map.class);
    }

    @Override
    public List<?> parseArray(InputStream stream) throws Exception {
        return parser.parse(stream, List.class);
    }

    @Override
    public void verifyObjectSize(Map<?, ?> object, int expectedSize) {
        assertSize(expectedSize, object.size());
    }

    @Override
    public void verifyArraySize(List<?> object, int expectedSize) {
        assertSize(expectedSize, object.size());
    }
}

final class DslJsonParser implements Parser<Map<?, ?>, long[]> {

    final DslJson<Object> json = new DslJson();

    @Override
    public String name() {
        return "dsl-json 1.9.5";
    }

    @Override
    public Map<?, ?> parseObject(InputStream stream) throws Exception {
        return json.deserialize(Map.class, stream);
    }

    @Override
    public long[] parseArray(InputStream stream) throws Exception {
        return json.deserialize(long[].class, stream);
    }

    @Override
    public void verifyObjectSize(Map<?, ?> object, int expectedSize) {
        assertSize(expectedSize, object.size());
    }

    @Override
    public void verifyArraySize(long[] object, int expectedSize) {
        assertSize(expectedSize, object.length);
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