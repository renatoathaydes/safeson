package com.athaydes.json;

import java.util.Map;

/**
 * Strategy to deal with duplicate keys.
 */
public enum DuplicateKeyStrategy {
    /**
     * Do not allow duplicate keys. Fail to parse objects that contain duplicate keys.
     * <p>
     * For efficiency purposes, a key whose previous value was null does not count as a duplicate key.
     */
    FAIL,

    /**
     * Allow duplicate keys. Keep the last value for the key.
     */
    KEEP_LAST,

    /**
     * Allow duplicate keys. Keep the first value for the key.
     */
    KEEP_FIRST;

    private final MapUpdater mapUpdater;

    DuplicateKeyStrategy() {
        // "this" would be null here! So, use ordinal()
        switch (ordinal()) {
            case 0:
                mapUpdater = new FailIfKeyExists();
                break;
            case 1:
                mapUpdater = new KeepLast();
                break;
            case 2:
                mapUpdater = new KeepFirst();
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + name());
        }
    }

    MapUpdater getMapUpdater() {
        return mapUpdater;
    }
}

interface MapUpdater {
    boolean updateMap(Map<String, Object> map, String key, Object value);
}

final class FailIfKeyExists implements MapUpdater {
    @Override
    public boolean updateMap(Map<String, Object> map, String key, Object value) {
        var old = map.put(key, value);
        return old == null;
    }
}

final class KeepFirst implements MapUpdater {
    @Override
    public boolean updateMap(Map<String, Object> map, String key, Object value) {
        if (!map.containsKey(key)) {
            map.put(key, value);
        }
        return true;
    }
}

final class KeepLast implements MapUpdater {
    @Override
    public boolean updateMap(Map<String, Object> map, String key, Object value) {
        map.put(key, value);
        return true;
    }
}
