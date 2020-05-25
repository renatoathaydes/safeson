package tests;

import data.TestObject;

import java.io.InputStream;

public interface Parser<Obj, Arr> {
    String name();

    String cliId();

    Obj parseObject(InputStream stream) throws Exception;

    Arr parseArray(InputStream stream) throws Exception;

    TestObject parsePojo(InputStream stream) throws Exception;

    void verifyObjectSize(Obj object, int expectedSize);

    void verifyArraySize(Arr object, int expectedSize);

    default void verifyPojo(TestObject pojo) {

    }

    default void assertSize(int expectedSize, int actualSize) {
        if (expectedSize != actualSize) {
            throw new AssertionError(name() + " FAILED: expected size " + expectedSize + " but was " + actualSize);
        }
    }
}
