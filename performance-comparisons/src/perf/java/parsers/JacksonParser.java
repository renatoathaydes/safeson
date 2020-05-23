package parsers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import data.TestObject;
import tests.Parser;

import java.io.InputStream;

public final class JacksonParser implements Parser<JsonNode, JsonNode> {
    final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String name() {
        return "Jackson 2.11.0";
    }

    @Override
    public String cliId() {
        return "jackson";
    }

    @Override
    public JsonNode parseObject(InputStream stream) throws Exception {
        return mapper.readTree(stream);
    }

    @Override
    public JsonNode parseArray(InputStream stream) throws Exception {
        return mapper.readTree(stream);
    }

    @Override
    public TestObject parsePojo(InputStream stream) throws Exception {
        return mapper.readValue(stream, TestObject.class);
    }

    @Override
    public void verifyObjectSize(JsonNode rootNode, int expectedSize) {
        assertSize(expectedSize, rootNode.size());
    }

    @Override
    public void verifyArraySize(JsonNode rootNode, int expectedSize) {
        assertSize(expectedSize, rootNode.size());
    }
}
