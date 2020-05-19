package com.athaydes.json.pojo;

import org.junit.jupiter.api.Test;

import java.net.Socket;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PojosTest {

    @Test
    void canCreateEmptyPojos() {
        var pojos = PojoMapper.pojos();
        assertEquals(0, pojos.getMappers().size());
    }

    @Test
    void canCreatePojosWithNativeTypes() {
        var pojos = PojoMapper.pojos(String.class, Map.class);
        assertEquals(0, pojos.getMappers().size());
    }

    @Test
    void canCreatePojosWithOnePojo() {
        var pojos = Pojos.of(SmallPojo.class);
        assertEquals(1, pojos.getMappers().size());
        assertEquals(PojoMapper.of(SmallPojo.class), pojos.getMapper(SmallPojo.class));
    }

    @Test
    void canCreatePojosWithManyPojos() {
        var pojos = PojoMapper.pojos(SmallPojo.class, LargerPojo.class);
        assertEquals(2, pojos.getMappers().size());
        assertEquals(PojoMapper.of(SmallPojo.class), pojos.getMapper(SmallPojo.class));
        assertEquals(PojoMapper.of(LargerPojo.class), pojos.getMapper(LargerPojo.class));
    }

    @Test
    void cannotCreatePojosWithMissingPojosMapping() {
        var err = assertThrows(PojoException.class, () -> Pojos.of(LargerPojo.class));
        assertEquals("Unmapped types: [class com.athaydes.json.pojo.SmallPojo]", err.getMessage());
    }

    @Test
    void cannotCreatePojosWithTypesWithoutParameters() {
        var err = assertThrows(PojoException.class, () -> Pojos.of(Socket.class));
        assertEquals("Cannot map java.net.Socket as its parameter names are not present. " +
                        "Re-compile it with the -parameters flag.",
                err.getMessage());
    }

    @Test
    void cannotCreatePojosWithGenericType() {
        var err = assertThrows(PojoException.class, () -> Pojos.of(GenericClass.class));
        assertEquals("Unmapped types: [class java.lang.Object]", err.getMessage());
    }

    @Test
    void cannotCreateaPojosWithConflictingFieldTypes() {
        var err = assertThrows(PojoException.class, () -> Pojos.of(ConflictingParameterTypes.class));
        assertEquals("Conflicting types for parameters: b (Scalar{type=int} VS Scalar{type=boolean})", err.getMessage());
    }

    @Test
    void cannotCreateaPojosWithConflictingNumberFieldTypes() {
        var err = assertThrows(PojoException.class, () -> Pojos.of(ConflictingNumberParameterTypes.class));
        assertEquals("Conflicting types for parameters: " +
                        "b (Scalar{type=class java.lang.Integer} VS Scalar{type=class java.lang.Double}), " +
                        "c (Scalar{type=class java.lang.Integer} VS Scalar{type=class java.lang.Long})",
                err.getMessage());
    }

    @Test
    void cannotCreateaPojosWithConflictingPrimitiveFieldTypes() {
        var err = assertThrows(PojoException.class, () -> Pojos.of(ConflictingPrimitiveNumberParameterTypes.class));
        assertEquals("Conflicting types for parameters: " +
                        "b (Scalar{type=int} VS Scalar{type=double}), " +
                        "c (Scalar{type=int} VS Scalar{type=long})",
                err.getMessage());
    }
}
