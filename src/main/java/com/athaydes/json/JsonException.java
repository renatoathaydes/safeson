package com.athaydes.json;

public class JsonException extends RuntimeException {
    public JsonException(int index, String message) {
        super("JSON parsing error at index " + index + ": " + message);
    }
}
