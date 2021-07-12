package com.github.davidmoten.viem;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Collections;

import org.junit.Test;

import com.github.davidmoten.junit.Asserts;

public class UtilTest {

    @Test
    public void assertUtilityClass() {
        Asserts.assertIsUtilityClass(Algorithm.class);
    }

    @Test
    public void testGreaterThanEmpty() {
        assertFalse(Algorithm.greaterThan(new ViemSystem1(Collections.emptySet()),
                Collections.<String>emptySet(), Collections.singleton("boo")));
    }

    @Test
    public void testGreaterThan() {
        ViemSystem1 system = ViemSystem1.create();
        assertEquals(1, Algorithm.compare(system, "A", "B"));
        assertEquals(-1, Algorithm.compare(system, "B", "A"));
    }

}
