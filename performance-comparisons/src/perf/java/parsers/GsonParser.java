package parsers;

import com.google.gson.Gson;
import data.TestObject;
import tests.Parser;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public final class GsonParser implements Parser<Map<?, ?>, List<?>> {
    final Gson gson = new Gson();

    @Override
    public String name() {
        return "GSON 2.8.6";
    }

    @Override
    public String cliId() {
        return "gson";
    }

    @Override
    public Map<?, ?> parseObject(InputStream stream) throws Exception {
        return gson.fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), Map.class);
    }

    @Override
    public List<?> parseArray(InputStream stream) throws Exception {
        return gson.fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), List.class);
    }

    @Override
    public TestObject parsePojo(InputStream stream) throws Exception {
        return gson.fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), TestObject.class);
    }

    @Override
    public void verifyObjectSize(Map<?, ?> object, int expectedSize) {
        assertSize(expectedSize, object.size());
    }

    @Override
    public void verifyArraySize(List<?> object, int expectedSize) {
        assertSize(expectedSize, object.size());
    }
}
