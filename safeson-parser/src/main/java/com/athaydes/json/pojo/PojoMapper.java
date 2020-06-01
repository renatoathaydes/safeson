package com.athaydes.json.pojo;

import com.athaydes.json.DuplicateKeyStrategy;

import java.lang.reflect.Constructor;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private final PojoCreator<T> creator;

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
            var typeConflicts = new ArrayList<String>(2);
            for (PojoConstructor<T> constructor : constructors) {
                constructor.getParamTypeByName().forEach((name, type) -> {
                    var oldType = map.put(name, type);
                    if (oldType != null && !oldType.equals(type)) {
                        typeConflicts.add(String.format("%s (%s VS %s)", name, oldType, type));
                    }
                });
                names.addAll(constructor.getParamNames());
            }
            if (!typeConflicts.isEmpty()) {
                throw new PojoException("Conflicting types for parameters: " + String.join(", ", typeConflicts));
            }
            parameterTypeByName = Collections.unmodifiableMap(map);
            parameterNames = Collections.unmodifiableList(names);
        }
        this.creator = new PojoCreator<>(constructors);
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

    // keys are cached, hence we may compare them by identity
    @SuppressWarnings("StringEquality")
    public JsonType getTypeOf(String key) {
        for (Map.Entry<String, JsonType> stringJsonTypeEntry : parameterTypeByName.entrySet()) {
            if (stringJsonTypeEntry.getKey() == key) {
                return stringJsonTypeEntry.getValue();
            }
        }
        return null;
    }

    public PojoCreator<T> getCreator(DuplicateKeyStrategy strategy) {
        creator.start(strategy);
        return creator;
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
