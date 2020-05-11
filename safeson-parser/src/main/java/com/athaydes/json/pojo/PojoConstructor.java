package com.athaydes.json.pojo;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PojoConstructor<T> {
    private final Constructor<T> constructor;
    private final Set<String> paramNames;
    private final Map<String, Class<?>> paramTypeByName;

    public PojoConstructor(Constructor<T> constructor, List<Parameter> parameters) {
        this.constructor = constructor;
        Set<String> pNames = new LinkedHashSet<>(parameters.size());
        Map<String, Class<?>> params = new HashMap<>(pNames.size());
        for (Parameter parameter : parameters) {
            var pName = parameter.getName();
            pNames.add(pName);
            params.put(pName, parameter.getType());
        }
        this.paramNames = Collections.unmodifiableSet(pNames);
        this.paramTypeByName = Collections.unmodifiableMap(params);
    }

    public Constructor<T> getConstructor() {
        return constructor;
    }

    public Set<String> getParamNames() {
        return paramNames;
    }

    public Class<?> getTypeOfParameter(String parameter) {
        return paramTypeByName.get(parameter);
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
