package benchmarks;

import data.TestObject;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import parsers.GsonParser;
import parsers.JacksonParser;
import parsers.MinimalJsonParser;
import parsers.SafeSONParser;
import tests.RandomObjectGenerator;

import static benchmarks.BenchmarkHelper.runWithPojo;

public class RandomMemoryPojosBenchmark {

    @State(Scope.Thread)
    public static class ParserState {
        public static final int POJO_COUNT = 1_000;
        GsonParser gson;
        JacksonParser jackson;
        MinimalJsonParser minJson;
        SafeSONParser safeson;

        String[] objects = new String[POJO_COUNT];
        int objectIndex = 0;

        @Setup(Level.Iteration)
        public void setup() {
            gson = new GsonParser();
            jackson = new JacksonParser();
            minJson = new MinimalJsonParser();
            safeson = new SafeSONParser();
            for (int i = 0; i < POJO_COUNT; i++) {
                objects[i] = RandomObjectGenerator.generateRandom();
            }
        }

        String nextPojo() {
            return objects[objectIndex++ % POJO_COUNT];
        }
    }

    @Benchmark
    public TestObject gsonPojo(ParserState input) throws Exception {
        return runWithPojo(input.gson, input.nextPojo());
    }

    @Benchmark
    public TestObject jacksonPojo(ParserState input) throws Exception {
        return runWithPojo(input.jackson, input.nextPojo());
    }

    @Benchmark
    public TestObject minJsonPojo(ParserState input) throws Exception {
        return runWithPojo(input.minJson, input.nextPojo());
    }

    @Benchmark
    public TestObject safesonPojo(ParserState input) throws Exception {
        return runWithPojo(input.safeson, input.nextPojo());
    }

}
