package com.athaydes.json;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONBooleanTest implements TestHelper {

    final JSON json = new JSON();

    @Test
    public void canParseBoolean() {
        assertEquals(true, json.parse("true"));
        assertEquals(true, json.parse("true", Boolean.class));
        assertEquals(true, json.parse("true", Object.class));

        assertEquals(false, json.parse("false"));
        assertEquals(false, json.parse("false", Boolean.class));
        assertEquals(false, json.parse("false", Object.class));
    }

    @Test
    public void canParseBooleanWithWhitespaces() {
        assertEquals(true, json.parse(" true"));
        assertEquals(true, json.parse("true ", Boolean.class));
        assertEquals(true, json.parse("  true   ", Object.class));

        assertEquals(false, json.parse(" false"));
        assertEquals(false, json.parse("false   ", Boolean.class));
        assertEquals(false, json.parse("    false   ", Object.class));
    }

    @Test
    public void rejectsWrongCase() {
        assertThrowsJsonException(() -> json.parse("True", Boolean.class),
                "Invalid literal", 0);
        assertThrowsJsonException(() -> json.parse("TRUE", Boolean.class),
                "Invalid literal", 0);
        assertThrowsJsonException(() -> json.parse("truE", Boolean.class),
                "Invalid literal", 3);
        assertThrowsJsonException(() -> json.parse("falSE", Boolean.class),
                "Invalid literal", 3);
        assertThrowsJsonException(() -> json.parse("  falSE", Boolean.class),
                "Invalid literal", 5);
    }

}
