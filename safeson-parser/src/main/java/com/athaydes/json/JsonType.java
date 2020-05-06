package com.athaydes.json;

import java.util.List;
import java.util.Map;

public enum JsonType {
    NULL(Void.class),
    STRING(String.class),
    NUMBER(Number.class),
    BOOLEAN(Boolean.class),
    OBJECT(Map.class),
    ARRAY(List.class);

    private final Class<?> javaType;

    JsonType(Class<?> javaType) {
        this.javaType = javaType;
    }

    public Class<?> getJavaType() {
        return javaType;
    }
}
