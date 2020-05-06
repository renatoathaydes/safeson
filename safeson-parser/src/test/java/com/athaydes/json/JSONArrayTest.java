package com.athaydes.json;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONArrayTest implements TestHelper {

    final JSON json = new JSON();

    @Test
    public void canParseEmptyArray() {
        assertEquals(List.of(), json.parse("[]", List.class));
    }

    @Test
    public void canParseEmptyArrayWithWhitespaces() {
        assertEquals(List.of(), json.parse("  [    ]  ", List.class));
    }

    @Test
    public void canParseArrayOfNumbers() {
        assertEquals(List.of(0), json.parse("[0]", List.class));
        assertEquals(List.of(0, 1), json.parse("[0, 1]", List.class));
        assertEquals(List.of(30, 2000, 42), json.parse("[30 ,  2000, 42  ]", List.class));
        assertEquals(List.of(0.5, 1, 1.5), json.parse("[ 0.5, 1  ,1.5]", List.class));
    }

    @Test
    public void canParseArrayOfStrings() {
        assertEquals(List.of("foo"), json.parse("[\"foo\"]", List.class));
        assertEquals(List.of("foo", "bar"), json.parse("[\"foo\", \"bar\"]", List.class));
        assertEquals(List.of("[1,2,3]", "4", "[5,6]"), json.parse("[\"[1,2,3]\" ,  \"4\", \"[5,6]\"  ]", List.class));
        assertEquals(List.of("{}", "\u0048", "1e+9"), json.parse("[ \"{}\", \"\\u0048\"  ,\"1e+9\"]", List.class));
    }

    @Test
    public void canParseArrayOfArrays() {
        assertEquals(List.of(List.of()), json.parse("[[]]", List.class));
        assertEquals(List.of(List.of(0)), json.parse("[[0]]", List.class));
        assertEquals(List.of(List.of(0), 1), json.parse("  [[0], 1]  ", List.class));
        assertEquals(List.of(List.of(0), List.of(1)), json.parse("[[0], [1]]", List.class));
        assertEquals(List.of(List.of(0, 1), List.of(2, 3)), json.parse("[[0,1], [ 2,3 ]]", List.class));
        assertEquals(List.of(List.of(0, List.of(1)), List.of(List.of(2, 3))), json.parse("[[0,[1]], [ [2,3] ]]", List.class));

        assertEquals(List.of(List.of(List.of(List.of(List.of(List.of(List.of(List.of(List.of(List.of(List.of(List.of(List.of(List.of(List.of())))))))))))))),
                json.parse("[[[[[[[[[[[[[[[]]]]]]]]]]]]]]]", List.class));
        assertEquals(List.of(List.of(List.of(List.of(List.of(List.of(List.of(List.of(List.of(List.of(List.of(List.of(List.of(List.of(List.of(0))))))))))))))),
                json.parse("[[[[[[[[[[[[[[[0]]]]]]]]]]]]]]]", List.class));
    }

    @Test
    void rejectsBadlyFormedArrays() {
        assertThrowsJsonException(() -> json.parse("[", List.class), "Unterminated array", 0);
        assertThrowsJsonException(() -> json.parse("[[", List.class), "Unterminated array", 1);
        assertThrowsJsonException(() -> json.parse("[1, 2", List.class), "Unterminated array", 4);
        assertThrowsJsonException(() -> json.parse("[1,", List.class), "Unterminated array", 2);
        assertThrowsJsonException(() -> json.parse("[1}", List.class), "Expected ',' or ']', got '}'", 2);
        assertThrowsJsonException(() -> json.parse("[1, }", List.class), "Invalid literal", 4);
        assertThrowsJsonException(() -> json.parse("[1,,", List.class), "Invalid literal", 3);
        assertThrowsJsonException(() -> json.parse("]", List.class), "Expected array but got ']'", 0);
        assertThrowsJsonException(() -> json.parse(" ]", List.class), "Expected array but got ']'", 1);
        assertThrowsJsonException(() -> json.parse("[]]", List.class), "Illegal trailing content: ']'", 2);
        assertThrowsJsonException(() -> json.parse("[[[[[[[[]]]]]]]", List.class), "Unterminated array", 14);
    }
}
