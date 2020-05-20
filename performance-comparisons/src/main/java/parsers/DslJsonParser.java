package parsers;

import com.dslplatform.json.DslJson;
import tests.Parser;

import java.io.InputStream;
import java.util.Map;

public final class DslJsonParser implements Parser<Map<?, ?>, long[]> {

    final DslJson<Object> json = new DslJson();

    @Override
    public String name() {
        return "dsl-json 1.9.5";
    }

    @Override
    public String cliId() {
        return "dsl-json";
    }

    @Override
    public Map<?, ?> parseObject(InputStream stream) throws Exception {
        return json.deserialize(Map.class, stream);
    }

    @Override
    public long[] parseArray(InputStream stream) throws Exception {
        return json.deserialize(long[].class, stream);
    }

    @Override
    public void verifyObjectSize(Map<?, ?> object, int expectedSize) {
        assertSize(expectedSize, object.size());
    }

    @Override
    public void verifyArraySize(long[] object, int expectedSize) {
        assertSize(expectedSize, object.length);
    }
}
