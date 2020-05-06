package com.athaydes.json;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONNumberTest implements TestHelper {

    final JSON json = new JSON();

    @Test
    public void canParseInts() {
        assertEquals(0, json.parse("0", Number.class));
        assertEquals(1, json.parse("1", Number.class));
        assertEquals(10, json.parse("10", Number.class));
        assertEquals(99, json.parse("99", Number.class));
        assertEquals(123456789, json.parse("123456789", Number.class));
        assertEquals(1_000_000_000, json.parse("1000000000", Number.class));
        assertEquals(4_000_000_000L, json.parse("4000000000", Number.class));

        assertEquals(Long.MAX_VALUE - 50, json.parse("" + (Long.MAX_VALUE - 50), Number.class));
        assertEquals(Long.MAX_VALUE - 4, json.parse("" + (Long.MAX_VALUE - 4), Number.class));
        assertEquals(Long.MAX_VALUE - 3, json.parse("" + (Long.MAX_VALUE - 3), Number.class));
        assertEquals(Long.MAX_VALUE - 2, json.parse("" + (Long.MAX_VALUE - 2), Number.class));
        assertEquals(Long.MAX_VALUE - 1, json.parse("" + (Long.MAX_VALUE - 1), Number.class));
        assertEquals(Long.MAX_VALUE, json.parse("" + Long.MAX_VALUE, Number.class));

        assertEquals(0, json.parse("0e0", Number.class));
        assertEquals(0D, json.parse("0e+1", Number.class));
        assertEquals(0D, json.parse("0e-1", Number.class));
        assertEquals(123456789e1, json.parse("123456789e1", Number.class));
        assertEquals(1234e6, json.parse("1234e6", Number.class));
        assertEquals(123456789e18, json.parse("123456789e18", Number.class));
        assertEquals(99E99, json.parse("99E99", Number.class));
        assertEquals(99E-99, json.parse("99E-99", Number.class));

        // we only get around 15 digits of precision! Can we improve that?
        assertEquals(5309423e238D, json.parse("5309423e238", Number.class).doubleValue(), 1e230);
    }

    @Test
    public void canParseNegativeInts() {
        assertEquals(-0, json.parse("-0", Number.class));
        assertEquals(-1, json.parse("-1", Number.class));
        assertEquals(-10, json.parse("-10", Number.class));
        assertEquals(-99, json.parse("-99", Number.class));
        assertEquals(-123456789, json.parse("-123456789", Number.class));
        assertEquals(-1_000_000_000, json.parse("-1000000000", Number.class));

        // FIXME
//        assertEquals(Long.MIN_VALUE + 10, json.parse("" + Long.MIN_VALUE + 10, Number.class));

        assertEquals(-0, json.parse("-0e0", Number.class));
        assertEquals(0.0, json.parse("-0e+1", Number.class));
        assertEquals(0.0, json.parse("-0e-1", Number.class));
        assertEquals(-123456789e1, json.parse("-123456789e1", Number.class));
        assertEquals(-1234e6, json.parse("-1234e6", Number.class));
        assertEquals(-123456789e18, json.parse("-123456789e18", Number.class));
        assertEquals(-99E99, json.parse("-99E99", Number.class));
        assertEquals(-99E-99, json.parse("-99E-99", Number.class));
        assertEquals(-5309423e238D, json.parse("-5309423e238", Number.class).doubleValue(), 1e234);
    }

    @Test
    public void canParseDoubles() {
        assertEquals(0, json.parse("0.0", Number.class));
        assertEquals(10, json.parse("10.0", Number.class));
        assertEquals(1.1D, json.parse("1.1", Number.class));
        assertEquals(10.5623D, json.parse("10.5623", Number.class));
        assertEquals(99.99999999999D, json.parse("99.99999999999", Number.class));
        assertEquals(123456789.123456789D, json.parse("123456789.123456789", Number.class));

        assertEquals(0, json.parse("0.0e0", Number.class));
        assertEquals(100.0, json.parse("10.0e1", Number.class));
        assertEquals(1.1e5D, json.parse("1.1e5", Number.class));
        assertEquals(10.5623e+57D, json.parse("10.5623e+57", Number.class).doubleValue(), 1e+45);
        assertEquals(99.99999999999E53D, json.parse("99.99999999999E53", Number.class));
        assertEquals(123456789.123456789e-38D, json.parse("123456789.123456789e-38", Number.class));
    }

    @Test
    public void canParseNegativeDoubles() {
        assertEquals(-0, json.parse("-0.0", Number.class));
        assertEquals(-10, json.parse("-10.0", Number.class));
        assertEquals(-1.1D, json.parse("-1.1", Number.class));
        assertEquals(-10.5623D, json.parse("-10.5623", Number.class));
        assertEquals(-99.99999999999D, json.parse("-99.99999999999", Number.class));
        assertEquals(-123456789.123456789D, json.parse("-123456789.123456789", Number.class));

        assertEquals(-0, json.parse("-0.0e0", Number.class));
        assertEquals(-100.0, json.parse("-10.0e1", Number.class));
        assertEquals(-1.1e5D, json.parse("-1.1e5", Number.class));
        assertEquals(-10.5623e+57D, json.parse("-10.5623e+57", Number.class).doubleValue(), 1e+45);
        assertEquals(-99.99999999999E53D, json.parse("-99.99999999999E53", Number.class));
        assertEquals(-123456789.123456789e-38D, json.parse("-123456789.123456789e-38", Number.class));
    }

}
