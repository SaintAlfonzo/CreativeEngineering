// Copyright 2026 SaintAlfonzo. GPLv3. Minimal stand-in for the JUnit 5 Assertions the upstream Power Grid solver tests
// use (tools/test_power_grid_solver.sh). Failures throw AssertionError with JUnit's message shape.
package org.junit.jupiter.api;

import java.util.Objects;

public final class Assertions {
    private Assertions() {}

    private static void fail(String message, Object expected, Object actual) {
        throw new AssertionError((message == null ? "" : message + " ==> ") + "expected: <" + expected + "> but was: <" + actual + ">");
    }

    public static void assertEquals(double expected, double actual, double delta, String message) {
        if(Double.isNaN(actual) || Math.abs(expected - actual) > delta)
            fail(message + " (delta " + delta + ")", expected, actual);
    }

    public static void assertEquals(double expected, double actual, double delta) {
        assertEquals(expected, actual, delta, null);
    }

    public static void assertEquals(double expected, double actual, String message) {
        if(Double.compare(expected, actual) != 0)
            fail(message, expected, actual);
    }

    public static void assertEquals(double expected, double actual) {
        assertEquals(expected, actual, (String) null);
    }

    // JUnit compares float overloads in float precision.
    public static void assertEquals(float expected, float actual, float delta, String message) {
        if(Float.isNaN(actual) || Math.abs(expected - actual) > delta)
            fail(message + " (delta " + delta + ")", expected, actual);
    }

    public static void assertEquals(float expected, float actual, float delta) {
        assertEquals(expected, actual, delta, null);
    }

    public static void assertEquals(float expected, float actual, String message) {
        if(Float.compare(expected, actual) != 0)
            fail(message, expected, actual);
    }

    public static void assertEquals(float expected, float actual) {
        assertEquals(expected, actual, (String) null);
    }

    public static void assertEquals(long expected, long actual, String message) {
        if(expected != actual)
            fail(message, expected, actual);
    }

    public static void assertEquals(long expected, long actual) {
        assertEquals(expected, actual, (String) null);
    }

    public static void assertEquals(Object expected, Object actual, String message) {
        if(!Objects.equals(expected, actual))
            fail(message, expected, actual);
    }

    public static void assertEquals(Object expected, Object actual) {
        assertEquals(expected, actual, null);
    }

    public static void assertTrue(boolean condition, String message) {
        if(!condition)
            throw new AssertionError(message == null ? "expected: <true>" : message);
    }

    public static void assertTrue(boolean condition) {
        assertTrue(condition, null);
    }

    public static void assertFalse(boolean condition, String message) {
        if(condition)
            throw new AssertionError(message == null ? "expected: <false>" : message);
    }

    public static void assertFalse(boolean condition) {
        assertFalse(condition, null);
    }

    public static void assertNotNull(Object value, String message) {
        if(value == null)
            throw new AssertionError(message == null ? "expected: not <null>" : message);
    }

    public static void assertNotNull(Object value) {
        assertNotNull(value, null);
    }
}
