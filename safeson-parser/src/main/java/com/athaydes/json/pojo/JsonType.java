package com.athaydes.json.pojo;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public abstract class JsonType {

    private JsonType() {
    }

    abstract <T> T match(Function<Scalar, T> onScalar, Function<Compound, T> onCompound);

    enum Container {
        MAP, LIST, OPTIONAL, NONE, UNSUPPORTED;

        public static Container getContainerOf(Type type) {
            if (type.equals(Map.class)) {
                return MAP;
            }
            if (type.equals(List.class)) {
                return LIST;
            }
            if (type.equals(Optional.class)) {
                return OPTIONAL;
            }
            if (type instanceof ParameterizedType) {
                return UNSUPPORTED;
            }
            return NONE;
        }
    }

    public static final class Compound extends JsonType {
        private final JsonType type;
        private final Container container;

        public Compound(JsonType type, Container container) {
            this.type = type;
            this.container = container;
        }

        public JsonType getType() {
            return type;
        }

        public Container getContainer() {
            return container;
        }

        @Override
        <T> T match(Function<Scalar, T> onScalar, Function<Compound, T> onCompound) {
            return onCompound.apply(this);
        }
    }

    public static final class Scalar extends JsonType {
        private final Class<?> type;

        public Scalar(Class<?> type) {
            this.type = type;
        }

        public Class<?> getValueType() {
            return type;
        }

        @Override
        <T> T match(Function<Scalar, T> onScalar, Function<Compound, T> onCompound) {
            return onScalar.apply(this);
        }
    }
}
