package benchmarks;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import parsers.DslJsonParser;
import parsers.GsonParser;
import parsers.JacksonParser;
import parsers.MinimalJsonParser;
import parsers.SafeSONParser;

import static benchmarks.BenchmarkHelper.runWithIntegers;
import static benchmarks.BenchmarkHelper.runWithObject;

public class InputStreamSourcesBenchmark {

    @State(Scope.Thread)
    public static class ParserState {
        GsonParser gson;
        JacksonParser jackson;
        MinimalJsonParser minJson;
        SafeSONParser safeson;
        DslJsonParser dslJson;

        @Setup(Level.Iteration)
        public void setup() {
            gson = new GsonParser();
            jackson = new JacksonParser();
            minJson = new MinimalJsonParser();
            safeson = new SafeSONParser();
            dslJson = new DslJsonParser();
        }
    }

    @Benchmark
    public Object gsonObject(ParserState input) throws Exception {
        return runWithObject(input.gson);
    }

    @Benchmark
    public Object jacksonObject(ParserState input) throws Exception {
        return runWithObject(input.jackson);
    }

    @Benchmark
    public Object minJsonObject(ParserState input) throws Exception {
        return runWithObject(input.minJson);
    }

    @Benchmark
    public Object safesonObject(ParserState input) throws Exception {
        return runWithObject(input.safeson);
    }

    @Benchmark
    public Object dslJsonObject(ParserState input) throws Exception {
        return runWithObject(input.dslJson);
    }

    @Benchmark
    public Object gsonIntegers(ParserState input) throws Exception {
        return runWithIntegers(input.gson);
    }

    @Benchmark
    public Object jacksonIntegers(ParserState input) throws Exception {
        return runWithIntegers(input.jackson);
    }

    @Benchmark
    public Object minJsonIntegers(ParserState input) throws Exception {
        return runWithIntegers(input.minJson);
    }

    @Benchmark
    public Object safesonIntegers(ParserState input) throws Exception {
        return runWithIntegers(input.safeson);
    }

    @Benchmark
    public Object dslJsonIntegers(ParserState input) throws Exception {
        return runWithIntegers(input.dslJson);
    }

}
