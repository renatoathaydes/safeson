package com.athaydes.json;

public final class JSONConfig {
    public static final JSONConfig DEFAULT = new JSONConfig(1024 * 1000, 512, 128, true);

    private final int maxStringLength;
    private final int maxRecursionDepth;
    private final int maxWhitespace;
    private final boolean consumeTrailingContent;

    public JSONConfig(int maxStringLength, int maxRecursionDepth, int maxWhitespace,
                      boolean consumeTrailingContent) {
        if (maxStringLength < 16) {
            throw new IllegalArgumentException("maxStringLength must be 16 or greater");
        }
        if (maxRecursionDepth < 4) {
            throw new IllegalArgumentException("maxRecursionDepth must be 4 or greater");
        }
        if (maxWhitespace < 2) {
            throw new IllegalArgumentException("maxWhitespace must be 2 or greater");
        }
        this.maxStringLength = maxStringLength;
        this.maxRecursionDepth = maxRecursionDepth;
        this.maxWhitespace = maxWhitespace;
        this.consumeTrailingContent = consumeTrailingContent;
    }

    public int maxStringLength() {
        return maxStringLength;
    }

    public int maxRecursionDepth() {
        return maxRecursionDepth;
    }

    public int maxWhitespace() {
        return maxWhitespace;
    }

    public boolean shouldConsumeTrailingContent() {
        return consumeTrailingContent;
    }
}
