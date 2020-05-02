package com.athaydes.json;

public class JsonException extends RuntimeException {
    public JsonException(int index, String message) {
        super("JSON syntax error at index " + index + ": " + message);
    }
}
