package com.athaydes.json.pojo;

import com.athaydes.json.DuplicateKeyStrategy;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public final class PojoCreator<T> {
    private final List<PojoConstructor<T>> constructors;
    private final String[][] constructorsParamNames;
    private final Object[][] constructorsValues;
    private final boolean[][] optionalParams;

    private DuplicateKeyStrategy strategy;

    public PojoCreator(List<PojoConstructor<T>> constructors) {
        this.constructors = constructors;
        constructorsParamNames = new String[constructors.size()][];
        constructorsValues = new Object[constructors.size()][];
        optionalParams = new boolean[constructors.size()][];
        for (int i = 0; i < constructorsParamNames.length; i++) {
            var constructor = constructors.get(i);
            constructorsParamNames[i] = new String[constructor.size()];
            constructorsValues[i] = new Object[constructorsParamNames[i].length];
            optionalParams[i] = new boolean[constructorsParamNames[i].length];
            int j = 0;
            for (var entry : constructor.getParamTypeByName().entrySet()) {
                constructorsParamNames[i][j] = entry.getKey();
                try {
                    optionalParams[i][j] = entry.getValue().match(
                            scalar -> false,
                            compound -> compound.getContainer() == JsonType.Container.OPTIONAL);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                j++;
            }
        }
    }

    void start(DuplicateKeyStrategy strategy) {
        this.strategy = strategy;
        for (var constructorsValue : constructorsValues) {
            Arrays.fill(constructorsValue, null);
        }
    }

    public boolean set(String key, Object value) {
        var found = false;
        for (int i = 0; i < constructorsParamNames.length; i++) {
            for (int j = 0; j < constructorsParamNames[i].length; j++) {
                //noinspection StringEquality
                if (constructorsParamNames[i][j] == key) {
                    found = true;
                    if (constructorsValues[i][j] == null) {
                        constructorsValues[i][j] = value;
                    } else switch (strategy) {
                        case FAIL:
                            return false;
                        case KEEP_LAST:
                            constructorsValues[i][j] = value;
                            break;
                        case KEEP_FIRST:
                            break;
                    }
                }
            }
        }
        if (!found) {
            throw new IllegalArgumentException("POJO does not have field '" + key + "'");
        }
        return true;
    }

    public T create() {
        for (int i = 0; i < constructorsValues.length; i++) {
            var found = true;
            for (int j = 0; j < constructorsValues[i].length; j++) {
                if (constructorsValues[i][j] == null) {
                    if (optionalParams[i][j]) {
                        constructorsValues[i][j] = Optional.empty();
                    } else {
                        found = false;
                        break;
                    }
                }
            }
            if (found) {
                return constructors.get(i).createPojo(constructorsValues[i]);
            }
        }
        throw new PojoException("Unable to create instance of " +
                constructors.get(0).getConstructor().getDeclaringClass() + ", " +
                "available fields do not match any available constructor");
    }
}
