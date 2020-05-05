package com.athaydes.json;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONConfigTest implements TestHelper {
    @Test
    void stringBufferCapacity() {
        var json = new JSON(new JsonConfig(4096, 4, 4, true));

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
        var json = new JSON(new JsonConfig(4096, 4, 4, true));
        assertEquals("foo", json.parse("    \"foo\"", String.class));
        assertEquals("foo", json.parse("    \"foo\"    ", String.class));

        assertThrowsJsonException(() -> json.parse("     \"foo\"", String.class),
                "Too much whitespace", 4);
        assertThrowsJsonException(() -> json.parse("   \"foo\"     ", String.class),
                "Too much whitespace", 12);
    }

    @Test
    public void maxRecursion() {
        var json = new JSON(new JsonConfig(16, 4, 4, true));
        assertEquals(List.of(List.of(List.of(List.of()))), json.parse("[[[[]]]]", List.class));

        assertThrowsJsonException(() -> json.parse("[[[[[", List.class), "Recursion limit breached", 4);
    }
}
