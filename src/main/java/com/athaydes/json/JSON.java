package com.athaydes.json;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public final class JSON {
    public static <T> T parse(String json, Class<T> type) throws Exception {
        var stream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
        return parse(stream, type);
    }

    public static <T> T parse(InputStream stream, Class<T> type) throws Exception {
        var jsonStream = new JsonStream(stream);
        if (type.equals(String.class)) {
            jsonStream.read();
            return type.cast(parseString(jsonStream));
        }
        if (type.equals(Boolean.class)) {
            jsonStream.read();
            return type.cast(parseBoolean(jsonStream));
        }
        return parseObject(jsonStream, type);
    }

    private static <T> T parseObject(JsonStream stream, Class<T> type) throws Exception {
        int b;
        T result = type.getDeclaredConstructor().newInstance();
        while ((b = stream.read()) > 0) {
            switch (b) {
                case '{':
                    while (true) {
                        skipWhitespace(stream);
                        var key = parseString(stream);
                        var field = type.getDeclaredField(key);
                        var fType = field.getType();
                        skipWhitespace(stream);
                        if (stream.bt != ':') {
                            throw new RuntimeException();
                        }
                        skipWhitespace(stream);
                        if (Number.class.isAssignableFrom(fType)) {
                            field.set(result, parseNumber(stream));
                        } else if (boolean.class.isAssignableFrom(fType)) {
                            field.set(result, parseBoolean(stream));
                        } else if (String.class.isAssignableFrom(fType)) {
                            field.set(result, parseString(stream));
                        }
                        skipWhitespace(stream);
                    }
                default:
                    throw new RuntimeException("Invalid JSON");
            }
        }
        return result;
    }

    private static boolean parseBoolean(JsonStream stream) throws IOException {
        switch (stream.bt) {
            case 't':
                if (stream.read() == 'r') {
                    if (stream.read() == 'u') {
                        if (stream.read() == 'e') {
                            return true;
                        }
                    }
                }
                throw new RuntimeException();
            case 'f':
                if (stream.read() == 'a') {
                    if (stream.read() == 'l') {
                        if (stream.read() == 's') {
                            if (stream.read() == 'e') {
                                return false;
                            }
                        }
                    }
                }
                throw new RuntimeException();
            default:
                throw new RuntimeException();
        }
    }

    private static Object parseNumber(JsonStream stream) {
        var c = stream.bt;
        while (true) {
            switch (c) {
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    throw new RuntimeException("Number not implemented");
            }
        }
    }

    private static String parseString(JsonStream stream) throws IOException {
        if (stream.bt == '"') {
            var builder = ByteBuffer.allocate(4096);
            int c;
            while ((c = stream.read()) > 0) {
                // if the highest bit is 1, this is not ASCII but a UTF-8 codepoint
                if ((c & 0b1000_0000) == 0b1000_0000) {
                    builder.put((byte) c);
                    continue;
                }
                if (c == '"') break;
                if (0x00 <= c && c <= 0x1f) {
                    throw new JsonException(stream.index, "Unescaped control character (hex = " +
                            Integer.toHexString(c) + ")");
                }
                if (c == '\\') {
                    c = stream.read();
                    if (c < 0) throw new JsonException(stream.index, "Unterminated String");
                    if (c == 'u') {
                        parseHexCode(stream, builder);
                    } else {
                        switch (c) {
                            case '"':
                                builder.put((byte) '"');
                                break;
                            case '\\':
                                builder.put((byte) '\\');
                                break;
                            case '/':
                                builder.put((byte) '/');
                                break;
                            case 'b':
                                builder.put((byte) '\b');
                                break;
                            case 'f':
                                builder.put((byte) '\f');
                                break;
                            case 'n':
                                builder.put((byte) '\n');
                                break;
                            case 'r':
                                builder.put((byte) '\r');
                                break;
                            case 't':
                                builder.put((byte) '\t');
                                break;
                            default:
                                // anything may be escaped!
                                builder.put((byte) c);
                        }
                    }
                } else {
                    builder.put((byte) c);
                }
            }
            byte[] bytes = builder.array();
            return new String(bytes, 0, builder.position(), StandardCharsets.UTF_8);
        } else {
            throw new JsonException(stream.index, "Expected '\"', got '" + ((char) stream.bt) + "'");
        }
    }

    private static void parseHexCode(JsonStream stream, ByteBuffer buffer) throws IOException {
        int code = 0;
        for (var shift = 3; shift >= 0; shift--) {
            var c = stream.read();
            if (c < 0) throw new RuntimeException("Unterminated unicode sequence");
            code += (hex(c) << (4 * shift));
        }
        if (code <= 0x0000_007F) {
            // case: 0xxxxxxx
            buffer.put((byte) code);
        } else if (code <= 0x0000_07FF) {
            // case: 110xxxxx 10xxxxxx
            buffer.put((byte) ((code >>> 6) | 0b1100_0000));
            buffer.put((byte) ((code & 0b0011_1111) | 0b1000_0000));
        } else if (code <= 0x0000_FFFF) {
            // The definition of UTF-8 prohibits encoding character numbers between
            // U+D800 and U+DFFF (https://tools.ietf.org/html/rfc3629#section-3)
            if (0xd800 <= code && code <= 0xdfff) {
                throw new JsonException(stream.index, "Illegal unicode sequence: " + Integer.toHexString(code));
            }

            // case: 1110xxxx 10xxxxxx 10xxxxxx
            buffer.put((byte) ((code >>> 12) | 0b1110_0000));
            buffer.put((byte) (((code >>> 6) & 0b0011_1111) | 0b1000_0000));
            buffer.put((byte) ((code & 0b0011_1111) | 0b1000_0000));
        } else if (code <= 0x0010_FFFF) {
            // case: 11110xxx 10xxxxxx 10xxxxxx 10xxxxxx
            buffer.put((byte) ((code >>> 18) | 0b1111_0000));
            buffer.put((byte) (((code >>> 12) & 0b0011_1111) | 0b1000_0000));
            buffer.put((byte) (((code >>> 6) & 0b0011_1111) | 0b1000_0000));
            buffer.put((byte) ((code & 0b0011_1111) | 0b1000_0000));
        } else {
            throw new JsonException(stream.index, "Invalid code unit sequence: " + Integer.toHexString(code));
        }
    }

    private static byte hex(int b) {
        switch (b) {
            case '0':
                return 0x0;
            case '1':
                return 0x1;
            case '2':
                return 0x2;
            case '3':
                return 0x3;
            case '4':
                return 0x4;
            case '5':
                return 0x5;
            case '6':
                return 0x6;
            case '7':
                return 0x7;
            case '8':
                return 0x8;
            case '9':
                return 0x9;
            case 'a':
            case 'A':
                return 0xA;
            case 'b':
            case 'B':
                return 0xB;
            case 'c':
            case 'C':
                return 0xC;
            case 'd':
            case 'D':
                return 0xD;
            case 'e':
            case 'E':
                return 0xE;
            case 'f':
            case 'F':
                return 0xF;
            default:
                throw new RuntimeException("Illegal hex digit");
        }
    }

    private static void skipWhitespace(JsonStream stream) throws IOException {
        int b;
        while ((b = stream.read()) > 0) {
            switch (b) {
                case ' ':
                case '\t':
                case '\n':
                case '\r':
                    continue;
                default:
                    break;
            }
        }
    }

    private static final class JsonStream extends InputStream {
        final InputStream stream;
        int bt;
        int index;

        public JsonStream(InputStream stream) {
            this.stream = stream;
        }

        @Override
        public int read() throws IOException {
            bt = stream.read();
            if (bt > 0) {
                index++;
            }
            return bt;
        }
    }
}
