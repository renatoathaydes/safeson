package com.athaydes.json;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public final class JSON {

    private final JsonConfig config;

    private ByteBuffer buffer;


    public JSON() {
        this(JsonConfig.DEFAULT);
    }

    public JSON(JsonConfig config) {
        this.config = config;
        buffer = ByteBuffer.allocate(Math.min(1024, config.maxStringLength()));
        buffer.mark();
    }

    public <T> T parse(String json, Class<T> type) {
        var stream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
        return parse(stream, type);
    }

    public <T> T parse(InputStream stream, Class<T> type) {
        var jsonStream = new JsonStream(stream);
        try {
            skipWhitespace(jsonStream);
            T result;
            if (type.equals(String.class)) {
                result = type.cast(parseString(jsonStream));
            } else if (type.equals(Boolean.class)) {
                result = type.cast(parseBoolean(jsonStream));
            } else {
                result = parseObject(jsonStream, type);
            }
            if (config.shouldConsumeTrailingContent()) {
                verifyNoTrailingContent(jsonStream);
            }
            return result;
        } catch (JsonException e) {
            throw e;
        } catch (Exception e) {
            throw new JsonException(e);
        }
    }

    private <T> T parseObject(JsonStream stream, Class<T> type) throws Exception {
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

    private boolean parseBoolean(JsonStream stream) throws IOException {
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

    private Object parseNumber(JsonStream stream) {
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

    private String parseString(JsonStream stream) throws IOException {
        buffer.reset();
        if (stream.bt == '"') {
            var index = 0;
            // we may add up to 6 bytes in a single loop iteration
            var bufferMaxBytes = buffer.capacity() - 6;
            int c;
            boolean done = false;
            while ((c = stream.read()) > 0) {
                index++;
                if (index == bufferMaxBytes) {
                    growBuffer(stream.index);
                }
                // if the highest bit is 1, this is not ASCII but a UTF-8 codepoint
                if ((c & 0b1000_0000) == 0b1000_0000) {
                    buffer.put((byte) c);
                    continue;
                }
                if (c == '"') {
                    done = true;
                    break;
                }
                if (0x00 <= c && c <= 0x1f) {
                    throw new JsonException(stream.index, "Unescaped control character (hex = " +
                            Integer.toHexString(c) + ")");
                }
                if (c == '\\') {
                    c = stream.read();
                    if (c < 0) throw new JsonException(stream.index, "Unterminated String");
                    if (c == 'u') {
                        parseHexCode(stream);
                    } else {
                        switch (c) {
                            case '"':
                                buffer.put((byte) '"');
                                break;
                            case '\\':
                                buffer.put((byte) '\\');
                                break;
                            case '/':
                                buffer.put((byte) '/');
                                break;
                            case 'b':
                                buffer.put((byte) '\b');
                                break;
                            case 'f':
                                buffer.put((byte) '\f');
                                break;
                            case 'n':
                                buffer.put((byte) '\n');
                                break;
                            case 'r':
                                buffer.put((byte) '\r');
                                break;
                            case 't':
                                buffer.put((byte) '\t');
                                break;
                            default:
                                // anything may be escaped!
                                buffer.put((byte) c);
                        }
                    }
                } else {
                    buffer.put((byte) c);
                }
            }
            if (!done) {
                throw new JsonException(stream.index, "Unterminated String");
            }
            byte[] bytes = buffer.array();
            return new String(bytes, 0, buffer.position(), StandardCharsets.UTF_8);
        } else {
            throw new JsonException(stream.index, "Expected '\"', got '" + ((char) stream.bt) + "'");
        }
    }

    private void parseHexCode(JsonStream stream) throws IOException {
        int code = 0;
        for (var shift = 3; shift >= 0; shift--) {
            var c = stream.read();
            if (c < 0) throw new JsonException(stream.index, "Unterminated unicode sequence");
            code += (hex(c, stream.index) << (4 * shift));
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
                throw new JsonException(stream.index - 4, "Illegal unicode sequence: " + Integer.toHexString(code));
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
            throw new JsonException(stream.index - 4, "Invalid code unit sequence: " + Integer.toHexString(code));
        }
    }

    private void growBuffer(int index) {
        if (buffer.capacity() == config.maxStringLength()) {
            throw new JsonException(index,
                    "Maximum string length has been reached, unable to allocate enough memory for String");
        }
        ByteBuffer newBuffer = ByteBuffer.allocate(Math.min(buffer.capacity() * 2, config.maxStringLength()));
        var pos = buffer.position();
        newBuffer.put(buffer.array(), 0, pos);
        buffer = newBuffer;
        buffer.position(0);
        buffer.mark();
        buffer.position(pos);
    }

    private static byte hex(int b, int index) {
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
                throw new JsonException(index, "Illegal hex digit");
        }
    }

    private void verifyNoTrailingContent(JsonStream jsonStream) throws IOException {
        skipWhitespace(jsonStream);
        if (jsonStream.bt >= 0) {
            throw new JsonException(jsonStream.index, "Illegal trailing content");
        }
    }

    private void skipWhitespace(JsonStream stream) throws IOException {
        var index = 0;
        final var maxIndex = config.maxWhitespace();
        int b;
        loop:
        while ((b = stream.read()) > 0) {
            switch (b) {
                case ' ':
                case '\t':
                case '\n':
                case '\r':
                    if (index >= maxIndex) {
                        throw new JsonException(stream.index, "Too much whitespace");
                    }
                    index++;
                    break;
                default:
                    break loop;
            }
        }
    }

    private static final class JsonStream extends InputStream {
        final InputStream stream;
        int bt;
        int index = -1;

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
