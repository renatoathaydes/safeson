package com.athaydes.json.pojo;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class PojoTest {

    @Test
    void canDeserializeSmallPojo() {

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
