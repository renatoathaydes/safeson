package com.athaydes.json;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Most international Strings copied from http://www.madore.org/~david/misc/unitest/
 */
public class JSONStringTest implements TestHelper {

    final JSON json = new JSON();

    @Test
    public void canParseSimpleStrings() throws Exception {
        var example = "\"hello world\"";
        assertEquals("hello world", json.parse(example, String.class));
    }

    @Test
    public void canParseUsingNoTypeParameter() {
        var example = "\"JSON\"";
        assertEquals("JSON", json.parse(example));
    }

    @Test
    public void canParseRussianStrings() throws Exception {
        var example = "\"По оживлённым берегам\"";
        assertEquals("По оживлённым берегам", json.parse(example, String.class));
    }

    @Test
    public void canParseGreekStrings() throws Exception {
        var example = "\"Ἰοὺ ἰού· τὰ πάντʼ ἂν ἐξήκοι σαφῆ\"";
        assertEquals("Ἰοὺ ἰού· τὰ πάντʼ ἂν ἐξήκοι σαφῆ", json.parse(example, String.class));
    }

    @Test
    public void canParseSanskritStrings() throws Exception {
        var example = "\"पशुपतिरपि तान्यहानि कृच्छ्राद्\"";
        assertEquals("पशुपतिरपि तान्यहानि कृच्छ्राद्", json.parse(example, String.class));
    }

    @Test
    public void canParseChineseStrings() throws Exception {
        var example = "\"子曰：「學而時習之，不亦說乎？有朋自遠方來，不亦樂乎？\"";
        assertEquals("子曰：「學而時習之，不亦說乎？有朋自遠方來，不亦樂乎？", json.parse(example, String.class));
    }

    @Test
    public void canParseTamilStrings() throws Exception {
        var example = "\"ஸ்றீனிவாஸ ராமானுஜன் ஐயங்கார்\"";
        assertEquals("ஸ்றீனிவாஸ ராமானுஜன் ஐயங்கார்", json.parse(example, String.class));
    }

    @Test
    public void canParseArabicStrings() throws Exception {
        var example = "\"صِرَاطَ ٱلَّذِينَ أَنعَمْتَ عَلَيهِمْ غَيرِ ٱلمَغضُوبِ عَلَيهِمْ وَلاَ ٱلضَّالِّينَ\"";
        assertEquals("صِرَاطَ ٱلَّذِينَ أَنعَمْتَ عَلَيهِمْ غَيرِ ٱلمَغضُوبِ عَلَيهِمْ وَلاَ ٱلضَّالِّينَ", json.parse(example, String.class));
    }

    @Test
    public void canParseJapaneseStrings() throws Exception {
        // from https://en.wikipedia.org/wiki/Japanese_writing_system
        var example = "\"現代仮名遣い\"";
        assertEquals("現代仮名遣い", json.parse(example, String.class));
    }

    @Test
    public void canParseSwedishStrings() throws Exception {
        // from https://sverigesradio.se/sida/artikel.aspx?programid=83&artikel=7465133
        var example = "\"Folkhälsomyndigheten har under flera veckor beskrivit läget i landet som att vi befinner oss på en hög platå där smittspridningen av coronaviruset inte längre ökar. \"";
        assertEquals("Folkhälsomyndigheten har under flera veckor beskrivit läget i landet som att vi befinner oss på en hög platå där smittspridningen av coronaviruset inte längre ökar. ",
                json.parse(example, String.class));
    }

    @Test
    public void canParseUnicodeSequences() throws Exception {
        var example = "\"\\u65e5\\u672c\\u8a9e\\u6587\\u5b57\\u5217\"";
        assertEquals("\u65e5\u672c\u8a9e\u6587\u5b57\u5217",
                json.parse(example, String.class));
    }

    @Test
    public void canParseProblemStrings() throws Exception {
        assertEquals("A\u0000B", json.parse("\"A\\u0000B\"", String.class));

        assertEquals("\"/\b\f\n\r\t",
                json.parse("\"\\\"\\/\\b\\f\\n\\r\\t\"", String.class));

        assertEquals("\\", json.parse("\"\\u005C\"", String.class));

        // TODO support UTF-16 surrogate pairs
        //assertEquals("\uD834\uDD1E", json.parse("\"\\uD834\\uDD1E\"", String.class));

    }

    @Test
    public void rejectsInvalidStrings() {
        // illegal unicode characters: https://tools.ietf.org/html/rfc3629#section-3
        assertThrowsJsonException(() -> json.parse("\"\\uD800\"", String.class),
                "Illegal unicode sequence: d800", 2);
        assertThrowsJsonException(() -> json.parse("\"foo \\uDFFF\"", String.class),
                "Illegal unicode sequence: dfff", 6);

        // syntax errors
        assertThrowsJsonException(() -> json.parse("\"\\", String.class),
                "Unterminated String", 1);
        assertThrowsJsonException(() -> json.parse("\"\\\"", String.class),
                "Unterminated String", 2);
        assertThrowsJsonException(() -> json.parse("\"foo", String.class),
                "Unterminated String", 3);
        assertThrowsJsonException(() -> json.parse("foo", String.class),
                "Expected '\"', got 'f'", 0);
        assertThrowsJsonException(() -> json.parse("\\", String.class),
                "Expected '\"', got '\\'", 0);
        assertThrowsJsonException(() -> json.parse("\"\\uabc", String.class),
                "Unterminated unicode sequence", 5);
        assertThrowsJsonException(() -> json.parse("\"\\uabc\"", String.class),
                "Illegal hex digit", 6);
        assertThrowsJsonException(() -> json.parse("\"\\uabcx\"", String.class),
                "Illegal hex digit", 6);
    }

}
