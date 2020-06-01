package com.athaydes.json.pojo;

import com.athaydes.json.JSON;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
    void canDeserializeSmallPojoIgnoringExtraFields() {
        var parser = new JSON(Pojos.of(SmallPojo.class));
        var smallPojo = parser.parse("{ \"extra\": true, \"hello\":\"Privyet\", " +
                "\"sub\": {\"key\": 1}, \"count\":100, \"array\": [1,2,3]}", SmallPojo.class);
        assertEquals("Privyet", smallPojo.getHello());
        assertEquals(100, smallPojo.getCount());
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

    @Test
    void canDeserializeLargerPojoWithNullOptionalArgument() {
        var parser = new JSON(Pojos.of(SmallPojo.class, LargerPojo.class));
        var largerPojo = parser.parse("{" +
                "\"isTrue\": false," +
                "\"level\": 55," +
                "\"many\": 123," +
                "\"strings\": []," +
                "\"smallPojo\": {\"hello\":\"OI\",\"count\":999}," +
                "\"stringsMap\": {}," +
                "\"optionalPojo\": null" +
                "}", LargerPojo.class);
        /*
        (SmallPojo smallPojo, boolean isTrue, long many, double level, List<String> strings,
                      Map<String, List<String>> stringsMap, Optional<SmallPojo> optionalPojo)
         */
        assertEquals("OI", largerPojo.smallPojo.getHello());
        assertEquals(999, largerPojo.smallPojo.getCount());
        assertFalse(largerPojo.isTrue);
        assertEquals(123, largerPojo.many);
        assertEquals(55.0, largerPojo.level, 1e-15);
        assertEquals(List.of(), largerPojo.strings);
        assertEquals(Map.of(), largerPojo.stringsMap);
        assertFalse(largerPojo.optionalPojo.isPresent());
    }

    @Test
    void canParseBoxedNumbers() {
        var parser = new JSON(Pojos.of(BoxedNumbers.class));
        var pojo = parser.parse("{\"longN\": 1, \"intN\": 2, \"doubleN\": 4}", BoxedNumbers.class);
        assertEquals(1L, pojo.longN);
        assertEquals(2, pojo.intN);
        assertEquals(4.0, pojo.doubleN);
    }

    @Test
    void canConvertBoxedNumbers() {
        var parser = new JSON(Pojos.of(BoxedNumbers.class));
        var pojo = parser.parse("{\"longN\": 1.25, \"intN\": 2.4, \"doubleN\": 4}", BoxedNumbers.class);
        assertEquals(1L, pojo.longN);
        assertEquals(2, pojo.intN);
        assertEquals(4.0, pojo.doubleN);
    }

    @Test
    void canParsePrimitiveNumbers() {
        var parser = new JSON(Pojos.of(PrimitiveNumbers.class));
        var pojo = parser.parse("{\"longN\": 1, \"intN\": 2, \"doubleN\": 4}", PrimitiveNumbers.class);
        assertEquals(1L, pojo.longN);
        assertEquals(2, pojo.intN);
        assertEquals(4.0, pojo.doubleN);
    }

    @Test
    void canConvertPrimitiveNumbers() {
        var parser = new JSON(Pojos.of(PrimitiveNumbers.class));
        var pojo = parser.parse("{\"longN\": 1.25, \"intN\": 2.4, \"doubleN\": 4}", PrimitiveNumbers.class);
        assertEquals(1L, pojo.longN);
        assertEquals(2, pojo.intN);
        assertEquals(4.0, pojo.doubleN);
    }

    @Test
    void rejectsPojoWithWrongType() {
        var parser = new JSON(Pojos.of(SmallPojo.class, LargerPojo.class));
        var error = assertThrows(PojoException.class, () -> parser.parse("{" +
                // isTrue has wrong type
                "\"isTrue\": \"no\"," +
                "\"level\": 2.56," +
                "\"many\": 950332," +
                "\"strings\": [\"hello\", \"bye\"]," +
                "\"smallPojo\": {\"hello\":\"OI\",\"count\":999}," +
                "\"stringsMap\": {}" +
                "}", LargerPojo.class));
        assertEquals("Invalid value for key 'isTrue'", error.getMessage());
    }

    @Test
    void rejectsPojoWithWrongOptionalType() {
        var parser = new JSON(Pojos.of(SmallPojo.class, LargerPojo.class));
        var error = assertThrows(PojoException.class, () -> parser.parse("{" +
                "\"isTrue\": true," +
                "\"level\": 2.56," +
                "\"many\": 950332," +
                "\"strings\": []," +
                "\"smallPojo\": {\"hello\":\"OI\",\"count\":999}," +
                "\"stringsMap\": {}," +
                // optionalPojo should be object
                "\"optionalPojo\": \"wrong\"" +
                "}", LargerPojo.class));
        assertEquals("Invalid value for key 'optionalPojo'", error.getMessage());
    }

    @Test
    void rejectsPojoMissingMandatoryFields() {
        var parser = new JSON(Pojos.of(SmallPojo.class));
        var error = assertThrows(PojoException.class, () ->
                parser.parse("{\"foo\":\"Ola\",\"count\":32}", SmallPojo.class));

        assertEquals("Unable to create instance of class com.athaydes.json.pojo.SmallPojo, " +
                        "available fields do not match any available constructor",
                error.getMessage());
    }

    @Test
    void readmeExample() {
        var parser = new JSON(Pojos.of(Person.class));
        var person = parser.parse("{\n" +
                "  \"name\": \"Joe\",\n" +
                "  \"age\": 15\n" +
                "}", Person.class);
        assertEquals("Joe", person.name);
        assertEquals(15, person.age);
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

final class Person {
    final String name;
    final int age;

    public Person(String name, int age) {
        this.name = name;
        this.age = age;
    }
}

final class ConflictingParameterTypes {
    public ConflictingParameterTypes(String a, int b) {
    }

    public ConflictingParameterTypes(String a, boolean b) {
    }
}

final class ConflictingNumberParameterTypes {
    final String a;
    final Number b;
    final Number c;

    public ConflictingNumberParameterTypes(String a, Integer b, Integer c) {
        this.a = a;
        this.b = b;
        this.c = c;
    }

    public ConflictingNumberParameterTypes(String a, Long c, Double b) {
        this.a = a;
        this.b = b;
        this.c = c;
    }
}

final class ConflictingPrimitiveNumberParameterTypes {
    public ConflictingPrimitiveNumberParameterTypes(String a, int b, int c) {
    }

    public ConflictingPrimitiveNumberParameterTypes(String a, long c, double b) {
    }
}

final class BoxedNumbers {
    final Long longN;
    final Integer intN;
    final Double doubleN;

    public BoxedNumbers(Long longN, Integer intN, Double doubleN) {
        this.longN = longN;
        this.intN = intN;
        this.doubleN = doubleN;
    }
}

final class PrimitiveNumbers {
    final long longN;
    final int intN;
    final double doubleN;

    public PrimitiveNumbers(long longN, int intN, double doubleN) {
        this.longN = longN;
        this.intN = intN;
        this.doubleN = doubleN;
    }
}