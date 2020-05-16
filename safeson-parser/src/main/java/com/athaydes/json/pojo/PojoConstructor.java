package com.athaydes.json.pojo;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class PojoConstructor<T> {
    private final Constructor<T> constructor;
    private final Set<String> paramNames;
    private final Map<String, JsonType> paramTypeByName;

    public PojoConstructor(Constructor<T> constructor, List<Parameter> parameters) {
        this.constructor = constructor;
        Set<String> pNames = new LinkedHashSet<>(parameters.size());
        Map<String, JsonType> params = new HashMap<>(pNames.size());
        for (Parameter parameter : parameters) {
            var pName = parameter.getName();
            pNames.add(pName);
            params.put(pName, toJsonType(parameter));
        }
        this.paramNames = Collections.unmodifiableSet(pNames);
        this.paramTypeByName = Collections.unmodifiableMap(params);
    }

    private static JsonType toJsonType(Parameter parameter) {
        Class<?> type = parameter.getType();
        var container = JsonType.Container.getContainerOf(type);
        return getParameterJsonType(parameter, type, container);
    }

    private static JsonType getParameterJsonType(Parameter parameter, Type type, JsonType.Container container) {
        switch (container) {
            case MAP: {
                var typeParameters = ((ParameterizedType) parameter.getParameterizedType()).getActualTypeArguments();
                validateMapKeyType(parameter, typeParameters[0]);
                return new JsonType.Compound(validateValueType(parameter, typeParameters[1]), container);
            }
            case LIST:
                // fall-through
            case OPTIONAL: {
                var typeParameters = ((ParameterizedType) parameter.getParameterizedType()).getActualTypeArguments();
                return new JsonType.Compound(validateValueType(parameter, typeParameters[0]), container);
            }
            case NONE:
                return validateValueType(parameter, type);
            case UNSUPPORTED:
                throw new PojoException("Unsupported generic type for parameter for " + paramDescription(parameter) +
                        ": " + type);
            default:
                throw new IllegalStateException("Unknown container: " + container);
        }
    }

    private static void validateMapKeyType(Parameter parameter, Type typeParameter) {
        if (!typeParameter.equals(String.class)) {
            throw new PojoException("Illegal type parameter for " + paramDescription(parameter) +
                    " (only String keys are supported in JSON): " + typeParameter);
        }
    }

    private static JsonType validateValueType(Parameter parameter, Type typeParameter) {
        if (typeParameter instanceof ParameterizedType) {
            var rawType = ((ParameterizedType) typeParameter).getRawType();
            var container = JsonType.Container.getContainerOf(rawType);
            return getParameterJsonType(parameter, rawType, container);
        }
        String errorReason = "";
        if (typeParameter instanceof Class<?>) {
            var type = (Class<?>) typeParameter;
            if (type.isArray()) {
                errorReason = "type must not be an array";
            } else if (type.isAnonymousClass()) {
                errorReason = "type cannot be anonymous";
            } else if (type.isInterface() || (!type.isPrimitive() && Modifier.isAbstract(type.getModifiers()))) {
                errorReason = "type must be concrete";
            }
            if (errorReason.isEmpty()) {
                return new JsonType.Scalar(type);
            }
        }

        throw new PojoException("Illegal type parameter for " + paramDescription(parameter) +
                " (" + errorReason + "): " + typeParameter);
    }

    private static String paramDescription(Parameter parameter) {
        return parameter.getName() + " in constructor " +
                parameter.getDeclaringExecutable();
    }

    public Constructor<T> getConstructor() {
        return constructor;
    }

    public Set<String> getParamNames() {
        return paramNames;
    }

    public JsonType getTypeOfParameter(String parameter) {
        return paramTypeByName.get(parameter);
    }

    public Map<String, JsonType> getParamTypeByName() {
        return paramTypeByName;
    }

    public int size() {
        return paramNames.size();
    }

    public T createPojo(Object[] args) {
        try {
            return constructor.newInstance(args);
        } catch (Exception e) {
            throw new PojoException("Unable to instantiate POJO", e);
        }
    }
}
