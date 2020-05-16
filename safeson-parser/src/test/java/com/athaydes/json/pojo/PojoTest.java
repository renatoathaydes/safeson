package com.athaydes.json.pojo;

import com.athaydes.json.JSON;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PojoTest {

    @Test
    void canDeserializeSmallPojo() {
        var parser = new JSON(Pojos.of(SmallPojo.class));
        var smallPojo = parser.parse("{\"hello\":\"Ola\",\"count\":32}", SmallPojo.class);
        assertEquals("Ola", smallPojo.getHello());
        assertEquals(32, smallPojo.getCount());
    }

    @Test
    void canDeserializeLargerPojoWithAllArguments() {
        var parser = new JSON(Pojos.of(SmallPojo.class, LargerPojo.class));
        var largerPojo = parser.parse("{" +
                "\"smallPojo\": {\"hello\":\"Ola\",\"count\":32}," +
                "\"isTrue\": true," +
                "\"many\": 500," +
                "\"level\": 0.24," +
                "\"strings\": [\"a\", \"b\", \"c\"]," +
                "\"stringsMap\": {\"foo\": [\"bar\"]}," +
                "\"optionalPojo\": {\"hello\": \"Hej\", \"count\": 42}" +
                "}", LargerPojo.class);
        /*
        (SmallPojo smallPojo, boolean isTrue, long many, double level, List<String> strings,
                      Map<String, List<String>> stringsMap, Optional<SmallPojo> optionalPojo)
         */
        assertEquals("Ola", largerPojo.smallPojo.getHello());
        assertEquals(32, largerPojo.smallPojo.getCount());
        assertTrue(largerPojo.isTrue);
        assertEquals(500, largerPojo.many);
        assertEquals(0.24, largerPojo.level, 1e-15);
        assertEquals(List.of("a", "b", "c"), largerPojo.strings);
        assertEquals(Map.of("foo", List.of("bar")), largerPojo.stringsMap);
        assertTrue(largerPojo.optionalPojo.isPresent());
        assertEquals("Hej", largerPojo.optionalPojo.get().getHello());
        assertEquals(42, largerPojo.optionalPojo.get().getCount());
    }

    @Test
    void canDeserializeLargerPojoWithMissingOptionalArgument() {
        var parser = new JSON(Pojos.of(SmallPojo.class, LargerPojo.class));
        var largerPojo = parser.parse("{" +
                "\"isTrue\": false," +
                "\"level\": 2.56," +
                "\"many\": 950332," +
                "\"strings\": [\"hello\", \"bye\"]," +
                "\"smallPojo\": {\"hello\":\"OI\",\"count\":999}," +
                "\"stringsMap\": {}" +
                "}", LargerPojo.class);
        /*
        (SmallPojo smallPojo, boolean isTrue, long many, double level, List<String> strings,
                      Map<String, List<String>> stringsMap, Optional<SmallPojo> optionalPojo)
         */
        assertEquals("OI", largerPojo.smallPojo.getHello());
        assertEquals(999, largerPojo.smallPojo.getCount());
        assertFalse(largerPojo.isTrue);
        assertEquals(950332, largerPojo.many);
        assertEquals(2.56, largerPojo.level, 1e-15);
        assertEquals(List.of("hello", "bye"), largerPojo.strings);
        assertEquals(Map.of(), largerPojo.stringsMap);
        assertFalse(largerPojo.optionalPojo.isPresent());
    }
}

final class SmallPojo {
    private final String hello;
    private final int count;

    public SmallPojo(String hello, int count) {
        this.hello = hello;
        this.count = count;
    }

    public String getHello() {
        return hello;
    }

    public int getCount() {
        return count;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SmallPojo smallPojo = (SmallPojo) o;

        if (count != smallPojo.count) return false;
        return hello.equals(smallPojo.hello);
    }

    @Override
    public int hashCode() {
        int result = hello.hashCode();
        result = 31 * result + count;
        return result;
    }
}

final class LargerPojo {
    final SmallPojo smallPojo;
    final boolean isTrue;
    final long many;
    final double level;
    final List<String> strings;
    final Map<String, List<String>> stringsMap;
    final Optional<SmallPojo> optionalPojo;

    public LargerPojo(SmallPojo smallPojo, boolean isTrue, long many, double level, List<String> strings,
                      Map<String, List<String>> stringsMap, Optional<SmallPojo> optionalPojo) {
        this.smallPojo = smallPojo;
        this.isTrue = isTrue;
        this.many = many;
        this.level = level;
        this.strings = strings;
        this.stringsMap = stringsMap;
        this.optionalPojo = optionalPojo;
    }

    public LargerPojo(SmallPojo pojo, Map<String, List<String>> map, double min) {
        this(pojo, false, 3, min, List.of(), map, Optional.empty());
    }

    public LargerPojo(long many, double level) {
        this(null, true, many, level, List.of(), Map.of(), Optional.empty());
    }
}

final class NoConstructor {
    String s;
}

final class TakesArray {
    public TakesArray(int[] ints) {
    }
}

final class TakesInterface {
    public TakesInterface(Runnable run) {
    }
}

final class TakesAbstractClass {
    public TakesAbstractClass(InputStream is) {
    }
}

final class GenericClass<T> {
    public GenericClass(T t) {
    }
}
