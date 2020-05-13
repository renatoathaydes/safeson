package com.athaydes.json.pojo;

public class PojoException extends RuntimeException {
    public PojoException(String message) {
        super(message);
    }

    public PojoException(String message, Exception cause) {
        super(message, cause);
    }
}
