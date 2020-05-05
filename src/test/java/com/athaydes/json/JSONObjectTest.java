package com.athaydes.json;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONObjectTest implements TestHelper {

    final JSON json = new JSON();

    @Test
    void canParseEmptyObject() {
        assertEquals(Map.of(), json.parse("{}", Map.class));
        assertEquals(Map.of(), json.parse("{ }", Map.class));
        assertEquals(Map.of(), json.parse("   {   } ", Map.class));
    }

    @Test
    void canParseObjects() {
        assertEquals(Map.of("one", 1), json.parse("{\"one\": 1}", Map.class));
        assertEquals(Map.of("two", 2), json.parse("{\"two\": 2}", Map.class));
        assertEquals(Map.of("one", 1, "two", 2), json.parse("{\"one\": 1, \"two\": 2}", Map.class));
        assertEquals(Map.of("one", 1, "two", 2), json.parse("{\"one\" : 1 ,   \"two\"  : 2  } ", Map.class));
        assertEquals(Map.of("one", "uno", "two", "duo"), json.parse("{\"one\":\"uno\",\"two\":\"duo\"}", Map.class));
        assertEquals(Map.of("one", "uno", "two", 2), json.parse("{\"one\":\"uno\",\"two\": 2}", Map.class));
    }

    @Test
    void canParseRealisticObject() {
        // Source: http://www.json.org/example.html
        var example = "{\n" +
                "    \"glossary\": {\n" +
                "        \"title\": \"example glossary\",\n" +
                "\t\t\"GlossDiv\": {\n" +
                "            \"title\": \"S\",\n" +
                "\t\t\t\"GlossList\": {\n" +
                "                \"GlossEntry\": {\n" +
                "                    \"ID\": \"SGML\",\n" +
                "\t\t\t\t\t\"SortAs\": \"SGML\",\n" +
                "\t\t\t\t\t\"GlossTerm\": \"Standard Generalized Markup Language\",\n" +
                "\t\t\t\t\t\"Acronym\": \"SGML\",\n" +
                "\t\t\t\t\t\"Abbrev\": \"ISO 8879:1986\",\n" +
                "\t\t\t\t\t\"GlossDef\": {\n" +
                "                        \"para\": \"A meta-markup language, used to create markup languages such as DocBook.\",\n" +
                "\t\t\t\t\t\t\"GlossSeeAlso\": [\"GML\", \"XML\"]\n" +
                "                    },\n" +
                "\t\t\t\t\t\"GlossSee\": \"markup\"\n" +
                "                }\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "}";

        assertEquals(Map.of(
                "glossary", Map.of(
                        "title", "example glossary",
                        "GlossDiv", Map.of(
                                "title", "S",
                                "GlossList", Map.of(
                                        "GlossEntry", Map.ofEntries(
                                                Map.entry("ID", "SGML"),
                                                Map.entry("SortAs", "SGML"),
                                                Map.entry("GlossTerm", "Standard Generalized Markup Language"),
                                                Map.entry("Acronym", "SGML"),
                                                Map.entry("Abbrev", "ISO 8879:1986"),
                                                Map.entry("GlossDef", Map.of(
                                                        "para", "A meta-markup language, used to create markup languages such as DocBook.",
                                                        "GlossSeeAlso", List.of("GML", "XML")
                                                )),
                                                Map.entry("GlossSee", "markup")
                                        )
                                )
                        )
                )
        ), json.parse(example, Map.class));
    }
}
