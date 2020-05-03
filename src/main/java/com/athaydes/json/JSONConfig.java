package com.athaydes.json;

public final class JSONConfig {
    public static final JSONConfig DEFAULT = new JSONConfig(1024 * 1000, 512);

    private final int maxStringLength;
    private final int maxRecursionDepth;

    public JSONConfig(int maxStringLength, int maxRecursionDepth) {
        if (maxStringLength < 16) {
            throw new IllegalArgumentException("maxStringLength must be 16 or greater");
        }
        if (maxRecursionDepth < 4) {
            throw new IllegalArgumentException("maxRecursionDepth must be 4 or greater");
        }
        this.maxStringLength = maxStringLength;
        this.maxRecursionDepth = maxRecursionDepth;
    }

    public int maxStringLength() {
        return maxStringLength;
    }

    public int maxRecursionDepth() {
        return maxRecursionDepth;
    }
}
