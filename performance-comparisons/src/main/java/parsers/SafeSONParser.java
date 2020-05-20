package parsers;

import com.athaydes.json.JSON;
import tests.Parser;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public final class SafeSONParser implements Parser<Map<?, ?>, List<?>> {

    final JSON parser = new JSON();

    @Override
    public String name() {
        return "SafeSON 1.0";
    }

    @Override
    public String cliId() {
        return "safeson";
    }

    @Override
    public Map<?, ?> parseObject(InputStream stream) throws Exception {
        return parser.parse(stream, Map.class);
    }

    @Override
    public List<?> parseArray(InputStream stream) throws Exception {
        return parser.parse(stream, List.class);
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
