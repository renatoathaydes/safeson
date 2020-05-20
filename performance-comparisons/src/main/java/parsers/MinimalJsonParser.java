package parsers;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import tests.Parser;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public final class MinimalJsonParser implements Parser<JsonObject, JsonArray> {

    @Override
    public String name() {
        return "min-json 0.9.5";
    }

    @Override
    public String cliId() {
        return "min-json";
    }

    @Override
    public JsonObject parseObject(InputStream stream) throws Exception {
        return Json.parse(new InputStreamReader(stream, StandardCharsets.UTF_8)).asObject();
    }

    @Override
    public JsonArray parseArray(InputStream stream) throws Exception {
        return Json.parse(new InputStreamReader(stream, StandardCharsets.UTF_8)).asArray();
    }

    @Override
    public void verifyObjectSize(JsonObject object, int expectedSize) {
        assertSize(expectedSize, object.size());
    }

    @Override
    public void verifyArraySize(JsonArray object, int expectedSize) {
        assertSize(expectedSize, object.size());
    }
}
