package com.athaydes.json;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.function.Executable;

public interface TestHelper {
    default void assertThrowsJsonException(Executable executable,
                                           String expectedMessage,
                                           int index) {
        var ex = Assertions.assertThrows(JsonException.class, executable);
        var msg = new JsonException(index, expectedMessage).getMessage();
        Assertions.assertEquals(msg, ex.getMessage());
    }
}
