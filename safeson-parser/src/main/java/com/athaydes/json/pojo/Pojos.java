package com.athaydes.json.pojo;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class Pojos {

    public static final Pojos EMPTY = new Pojos(Map.of());

    private static final Set<Class<?>> NATIVE_TYPES = Set.of(int.class, long.class, double.class, boolean.class,
            String.class, List.class, Map.class);

    private final Map<? extends Class<?>, ? extends PojoMapper<?>> mappers;

    private Pojos(Map<? extends Class<?>, ? extends PojoMapper<?>> mappers) {
        this.mappers = mappers;
    }

    static Pojos of(Map<? extends Class<?>, ? extends PojoMapper<?>> mappers) {
        return new Pojos(validate(mappers));
    }

    public static Pojos of(Class<?>... types) {
        return PojoMapper.pojos(types);
    }

    public static <T> boolean isNativeJsonType(Class<?> type) {
        return NATIVE_TYPES.contains(type);
    }

    private static Map<? extends Class<?>, ? extends PojoMapper<?>> validate(
            Map<? extends Class<?>, ? extends PojoMapper<?>> mappers) {
        Set<Class<?>> requiredPojoTypes = new HashSet<>();
        for (PojoMapper<?> mapper : mappers.values()) {
            for (PojoConstructor<?> constructor : mapper.getConstructors()) {
                constructor.getParamNames().stream()
                        .map(name -> constructor.getTypeOfParameter(name).getValueType())
                        .forEach(requiredPojoTypes::add);
            }
        }
        requiredPojoTypes.removeAll(NATIVE_TYPES);
        requiredPojoTypes.removeAll(mappers.keySet());

        if (!requiredPojoTypes.isEmpty()) {
            throw new PojoException("Unmapped types: " + requiredPojoTypes);
        }

        return mappers;
    }

    @SuppressWarnings("unchecked")
    public <T> PojoMapper<T> getMapper(Class<T> type) {
        return (PojoMapper<T>) mappers.get(type);
    }

    public Collection<? extends PojoMapper<?>> getMappers() {
        return mappers.values();
    }
}
