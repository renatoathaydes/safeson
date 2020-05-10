package com.athaydes.json;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public interface TestHelper {
    default void assertThrowsJsonException(Executable executable,
                                           String expectedMessage,
                                           int index) {
        var ex = Assertions.assertThrows(JsonException.class, executable);
        var msg = new JsonException(index, expectedMessage).getMessage();
        if (!msg.equals(ex.getMessage())) {
            fail("Expected : " + msg + "\nActual   : " + ex.getMessage(), ex);
        }
        assertEquals(msg, ex.getMessage());
    }
}
