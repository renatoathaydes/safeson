package com.athaydes.json;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONNullTest implements TestHelper {

    final JSON json = new JSON();

    @Test
    public void canParseNull() {
        assertEquals(null, json.parse("null"));
        assertEquals(null, json.parse("null", Void.class));
        assertEquals(null, json.parse("null", Object.class));
    }

    @Test
    public void canParseNullWithWhitespace() {
        assertEquals(null, json.parse("null"));
        assertEquals(null, json.parse(" null", Void.class));
        assertEquals(null, json.parse("   null  ", Object.class));
    }

    @Test
    public void rejectsWrongCase() {
        assertThrowsJsonException(()-> json.parse("Null", Void.class),
                "Invalid literal", 0);
        assertThrowsJsonException(()-> json.parse("NULL", Void.class),
                "Invalid literal", 0);
        assertThrowsJsonException(()-> json.parse("nuLL", Void.class),
                "Invalid literal", 2);
        assertThrowsJsonException(()-> json.parse("  nuLL", Void.class),
                "Invalid literal", 4);
    }
}
