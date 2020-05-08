package com.athaydes.json;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.athaydes.json.DuplicateKeyStrategy.FAIL;
import static com.athaydes.json.DuplicateKeyStrategy.KEEP_FIRST;
import static com.athaydes.json.DuplicateKeyStrategy.KEEP_LAST;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DuplicateKeyStrategyTest {

    @Test
    void keepFirstKey() {
        var map = new LinkedHashMap<String, Object>(4);
        assertTrue(KEEP_FIRST.getMapUpdater().updateMap(map, "a", 1));
        assertTrue(KEEP_FIRST.getMapUpdater().updateMap(map, "b", 2));
        assertTrue(KEEP_FIRST.getMapUpdater().updateMap(map, "c", 3));
        assertTrue(KEEP_FIRST.getMapUpdater().updateMap(map, "c", 4));
        assertTrue(KEEP_FIRST.getMapUpdater().updateMap(map, "c", 5));
        assertTrue(KEEP_FIRST.getMapUpdater().updateMap(map, "a", 6));
        assertTrue(KEEP_FIRST.getMapUpdater().updateMap(map, "d", 7));

        assertEquals(Map.of("a", 1, "b", 2, "c", 3, "d", 7), map);
    }

    @Test
    void keepLastKey() {
        var map = new LinkedHashMap<String, Object>(4);
        assertTrue(KEEP_LAST.getMapUpdater().updateMap(map, "a", 1));
        assertTrue(KEEP_LAST.getMapUpdater().updateMap(map, "b", 2));
        assertTrue(KEEP_LAST.getMapUpdater().updateMap(map, "c", 3));
        assertTrue(KEEP_LAST.getMapUpdater().updateMap(map, "c", 4));
        assertTrue(KEEP_LAST.getMapUpdater().updateMap(map, "c", 5));
        assertTrue(KEEP_LAST.getMapUpdater().updateMap(map, "a", 6));
        assertTrue(KEEP_LAST.getMapUpdater().updateMap(map, "d", 7));

        assertEquals(Map.of("a", 6, "b", 2, "c", 5, "d", 7), map);
    }

    @Test
    void failOnDuplicateKey() {
        var map = new LinkedHashMap<String, Object>(4);
        assertTrue(FAIL.getMapUpdater().updateMap(map, "a", 1));
        assertTrue(FAIL.getMapUpdater().updateMap(map, "b", 2));
        assertTrue(FAIL.getMapUpdater().updateMap(map, "c", 3));
        assertFalse(FAIL.getMapUpdater().updateMap(map, "c", 4));
        assertFalse(FAIL.getMapUpdater().updateMap(map, "c", 5));
        assertFalse(FAIL.getMapUpdater().updateMap(map, "a", 6));
        assertTrue(FAIL.getMapUpdater().updateMap(map, "d", 7));

        assertEquals(Map.of("a", 6, "b", 2, "c", 5, "d", 7), map);
    }
}
