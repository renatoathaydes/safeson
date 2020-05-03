package com.athaydes.json;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Most international Strings copied from http://www.madore.org/~david/misc/unitest/
 */
public class JSONTest {
    @Test
    public void canParseSimpleStrings() throws Exception {
        var example = "\"hello world\"";
        assertEquals("hello world", JSON.parse(example, String.class));
    }

    @Test
    public void canParseRussianStrings() throws Exception {
        var example = "\"По оживлённым берегам\"";
        assertEquals("По оживлённым берегам", JSON.parse(example, String.class));
    }

    @Test
    public void canParseGreekStrings() throws Exception {
        var example = "\"Ἰοὺ ἰού· τὰ πάντʼ ἂν ἐξήκοι σαφῆ\"";
        assertEquals("Ἰοὺ ἰού· τὰ πάντʼ ἂν ἐξήκοι σαφῆ", JSON.parse(example, String.class));
    }

    @Test
    public void canParseSanskritStrings() throws Exception {
        var example = "\"पशुपतिरपि तान्यहानि कृच्छ्राद्\"";
        assertEquals("पशुपतिरपि तान्यहानि कृच्छ्राद्", JSON.parse(example, String.class));
    }

    @Test
    public void canParseChineseStrings() throws Exception {
        var example = "\"子曰：「學而時習之，不亦說乎？有朋自遠方來，不亦樂乎？\"";
        assertEquals("子曰：「學而時習之，不亦說乎？有朋自遠方來，不亦樂乎？", JSON.parse(example, String.class));
    }

    @Test
    public void canParseTamilStrings() throws Exception {
        var example = "\"ஸ்றீனிவாஸ ராமானுஜன் ஐயங்கார்\"";
        assertEquals("ஸ்றீனிவாஸ ராமானுஜன் ஐயங்கார்", JSON.parse(example, String.class));
    }

    @Test
    public void canParseArabicStrings() throws Exception {
        var example = "\"صِرَاطَ ٱلَّذِينَ أَنعَمْتَ عَلَيهِمْ غَيرِ ٱلمَغضُوبِ عَلَيهِمْ وَلاَ ٱلضَّالِّينَ\"";
        assertEquals("صِرَاطَ ٱلَّذِينَ أَنعَمْتَ عَلَيهِمْ غَيرِ ٱلمَغضُوبِ عَلَيهِمْ وَلاَ ٱلضَّالِّينَ", JSON.parse(example, String.class));
    }

    @Test
    public void canParseJapaneseStrings() throws Exception {
        // from https://en.wikipedia.org/wiki/Japanese_writing_system
        var example = "\"現代仮名遣い\"";
        assertEquals("現代仮名遣い", JSON.parse(example, String.class));
    }

    @Test
    public void canParseSwedishStrings() throws Exception {
        // from https://sverigesradio.se/sida/artikel.aspx?programid=83&artikel=7465133
        var example = "\"Folkhälsomyndigheten har under flera veckor beskrivit läget i landet som att vi befinner oss på en hög platå där smittspridningen av coronaviruset inte längre ökar. \"";
        assertEquals("Folkhälsomyndigheten har under flera veckor beskrivit läget i landet som att vi befinner oss på en hög platå där smittspridningen av coronaviruset inte längre ökar. ",
                JSON.parse(example, String.class));
    }

    @Test
    public void canParseUnicodeSequences() throws Exception {
        var example = "\"\\u65e5\\u672c\\u8a9e\\u6587\\u5b57\\u5217\"";
        assertEquals("\u65e5\u672c\u8a9e\u6587\u5b57\u5217",
                JSON.parse(example, String.class));
    }

    @Test
    public void canParseBoolean() throws Exception {
        assertEquals(true, JSON.parse("true", Boolean.class));
        assertEquals(false, JSON.parse("false", Boolean.class));
    }
}
