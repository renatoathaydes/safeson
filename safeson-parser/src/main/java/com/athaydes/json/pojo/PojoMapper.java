package com.athaydes.json.pojo;

import java.lang.reflect.Constructor;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Comparator.comparing;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

public final class PojoMapper<T> {
    private static final Map<Class<?>, PojoMapper<?>> mappers = new HashMap<>();

    private final JsonType pojoType;
    private final byte[][] keyBytesCache;
    private final boolean[] possibilities;
    private final Map<String, JsonType> parameterTypeByName;
    private final List<String> parameterNames;
    private final List<PojoConstructor<T>> constructors;

    private PojoMapper(Class<T> pojoType, List<PojoConstructor<T>> constructors) {
        this.pojoType = new JsonType.Scalar(pojoType);
        if (constructors.isEmpty()) {
            throw new PojoException("Cannot create POJO Mapper, no constructors available");
        }
        this.constructors = constructors;
        if (constructors.size() == 1) {
            parameterTypeByName = constructors.get(0).getParamTypeByName();
            parameterNames = new ArrayList<>(constructors.get(0).getParamNames());
        } else {
            var map = new HashMap<String, JsonType>();
            var names = new ArrayList<String>();
            for (PojoConstructor<T> constructor : constructors) {
                map.putAll(constructor.getParamTypeByName());
                names.addAll(constructor.getParamNames());
            }
            parameterTypeByName = Collections.unmodifiableMap(map);
            parameterNames = Collections.unmodifiableList(names);
        }
        this.keyBytesCache = createCache();
        this.possibilities = new boolean[keyBytesCache.length];
    }

    private byte[][] createCache() {
        var cache = new byte[parameterNames.size()][];
        var i = 0;
        for (String key : parameterNames) {
            cache[i++] = key.getBytes(StandardCharsets.UTF_8);
        }
        return cache;
    }

    public String keyFor(byte[] bytes, int len) {
        int candidates = 0;
        for (int i = 0; i < possibilities.length; i++) {
            if (keyBytesCache[i].length == len) {
                possibilities[i] = true;
                candidates++;
            } else {
                possibilities[i] = false;
            }
        }
        if (candidates > 0) {
            for (int possIndex = 0; possIndex < possibilities.length; possIndex++) {
                if (possibilities[possIndex]) {
                    var candidate = keyBytesCache[possIndex];
                    var chosenOne = true;
                    for (int i = 0; i < len; i++) {
                        if (candidate[i] != bytes[i]) {
                            possibilities[possIndex] = false;
                            candidates--;
                            chosenOne = false;
                            break;
                        }
                    }
                    if (chosenOne) {
                        return parameterNames.get(possIndex);
                    }
                }
                if (candidates == 0) {
                    break;
                }
            }
        }
        return new String(bytes, 0, len, StandardCharsets.UTF_8);
    }

    public List<PojoConstructor<T>> getConstructors() {
        return constructors;
    }

    public JsonType getPojoType() {
        return pojoType;
    }

    public JsonType getTypeOf(String key) {
        return parameterTypeByName.get(key);
    }

    public T create(Map<String, Object> args) throws Exception {
        Set<String> argNames = args.keySet();
        for (PojoConstructor<T> constructor : constructors) {
            Set<String> paramNames = constructor.getParamNames();
            if (argNames.containsAll(constructor.getMandatoryParamNames())) {
                var arguments = new Object[paramNames.size()];
                int i = 0;
                for (String paramName : paramNames) {
                    boolean isOptional = constructor.getTypeOfParameter(paramName)
                            .match(scalar -> false, compound -> compound.getContainer() == JsonType.Container.OPTIONAL);
                    arguments[i++] = isOptional
                            ? (args.containsKey(paramName) ? args.get(paramName) : Optional.empty())
                            : args.get(paramName);
                }
                return constructor.createPojo(arguments);
            }
        }
        throw new PojoException("Unable to create instance of " + pojoType.getValueType() + ", " +
                "available fields do not match any available constructor: " + argNames);
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
        })
                // make sure the longest constructors come first
                .sorted(comparing(c -> -c.getMandatoryParamNames().size()))
                .collect(toList());

        var mapper = new PojoMapper<>(type, constructors);
        mappers.put(type, mapper);
        return mapper;
    }

    static Pojos pojos(Class<?>... types) {
        var pojoMappers = Stream.of(types)
                .filter(not(Pojos::isNativeJsonType))
                .map(PojoMapper::of).collect(toSet());

        var map = mappers.entrySet().stream().filter(e -> pojoMappers.contains(e.getValue()))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

        return Pojos.of(map);
    }
}
