package com.athaydes.json;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONConfigTest implements TestHelper {
    @Test
    void stringBufferCapacity() {
        var json = new JSON(JsonConfig.builder().withMaxStringLength(4096).build());

        // grows once
        var txt = IntStream.range(0, 1025).map((i) -> 1).mapToObj(Integer::toString).collect(Collectors.joining());
        assertEquals(txt, json.parse("\"" + txt + "\"", String.class));

        // grows twice
        txt = IntStream.range(0, 2050).map((i) -> 1).mapToObj(Integer::toString).collect(Collectors.joining());
        assertEquals(txt, json.parse("\"" + txt + "\"", String.class));

        // can fill up to near capacity (it needs to keep 6 bytes for possible unicode sequences, plus the quote)
        txt = IntStream.range(0, 4088).map((i) -> 1).mapToObj(Integer::toString).collect(Collectors.joining());
        assertEquals(txt, json.parse("\"" + txt + "\"", String.class));

        // blows up if maximum allowed capacity exceeded
        var bigText = IntStream.range(0, 5000).map((i) -> 1).mapToObj(Integer::toString).collect(Collectors.joining());
        assertThrowsJsonException(() -> json.parse("\"" + bigText + "\"", String.class),
                "Maximum string length has been reached, unable to allocate enough memory for String",
                4090);
    }

    @Test
    public void maxWhitespace() {
        var json = new JSON(JsonConfig.builder().withMaxWhitespace(4).build());
        assertEquals("foo", json.parse("    \"foo\"", String.class));
        assertEquals("foo", json.parse("    \"foo\"    ", String.class));

        assertThrowsJsonException(() -> json.parse("     \"foo\"", String.class),
                "Too much whitespace", 4);
        assertThrowsJsonException(() -> json.parse("   \"foo\"     ", String.class),
                "Too much whitespace", 12);
    }

    @Test
    public void maxRecursion() {
        var json = new JSON(JsonConfig.builder().withMaxRecursionDepth(4).build());
        assertEquals(List.of(List.of(List.of(List.of()))), json.parse("[[[[]]]]", List.class));

        assertThrowsJsonException(() -> json.parse("[[[[[", List.class), "Recursion limit breached", 4);
    }

    @Test
    public void concatenatedContents() {
        var json = new JSON(JsonConfig.builder().withConsumeTrailingContent(false).build());
        var documents = "123:{\"foo\": true};[1,2,3];\"bar\"; 0.43: true: false:   null: 0 ";
        var bytes = new ByteArrayInputStream(documents.getBytes(StandardCharsets.UTF_8));

        assertEquals(123, json.parse(bytes));
        assertEquals(Map.of("foo", true), json.parse(bytes));
        assertEquals(List.of(1, 2, 3), json.parse(bytes));
        assertEquals("bar", json.parse(bytes));
        assertEquals(0.43, (double) json.parse(bytes), 1e-12);
        assertEquals(true, json.parse(bytes));
        assertEquals(false, json.parse(bytes));
        assertEquals(null, json.parse(bytes));
        assertEquals(0, json.parse(bytes));
    }

    @Test
    void keepObjectFirstKey() {
        var json = new JSON(JsonConfig.builder().withDuplicateKeyStrategy(DuplicateKeyStrategy.KEEP_FIRST).build());
        assertEquals(Map.of("a", 1, "b", 2), json.parse("{\"a\": 1,\"b\": 2,\"a\": 2,\"b\": 3}"));
    }

    @Test
    void keepObjectLastKey() {
        var json = new JSON(JsonConfig.builder().withDuplicateKeyStrategy(DuplicateKeyStrategy.KEEP_LAST).build());
        assertEquals(Map.of("a", 2, "b", 3), json.parse("{\"a\": 1,\"b\": 2,\"a\": 2,\"b\": 3}"));
    }

    @Test
    void failDuplicateKeys() {
        var json = new JSON(JsonConfig.builder().withDuplicateKeyStrategy(DuplicateKeyStrategy.FAIL).build());
        assertThrowsJsonException(() -> json.parse("{\"a\": 1,\"b\": 2,\"a\": 2,\"b\": 3}"),
                "Duplicate key: a", 15);
    }

}
