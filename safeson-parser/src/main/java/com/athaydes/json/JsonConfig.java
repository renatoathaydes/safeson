package com.athaydes.json;

import java.util.Objects;

public final class JsonConfig {
    public static final JsonConfig DEFAULT = new JsonConfig(1024 * 1000, 512, 128, true, DuplicateKeyStrategy.FAIL);

    private final int maxStringLength;
    private final int maxRecursionDepth;
    private final int maxWhitespace;
    private final boolean consumeTrailingContent;
    private final DuplicateKeyStrategy duplicateKeyStrategy;

    public static Builder builder() {
        return new Builder();
    }

    private JsonConfig(int maxStringLength, int maxRecursionDepth, int maxWhitespace,
                       boolean consumeTrailingContent, DuplicateKeyStrategy duplicateKeyStrategy) {
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
        this.duplicateKeyStrategy = duplicateKeyStrategy;
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

    public DuplicateKeyStrategy duplicateKeyStrategy() {
        return duplicateKeyStrategy;
    }

    MapUpdater mapUpdater() {
        return duplicateKeyStrategy.getMapUpdater();
    }

    public static final class Builder {
        private int maxStringLength = DEFAULT.maxStringLength;
        private int maxRecursionDepth = DEFAULT.maxRecursionDepth;
        private int maxWhitespace = DEFAULT.maxWhitespace;
        private boolean consumeTrailingContent = DEFAULT.consumeTrailingContent;
        private DuplicateKeyStrategy duplicateKeyStrategy = DEFAULT.duplicateKeyStrategy;

        public Builder withMaxStringLength(int maxStringLength) {
            this.maxStringLength = maxStringLength;
            return this;
        }

        public Builder withMaxRecursionDepth(int maxRecursionDepth) {
            this.maxRecursionDepth = maxRecursionDepth;
            return this;
        }

        public Builder withMaxWhitespace(int maxWhitespace) {
            this.maxWhitespace = maxWhitespace;
            return this;
        }

        public Builder withConsumeTrailingContent(boolean consumeTrailingContent) {
            this.consumeTrailingContent = consumeTrailingContent;
            return this;
        }

        public Builder withDuplicateKeyStrategy(DuplicateKeyStrategy duplicateKeyStrategy) {
            this.duplicateKeyStrategy = Objects.requireNonNull(duplicateKeyStrategy);
            return this;
        }

        JsonConfig build() {
            return new JsonConfig(maxStringLength, maxRecursionDepth, maxWhitespace,
                    consumeTrailingContent, duplicateKeyStrategy);
        }
    }
}
