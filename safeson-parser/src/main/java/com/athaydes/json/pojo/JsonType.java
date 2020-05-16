package com.athaydes.json.pojo;

import com.athaydes.json.util.CheckedFunction;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class JsonType {

    private JsonType() {
    }

    public abstract <T> T match(CheckedFunction<Scalar, T> onScalar,
                                CheckedFunction<Compound, T> onCompound) throws Exception;

    public abstract Class<?> getValueType();

    public enum Container {
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

        Compound(JsonType type, Container container) {
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
        public <T> T match(CheckedFunction<Scalar, T> onScalar,
                           CheckedFunction<Compound, T> onCompound) throws Exception {
            return onCompound.applyChecked(this);
        }

        @Override
        public Class<?> getValueType() {
            return type.getValueType();
        }
    }

    public static final class Scalar extends JsonType {
        private final Class<?> type;

        Scalar(Class<?> type) {
            this.type = type;
        }

        public Class<?> getValueType() {
            return type;
        }

        @Override
        public <T> T match(CheckedFunction<Scalar, T> onScalar,
                           CheckedFunction<Compound, T> onCompound) throws Exception {
            return onScalar.applyChecked(this);
        }

    }
}
