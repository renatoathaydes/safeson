package com.athaydes.json;

import com.athaydes.json.pojo.JsonType;
import com.athaydes.json.pojo.PojoException;
import com.athaydes.json.pojo.PojoMapper;
import com.athaydes.json.pojo.Pojos;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class JSON {

    private final JsonConfig config;
    private final Pojos pojos;

    private ByteBuffer buffer;

    public JSON() {
        this(JsonConfig.DEFAULT, Pojos.EMPTY);
    }

    public JSON(JsonConfig config) {
        this(config, Pojos.EMPTY);
    }

    public JSON(Pojos pojos) {
        this(JsonConfig.DEFAULT, pojos);
    }

    public JSON(JsonConfig config, Pojos pojos) {
        this.config = config;
        this.pojos = pojos;
        buffer = ByteBuffer.allocate(Math.min(1024, config.maxStringLength()));
        buffer.mark();
    }

    public Object parse(String json) {
        var stream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
        return parse(stream, Object.class, 0);
    }

    public <T> T parse(String json, Class<T> type) {
        var stream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
        return parse(stream, type, 0);
    }

    public Object parse(InputStream stream) {
        return parse(stream, Object.class, 0);
    }

    public <T> T parse(InputStream stream, Class<T> type) {
        return parse(stream, type, 0);
    }

    private <T> T parse(InputStream stream, Class<T> type, int recursionLevel) {
        JsonStream jsonStream = new JsonStream(stream);
        try {
            jsonStream.read();
        } catch (IOException e) {
            throw new JsonException(0, "Unable to read input");
        }
        var result = parse(jsonStream, type, recursionLevel);
        if (config.shouldConsumeTrailingContent()) {
            try {
                verifyNoTrailingContent(jsonStream);
            } catch (IOException e) {
                throw new JsonException(e);
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private <T> T parse(JsonStream jsonStream, Class<T> type, int recursionLevel) {
        try {
            skipWhitespace(jsonStream);
            T result;
            if (type.isPrimitive() || Number.class.isAssignableFrom(type)) {
                if (type.equals(Number.class)) {
                    result = (T) parseNumber(jsonStream);
                } else if (type.equals(Integer.class) || type.equals(int.class)) {
                    result = (T) Integer.valueOf(parseNumber(jsonStream).intValue());
                } else if (type.equals(Double.class) || type.equals(double.class)) {
                    result = (T) Double.valueOf(parseNumber(jsonStream).doubleValue());
                } else if (type.equals(Long.class) || type.equals(long.class)) {
                    result = (T) Long.valueOf(parseNumber(jsonStream).longValue());
                } else if (type.equals(boolean.class)) {
                    result = (T) parseBoolean(jsonStream);
                } else {
                    throw new JsonException(jsonStream.index, "Unmapped numeric type (only int, double and long " +
                            "- boxed or not - are supported): " + type);
                }
            } else if (type.equals(String.class)) {
                result = type.cast(parseString(jsonStream, null));
            } else if (type.equals(Boolean.class)) {
                result = type.cast(parseBoolean(jsonStream));
            } else if (type.equals(List.class)) {
                result = type.cast(parseArray(jsonStream, null, recursionLevel + 1));
            } else if (type.equals(Object.class)) {
                result = type.cast(probeTypeThenParse(jsonStream, recursionLevel + 1));
            } else if (type.equals(Void.class)) {
                result = type.cast(parseNull(jsonStream));
            } else if (type.equals(Map.class)) {
                result = type.cast(parseObjectToMap(jsonStream, null, recursionLevel + 1));
            } else {
                var mapper = pojos.getMapper(type);
                if (mapper == null) {
                    throw new JsonException(jsonStream.index, "Unmapped type: " + type);
                }
                result = type.cast(parsePojo(jsonStream, mapper, recursionLevel + 1));
            }
            return result;
        } catch (JsonException | PojoException e) {
            throw e;
        } catch (Exception e) {
            throw new JsonException(e);
        }
    }

    private Object parse(JsonStream jsonStream, JsonType type, int recursionLevel) throws Exception {
        return type.match(scalar -> parse(jsonStream, scalar.getValueType(), recursionLevel), compound -> {
            switch (compound.getContainer()) {
                case MAP:
                    return parseObjectToMap(jsonStream, compound.getType(), recursionLevel + 1);
                case LIST:
                    return parseArray(jsonStream, compound.getType(), recursionLevel + 1);
                case OPTIONAL:
                    return Optional.of(parse(jsonStream, compound.getType(), recursionLevel));
                case NONE:
                    return parse(jsonStream, compound.getValueType(), recursionLevel);
                case UNSUPPORTED:
                default:
                    throw new IllegalStateException("Unexpected type container: " + compound.getContainer());
            }
        });
    }

    private Object probeTypeThenParse(JsonStream stream, int recursionLevel) throws Exception {
        switch (stream.bt) {
            case '"':
                return parseString(stream, null);
            case 't':
            case 'f':
                return parseBoolean(stream);
            case 'n':
                return parseNull(stream);
            case '{':
                return parseObjectToMap(stream, null, recursionLevel + 1);
            case '[':
                return parseArray(stream, null, recursionLevel + 1);
        }
        if (startsNumber(stream.bt)) {
            return parseNumber(stream);
        }
        throw new JsonException(stream.index, "Invalid literal");
    }

    private Map<String, Object> parseObjectToMap(JsonStream stream,
                                                 JsonType valueType,
                                                 int recursionLevel) throws Exception {
        if (stream.bt != '{') {
            throw new JsonException(stream.index, "Expected object but got '" + ((char) stream.bt) + "'");
        }
        if (recursionLevel >= config.maxRecursionDepth()) {
            throw new JsonException(stream.index, "Recursion limit breached");
        }
        stream.read();
        skipWhitespace(stream);
        int c = stream.bt;
        Map<String, Object> result = new LinkedHashMap<>();
        if (c == '}') {
            stream.read();
            return result;
        }
        var mapUpdater = config.mapUpdater();
        while (c > 0) {
            var keyIndex = stream.index;
            var key = parseString(stream, null);
            skipWhitespace(stream);
            c = stream.bt;
            if (c == ':') {
                stream.read();
                skipWhitespace(stream);
                c = stream.bt;
            } else if (c < 0) {
                break;
            } else {
                throw new JsonException(stream.index, "Expected ':' or '}', got '" + ((char) c) + "'");
            }
            if (c < 0) {
                break;
            }
            var ok = mapUpdater.updateMap(result, key, valueType == null
                    ? probeTypeThenParse(stream, recursionLevel)
                    : parse(stream, valueType, recursionLevel));
            if (!ok) {
                throw new JsonException(keyIndex, "Duplicate key: " + key);
            }
            c = stream.bt;
            if (c <= 0) {
                throw new JsonException(stream.index, "Unterminated object");
            }
            skipWhitespace(stream);
            c = stream.bt;
            if (c == ',') {
                stream.read();
                skipWhitespace(stream);
                c = stream.bt;
            } else if (c == '}') {
                stream.read();
                return result;
            } else if (c < 0) {
                break;
            } else {
                throw new JsonException(stream.index, "Expected ',' or '}', got '" + ((char) c) + "'");
            }
        }
        throw new JsonException(stream.index, "Unterminated object");
    }

    private Object parsePojo(JsonStream stream, PojoMapper<?> mapper, int recursionLevel) throws Exception {
        if (stream.bt != '{') {
            throw new JsonException(stream.index, "Expected object but got '" + ((char) stream.bt) + "'");
        }
        if (recursionLevel >= config.maxRecursionDepth()) {
            throw new JsonException(stream.index, "Recursion limit breached");
        }
        stream.read();
        skipWhitespace(stream);
        int c = stream.bt;
        var creator = mapper.getCreator(config.duplicateKeyStrategy());
        if (c == '}') {
            stream.read();
            return creator.create();
        }
        while (c > 0) {
            var keyIndex = stream.index;
            var key = parseString(stream, mapper);
            skipWhitespace(stream);
            c = stream.bt;
            if (c == ':') {
                stream.read();
                skipWhitespace(stream);
                c = stream.bt;
            } else if (c < 0) {
                break;
            } else {
                throw new JsonException(stream.index, "Expected ':' or '}', got '" + ((char) c) + "'");
            }
            if (c < 0) {
                break;
            }
            var expectedValueType = mapper.getTypeOf(key);
            Object value = null;
            if (expectedValueType == null) {
                // TODO discard parsed content
                probeTypeThenParse(stream, recursionLevel);
            } else if (expectedValueType.match(scalar -> false,
                    compound -> compound.getContainer() == JsonType.Container.OPTIONAL) &&
                    stream.bt == 'n') {
                value = Optional.ofNullable(parseNull(stream));
            } else try {
                value = parse(stream, expectedValueType, recursionLevel);
            } catch (JsonException e) {
                throw new PojoException("Invalid value for key '" + key + "'", e);
            }
            var ok = value == null ? true : creator.set(key, value);
            if (!ok) {
                throw new JsonException(keyIndex, "Duplicate key: " + key);
            }
            c = stream.bt;
            if (c <= 0) {
                throw new JsonException(stream.index, "Unterminated object");
            }
            skipWhitespace(stream);
            c = stream.bt;
            if (c == ',') {
                stream.read();
                skipWhitespace(stream);
                c = stream.bt;
            } else if (c == '}') {
                stream.read();
                return creator.create();
            } else if (c < 0) {
                break;
            } else {
                throw new JsonException(stream.index, "Expected ',' or '}', got '" + ((char) c) + "'");
            }
        }
        throw new JsonException(stream.index, "Unterminated object");
    }

    private List<Object> parseArray(JsonStream stream, JsonType itemType, int recursionLevel) throws Exception {
        if (stream.bt != '[') {
            throw new JsonException(stream.index, "Expected array but got '" + ((char) stream.bt) + "'");
        }
        if (recursionLevel > config.maxRecursionDepth()) {
            throw new JsonException(stream.index, "Recursion limit breached");
        }
        stream.read();
        skipWhitespace(stream);
        int c = stream.bt;
        var result = new ArrayList<>();
        if (c == ']') {
            stream.read();
            return result;
        }
        while (c > 0) {
            if (itemType == null) {
                result.add(probeTypeThenParse(stream, recursionLevel));
            } else {
                result.add(parse(stream, itemType, recursionLevel));
            }
            c = stream.bt;
            if (c <= 0) {
                throw new JsonException(stream.index, "Unterminated array");
            }
            skipWhitespace(stream);
            c = stream.bt;
            if (c == ',') {
                stream.read();
                skipWhitespace(stream);
                c = stream.bt;
            } else if (c == ']') {
                stream.read();
                return result;
            } else if (c < 0) {
                break;
            } else {
                throw new JsonException(stream.index, "Expected ',' or ']', got '" + ((char) c) + "'");
            }
        }
        throw new JsonException(stream.index, "Unterminated array");
    }

    private Void parseNull(JsonStream stream) throws IOException {
        if (stream.bt == 'n') {
            if (stream.read() == 'u') {
                if (stream.read() == 'l') {
                    if (stream.read() == 'l') {
                        stream.read();
                        return null;
                    }
                }
            }
        }
        throw new JsonException(stream.index, "Invalid literal");
    }

    private Boolean parseBoolean(JsonStream stream) throws IOException {
        switch (stream.bt) {
            case 't':
                if (stream.read() == 'r') {
                    if (stream.read() == 'u') {
                        if (stream.read() == 'e') {
                            stream.read();
                            return Boolean.TRUE;
                        }
                    }
                }
            case 'f':
                if (stream.read() == 'a') {
                    if (stream.read() == 'l') {
                        if (stream.read() == 's') {
                            if (stream.read() == 'e') {
                                stream.read();
                                return Boolean.FALSE;
                            }
                        }
                    }
                }
        }
        throw new JsonException(stream.index, "Invalid literal");
    }

    private Number parseNumber(JsonStream stream) throws IOException {
        buffer.reset();
        boolean isNegative;
        long intPart = 0;
        double fracPart = 0;
        int expPart = 0;
        var c = stream.bt;
        if (c == '-') {
            isNegative = true;
            c = stream.read();
        } else {
            isNegative = false;
        }
        if (c == '0') {
            stream.read();
            buffer.put((byte) 0); // use buffer to avoid error later
        } else if ('1' <= c && c <= '9') {
            intPart = parseLong(stream);
        } else {
            throw new JsonException(stream.index, "Invalid literal");
        }
        if (stream.bt == '.') {
            c = stream.read();
            if ('0' <= c && c <= '9') {
                fracPart = parseFraction(stream);
            } else if (c < 0) {
                throw new JsonException(stream.index, "Unterminated number");
            } else {
                throw new JsonException(stream.index, "Invalid literal");
            }
        }
        c = stream.bt;
        if (c == 'e' || c == 'E') {
            var isExpNegative = false;
            c = stream.read();
            if (c == '-' || c == '+') {
                isExpNegative = (c == '-');
                c = stream.read();
            }
            if ('0' <= c && c <= '9') {
                expPart = Math.toIntExact(parseLong(stream));
                if (isExpNegative) {
                    expPart = expPart * -1;
                }
            } else if (c < 0) {
                throw new JsonException(stream.index, "Unterminated number");
            } else {
                throw new JsonException(stream.index, "Invalid literal");
            }
        }
        // at least one byte should have been put into the buffer, otherwise we parsed no input at all
        if (buffer.position() == 0) {
            throw new JsonException(stream.index, "Invalid literal");
        }
        if (isNegative) {
            intPart = intPart * -1;
            fracPart = fracPart * -1.0;
        }
        if (fracPart == 0L && expPart == 0) {
            if (intPart < Integer.MAX_VALUE) {
                return (int) intPart;
            }
            return intPart;
        } else {
            var mult = pow10(expPart);
            // the order of the operations matters! Sum last to avoid precision being lost.
            return intPart * mult + fracPart * mult;
        }
    }

    private long parseLong(JsonStream stream) throws IOException {
        buffer.reset();
        // assume the first digit has been checked
        buffer.put((byte) (stream.bt - 48));
        int c;
        while ((c = stream.read()) > 0 && '0' <= c && c <= '9') {
            buffer.put((byte) (c - 48));
        }
        final var pos = buffer.position();
        long result = 0;
        for (var i = 0; i < pos; i++) {
            long b = buffer.get(i);
            result += b * ((long) pow10(pos - i - 1));
        }
        return result;
    }

    private static double parseFraction(JsonStream stream) throws IOException {
        var result = 0d;
        var c = stream.bt - 48;
        for (int i = -1; 0 <= c && c <= 9; i--) {
            result += c * pow10(i);
            var b = stream.read();
            if (b < 0) {
                break;
            }
            c = b - 48;
        }
        return result;
    }

    private String parseString(JsonStream stream,
                               PojoMapper<?> mapper) throws IOException {
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
                    // except for the illegal FF
                    if (c == 0xff) {
                        throw new JsonException(stream.index, "Illegal codepoint: " + Integer.toHexString(c));
                    }
                    buffer.put((byte) c);
                    continue;
                }
                if (c == '"') {
                    done = true;
                    stream.read();
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
                                throw new JsonException(stream.index, "Illegal escaped character: " + ((char) c));
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
            return mapper == null ?
                    new String(bytes, 0, buffer.position(), StandardCharsets.UTF_8)
                    : mapper.keyFor(bytes, buffer.position());
        } else {
            throw new JsonException(stream.index, "Expected '\"', got '" + ((char) stream.bt) + "'");
        }
    }

    private void parseHexCode(JsonStream stream) throws IOException {
        int code = readHexCode(stream);
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
                // but the JSON spec allows UTF-16 surrogate pairs, so check for that!
                tryReadUtf16Surrogate(stream, code);
            } else {
                // case: 1110xxxx 10xxxxxx 10xxxxxx
                buffer.put((byte) ((code >>> 12) | 0b1110_0000));
                buffer.put((byte) (((code >>> 6) & 0b0011_1111) | 0b1000_0000));
                buffer.put((byte) ((code & 0b0011_1111) | 0b1000_0000));
            }
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

    private void tryReadUtf16Surrogate(JsonStream stream, int highSurrogate) throws IOException {
        // https://tools.ietf.org/html/rfc8259#section-7
        // To escape an extended character that is not in the Basic Multilingual
        // Plane, the character is represented as a 12-character sequence,
        // encoding the UTF-16 surrogate pair.

        // UTF-16 RFC: https://tools.ietf.org/html/rfc2781#section-2
        // Characters with values between 0x10000 and 0x10FFFF are
        // represented by a 16-bit integer with a value between 0xD800 and 0xDBFF (...)
        // followed by a 16-bit integer with a value between 0xDC00 and 0xDFFF (...)
        int code;
        if (0xd800 <= highSurrogate && highSurrogate <= 0xdbff) {
            code = highSurrogate & 0b1111111111;
        } else {
            throw new JsonException(stream.index - 4, "Invalid code unit sequence: " + Integer.toHexString(highSurrogate));
        }

        // read the low-surrogate unicode sequence
        var c = stream.read();
        if (c == '\\') {
            c = stream.read();
            if (c == 'u') {
                c = readHexCode(stream);
            } else {
                throw new JsonException(stream.index, "Invalid UTF-16 low-surrogate pair");
            }
        } else {
            throw new JsonException(stream.index, "Invalid UTF-16 low-surrogate pair");
        }
        if (0xdc00 <= c && c <= 0xdfff) {
            // surrogate-pair accepted, decode the UTF-16 bytes.
            // See https://tools.ietf.org/html/rfc2781#section-2.2
            var lowSurrogate = c & 0b1111111111;
            code = ((code << 10) | lowSurrogate) + 0x10000;

            // now encode it as UTF-8...
            // UTF8 case:  11110xxx 10xxxxxx 10xxxxxx 10xxxxxx
            buffer.put((byte) ((code >>> 18) | 0b1111_0000));
            buffer.put((byte) (((code >>> 12) & 0b0011_1111) | 0b1000_0000));
            buffer.put((byte) (((code >>> 6) & 0b0011_1111) | 0b1000_0000));
            buffer.put((byte) ((code & 0b0011_1111) | 0b1000_0000));
        } else {
            throw new JsonException(stream.index - 4, "Illegal UTF-16 lower surrogate pair: " + Integer.toHexString(code));
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

    private static int readHexCode(JsonStream stream) throws IOException {
        int code = 0;
        for (var shift = 3; shift >= 0; shift--) {
            var c = stream.read();
            if (c < 0) throw new JsonException(stream.index, "Unterminated unicode sequence");
            code += (hex(c, stream.index) << (4 * shift));
        }
        return code;
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

    private static final double[] powersOf10 = new double[]{
            1e-12, 1e-11, 1e-10, 1e-9, 1e-8, 1e-7,
            1e-6, 1e-5, 1e-4, 1e-3, 1e-2, 1e-1, 1e0,
            1e1, 1e2, 1e3, 1e4, 1e5, 1e6, 1e7, 1e8, 1e9, 1e10, 1e11, 1e12, 1e13, 1e14, 1e15, 1e16,
            1e17, 1e18, 1e19, 1e20, 1e21, 1e22, 1e23, 1e24, 1e25, 1e26, 1e27, 1e28, 1e29, 1e30, 1e31, 1e32
    };

    private static double pow10(int n) {
        if (-13 < n && n < 21) {
            return powersOf10[n + 12];
        }
        return Math.pow(10, n);
    }

    private static final boolean[] startsNumber = new boolean[256];

    static {
        startsNumber['-'] = true;
        startsNumber['0'] = true;
        startsNumber['1'] = true;
        startsNumber['2'] = true;
        startsNumber['3'] = true;
        startsNumber['4'] = true;
        startsNumber['5'] = true;
        startsNumber['6'] = true;
        startsNumber['7'] = true;
        startsNumber['8'] = true;
        startsNumber['9'] = true;
    }

    private static boolean startsNumber(int b) {
        return startsNumber[b];
    }

    private void verifyNoTrailingContent(JsonStream jsonStream) throws IOException {
        if (jsonStream.bt > 0 && isWhitespace(jsonStream.bt)) {
            skipWhitespace(jsonStream);
        }
        if (jsonStream.bt >= 0) {
            throw new JsonException(jsonStream.index, "Illegal trailing content: '" + ((char) jsonStream.bt) + "'");
        }
    }

    private void skipWhitespace(JsonStream stream) throws IOException {
        if (stream.bt < 0) {
            return;
        }
        if (!isWhitespace(stream.bt)) {
            return;
        }
        var index = 1;
        final var maxIndex = config.maxWhitespace();
        int b;
        while ((b = stream.read()) > 0) {
            if (isWhitespace(b)) {
                if (index >= maxIndex) {
                    throw new JsonException(stream.index, "Too much whitespace");
                }
                index++;
            } else {
                break;
            }
        }
    }

    private static final boolean[] WHITESPACE_BYTES = new boolean[256];

    static {
        WHITESPACE_BYTES[' '] = true;
        WHITESPACE_BYTES['\t'] = true;
        WHITESPACE_BYTES['\n'] = true;
        WHITESPACE_BYTES['\r'] = true;
    }

    private static boolean isWhitespace(int c) {
        return WHITESPACE_BYTES[c];
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
