package com.athaydes.json.pojo;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

public final class PojoMapper<T> {
    private static final Map<Class<?>, PojoMapper<?>> mappers = new HashMap<>();

    private final List<PojoConstructor<T>> constructors;

    private PojoMapper(List<PojoConstructor<T>> constructors) {
        if (constructors.isEmpty()) {
            throw new PojoException("Cannot create POJO Mapper, no constructors available");
        }
        this.constructors = constructors;
    }

    @SuppressWarnings("unchecked")
    public static <T> PojoMapper<T> of(Class<T> type) {
        if (mappers.containsKey(type)) {
            return (PojoMapper<T>) mappers.get(type);
        }

        var constructors = Arrays.stream(type.getConstructors()).map(c -> {
            var params = Arrays.stream(c.getParameters()).map(p -> {
                if (!p.isNamePresent()) throw new PojoException("Cannot map " + type.getName() +
                        " as its parameter names are not present. " +
                        "Re-compile it with the -parameters flag.");
                return p;
            }).collect(toList());
            return new PojoConstructor<T>((Constructor<T>) c, params);
        }).collect(toList());

        var mapper = new PojoMapper<>(constructors);
        mappers.put(type, mapper);
        return mapper;
    }

    static Pojos pojos(Class<?>... types) {
        var pojoMappers = Stream.of(types)
                .filter(Predicate.not(Pojos::isNativeJsonType))
                .map(PojoMapper::of).collect(toSet());

        var map = mappers.entrySet().stream().filter(e -> pojoMappers.contains(e.getValue()))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

        return new Pojos(map);
    }

    public List<PojoConstructor<T>> getConstructors() {
        return constructors;
    }

}
