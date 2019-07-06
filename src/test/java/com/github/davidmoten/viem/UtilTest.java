package com.github.davidmoten.viem;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Collections;

import org.junit.Test;

import com.github.davidmoten.junit.Asserts;

public class UtilTest {

    @Test
    public void assertUtilityClass() {
        Asserts.assertIsUtilityClass(Util.class);
    }

    @Test
    public void testGreaterThanEmpty() {
        assertFalse(Util.greaterThan(new SystemImpl(Collections.emptySet()),
                Collections.<String>emptySet(), Collections.singleton("boo")));
    }

    @Test
    public void testGreaterThan() {
        SystemImpl system = SystemImpl.create();
        assertEquals(1, Util.compare(system, "A", "B"));
        assertEquals(-1, Util.compare(system, "B", "A"));
    }

}
