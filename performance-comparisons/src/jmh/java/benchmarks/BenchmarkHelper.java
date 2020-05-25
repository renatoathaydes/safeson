package benchmarks;

import data.TestObject;
import tests.Parser;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

abstract class BenchmarkHelper {
    static TestObject runWithPojo(Parser<?, ?> parser, String jsonString) throws Exception {
        var json = new ByteArrayInputStream(jsonString.getBytes(StandardCharsets.UTF_8));
        return parser.parsePojo(json);
    }

    static Object runWithObject(Parser<?, ?> parser) throws Exception {
        var stream = new BufferedInputStream(InputStreamSourcesBenchmark.class.getResourceAsStream("/rap.json"));
        return parser.parseObject(stream);
    }

    static Object runWithIntegers(Parser<?, ?> parser) throws Exception {
        var stream = new BufferedInputStream(InputStreamSourcesBenchmark.class.getResourceAsStream("/integers.json"));
        return parser.parseArray(stream);
    }
}
