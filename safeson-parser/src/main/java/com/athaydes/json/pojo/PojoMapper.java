package com.athaydes.json.pojo;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;

public final class PojoMapper<T> {

    private final List<PojoConstructor<T>> constructors;

    public PojoMapper(List<PojoConstructor<T>> constructors) {
        if (constructors.isEmpty()) {
            throw new IllegalArgumentException("Cannot create POJO Mapper, no constructors available");
        }
        this.constructors = constructors;
    }

    public List<PojoConstructor<T>> getConstructors() {
        return constructors;
    }

    @SuppressWarnings("unchecked")
    public static <T> PojoMapper<T> of(Class<T> type) {
        var constructors = Arrays.stream(type.getConstructors()).map(c -> {
            var params = Arrays.stream(c.getParameters()).map(p -> {
                if (!p.isNamePresent()) throw new IllegalArgumentException("Cannot map " + type.getName() +
                        " as its parameter names are not present. " +
                        "Re-compile it with the -parameters flag.");
                return p;
            }).collect(toList());
            return new PojoConstructor<T>((Constructor<T>) c, params);
        }).collect(toList());
        return new PojoMapper<>(constructors);
    }

}
