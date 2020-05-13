package com.athaydes.json.pojo;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PojoMapperTest {

    @Test
    void canMapPojo() {
        var mapper = PojoMapper.of(SmallPojo.class);
        assertEquals(1, mapper.getConstructors().size());
        assertEquals(List.of("hello", "count"), List.copyOf(mapper.getConstructors().get(0).getParamNames()));
        assertEquals(String.class, mapper.getConstructors().get(0).getTypeOfParameter("hello")
                .match(s -> s.getValueType(), c -> null));
        assertEquals(int.class, mapper.getConstructors().get(0).getTypeOfParameter("count")
                .match(s -> s.getValueType(), c -> null));

        var pojo = mapper.getConstructors().get(0).createPojo(new Object[]{"world", 10});
        assertEquals(new SmallPojo("world", 10), pojo);
    }

    @Test
    void canMapManyConstructors() {
        var mapper = PojoMapper.of(LargerPojo.class);
        assertEquals(3, mapper.getConstructors().size());

        assertEquals(List.of("smallPojo", "isTrue", "many", "level", "strings", "stringsMap", "optionalPojo"),
                List.copyOf(mapper.getConstructors().get(0).getParamNames()));

        assertEquals(List.of("pojo", "map", "min"),
                List.copyOf(mapper.getConstructors().get(1).getParamNames()));

        assertEquals(List.of("many", "level"),
                List.copyOf(mapper.getConstructors().get(2).getParamNames()));

        // assert first constructor types
        assertEquals(SmallPojo.class, mapper.getConstructors().get(0).getTypeOfParameter("smallPojo")
                .match(s -> s.getValueType(), c -> null));
        assertEquals(boolean.class, mapper.getConstructors().get(0).getTypeOfParameter("isTrue")
                .match(s -> s.getValueType(), c -> null));
        assertEquals(long.class, mapper.getConstructors().get(0).getTypeOfParameter("many")
                .match(s -> s.getValueType(), c -> null));
        assertEquals(double.class, mapper.getConstructors().get(0).getTypeOfParameter("level")
                .match(s -> s.getValueType(), c -> null));

        assertEquals(JsonType.Container.LIST, mapper.getConstructors().get(0).getTypeOfParameter("strings")
                .match(s -> null, c -> c.getContainer()));
        assertEquals(String.class, mapper.getConstructors().get(0).getTypeOfParameter("strings")
                .match(s -> null, c -> c.getType().match(s -> s.getValueType(), c2 -> "wrong")));

        assertEquals(JsonType.Container.MAP, mapper.getConstructors().get(0).getTypeOfParameter("stringsMap")
                .match(s -> null, c -> c.getContainer()));
        assertEquals(JsonType.Container.LIST, mapper.getConstructors().get(0).getTypeOfParameter("stringsMap")
                .match(s -> null, c -> c.getType().match(s -> null, c2 -> c2.getContainer())));
        assertEquals(String.class, mapper.getConstructors().get(0).getTypeOfParameter("stringsMap")
                .match(s -> null, c -> c.getType().match(s -> null, c2 -> c2.getType()
                        .match(s -> s.getValueType(), c3 -> "wrong"))));

        assertEquals(JsonType.Container.OPTIONAL, mapper.getConstructors().get(0).getTypeOfParameter("optionalPojo")
                .match(s -> null, c -> c.getContainer()));
        assertEquals(SmallPojo.class, mapper.getConstructors().get(0).getTypeOfParameter("optionalPojo")
                .match(s -> null, c -> c.getType().match(s -> s.getValueType(), c2 -> "wrong")));

        // assert second constructor types
        assertEquals(SmallPojo.class, mapper.getConstructors().get(1).getTypeOfParameter("pojo")
                .match(s -> s.getValueType(), c -> null));

        assertEquals(JsonType.Container.MAP, mapper.getConstructors().get(1).getTypeOfParameter("map")
                .match(s -> null, c -> c.getContainer()));
        assertEquals(JsonType.Container.LIST, mapper.getConstructors().get(1).getTypeOfParameter("map")
                .match(s -> null, c -> c.getType().match(s -> null, c2 -> c2.getContainer())));
        assertEquals(String.class, mapper.getConstructors().get(1).getTypeOfParameter("map")
                .match(s -> null, c -> c.getType().match(s -> null, c2 -> c2.getType()
                        .match(s -> s.getValueType(), c3 -> "wrong"))));

        assertEquals(double.class, mapper.getConstructors().get(1).getTypeOfParameter("min")
                .match(s -> s.getValueType(), c -> null));

        // assert last constructor types
        assertEquals(long.class, mapper.getConstructors().get(2).getTypeOfParameter("many")
                .match(s -> s.getValueType(), c -> null));
        assertEquals(double.class, mapper.getConstructors().get(2).getTypeOfParameter("level")
                .match(s -> s.getValueType(), c -> null));

    }

    @Test
    void cannotMapTypeWithoutConstructor() {
        var err = assertThrows(PojoException.class, () -> PojoMapper.of(NoConstructor.class));
        assertEquals("Cannot create POJO Mapper, no constructors available", err.getMessage());
    }

    @Test
    void cannotMapTypeTakingArray() {
        var err = assertThrows(PojoException.class, () -> PojoMapper.of(TakesArray.class));
        assertEquals("Illegal type parameter for ints in constructor " +
                        "public com.athaydes.json.pojo.TakesArray(int[]) " +
                        "(type must not be an array): class [I",
                err.getMessage());
    }

    @Test
    void cannotMapTypeTakingInterface() {
        var err = assertThrows(PojoException.class, () -> PojoMapper.of(TakesInterface.class));
        assertEquals("Illegal type parameter for run in constructor " +
                        "public com.athaydes.json.pojo.TakesInterface(java.lang.Runnable) " +
                        "(type must be concrete): interface java.lang.Runnable",
                err.getMessage());
    }

    @Test
    void cannotMapTypeTakingAbstractClass() {
        var err = assertThrows(PojoException.class, () -> PojoMapper.of(TakesAbstractClass.class));
        assertEquals("Illegal type parameter for is in constructor " +
                        "public com.athaydes.json.pojo.TakesAbstractClass(java.io.InputStream) " +
                        "(type must be concrete): class java.io.InputStream",
                err.getMessage());
    }
}
