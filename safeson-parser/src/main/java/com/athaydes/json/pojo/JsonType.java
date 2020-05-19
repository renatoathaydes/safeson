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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Compound compound = (Compound) o;

            if (!type.equals(compound.type)) return false;
            return container == compound.container;
        }

        @Override
        public int hashCode() {
            int result = type.hashCode();
            result = 31 * result + container.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return "Compound{" +
                    "type=" + type +
                    ", container=" + container +
                    '}';
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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Scalar scalar = (Scalar) o;

            return type.equals(scalar.type);
        }

        @Override
        public int hashCode() {
            return type.hashCode();
        }

        @Override
        public String toString() {
            return "Scalar{" +
                    "type=" + type +
                    '}';
        }
    }
}
