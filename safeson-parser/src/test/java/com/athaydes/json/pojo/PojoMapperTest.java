package com.athaydes.json.pojo;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PojoMapperTest {
    @Test
    void canMapPojo() {
        var mapper = PojoMapper.of(SmallPojo.class);
        assertEquals(1, mapper.getConstructors().size());
        assertEquals(List.of("hello", "count"), List.copyOf(mapper.getConstructors().get(0).getParamNames()));
        assertEquals(String.class, mapper.getConstructors().get(0).getTypeOfParameter("hello"));
        assertEquals(int.class, mapper.getConstructors().get(0).getTypeOfParameter("count"));

        var pojo = mapper.getConstructors().get(0).createPojo(new Object[]{"world", 10});
        assertEquals(new SmallPojo("world", 10), pojo);
    }
}
