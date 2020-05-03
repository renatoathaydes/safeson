package com.athaydes.json;

public class JsonException extends RuntimeException {
    public JsonException(int index, String message) {
        super("JSON parsing error at index " + index + ": " + message);
    }

    public JsonException(Exception cause) {
        super("Problem while parsing JSON", cause);
    }
}
