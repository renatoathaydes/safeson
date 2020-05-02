package com.athaydes.json;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Most international Strings copied from http://www.madore.org/~david/misc/unitest/
 */
public class JSONTest {
    @Test
    public void canParseSimpleStrings() throws Exception {
        var example = "\"hello world\"";
        assertEquals(JSON.parse(example, String.class), "hello world");
    }

    @Test
    public void canParseRussianStrings() throws Exception {
        var example = "\"По оживлённым берегам\"";
        assertEquals(JSON.parse(example, String.class), "По оживлённым берегам");
    }

    @Test
    public void canParseGreekStrings() throws Exception {
        var example = "\"Ἰοὺ ἰού· τὰ πάντʼ ἂν ἐξήκοι σαφῆ\"";
        assertEquals(JSON.parse(example, String.class), "Ἰοὺ ἰού· τὰ πάντʼ ἂν ἐξήκοι σαφῆ");
    }

    @Test
    public void canParseSanskritStrings() throws Exception {
        var example = "\"पशुपतिरपि तान्यहानि कृच्छ्राद्\"";
        assertEquals(JSON.parse(example, String.class), "पशुपतिरपि तान्यहानि कृच्छ्राद्");
    }

    @Test
    public void canParseChineseStrings() throws Exception {
        var example = "\"子曰：「學而時習之，不亦說乎？有朋自遠方來，不亦樂乎？\"";
        assertEquals(JSON.parse(example, String.class), "子曰：「學而時習之，不亦說乎？有朋自遠方來，不亦樂乎？");
    }

    @Test
    public void canParseTamilStrings() throws Exception {
        var example = "\"ஸ்றீனிவாஸ ராமானுஜன் ஐயங்கார்\"";
        assertEquals(JSON.parse(example, String.class), "ஸ்றீனிவாஸ ராமானுஜன் ஐயங்கார்");
    }

    @Test
    public void canParseArabicStrings() throws Exception {
        var example = "\"صِرَاطَ ٱلَّذِينَ أَنعَمْتَ عَلَيهِمْ غَيرِ ٱلمَغضُوبِ عَلَيهِمْ وَلاَ ٱلضَّالِّينَ\"";
        assertEquals(JSON.parse(example, String.class), "صِرَاطَ ٱلَّذِينَ أَنعَمْتَ عَلَيهِمْ غَيرِ ٱلمَغضُوبِ عَلَيهِمْ وَلاَ ٱلضَّالِّينَ");
    }

    @Test
    public void canParseJapaneseStrings() throws Exception {
        // from https://en.wikipedia.org/wiki/Japanese_writing_system
        var example = "\"現代仮名遣い\"";
        assertEquals(JSON.parse(example, String.class), "現代仮名遣い");
    }

    @Test
    public void canParseSwedishStrings() throws Exception {
        // from https://sverigesradio.se/sida/artikel.aspx?programid=83&artikel=7465133
        var example = "\"Folkhälsomyndigheten har under flera veckor beskrivit läget i landet som att vi befinner oss på en hög platå där smittspridningen av coronaviruset inte längre ökar. \"";
        assertEquals(JSON.parse(example, String.class), "Folkhälsomyndigheten har under flera veckor beskrivit läget i landet som att vi befinner oss på en hög platå där smittspridningen av coronaviruset inte längre ökar. ");
    }

    @Test
    public void canParseUnicodeSequences() throws Exception {
        var example = "\"\\u65e5\\u672c\\u8a9e\\u6587\\u5b57\\u5217\"";
        assertEquals("\u65e5\u672c\u8a9e\u6587\u5b57\u5217",
                JSON.parse(example, String.class));
    }
}
