package com.github.davidmoten.viem;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;

import org.junit.Test;

public class EntityStateDefaultTest {

    @Test
    public void testEquals() {
        EntityState<String, String, Integer> e = create(1);
        assertTrue(e.equals(e));
        assertFalse(e.equals(null));
        assertFalse(e.equals(123));
        assertTrue(create(null).equals(create(null)));
        assertFalse(create(null).equals(create(1)));
        assertFalse(create(1).equals(create(2)));
    }

    @Test
    public void testHashCode() {
        assertEquals(962, create(1).hashCode());
        assertEquals(961, create(null).hashCode());
    }

    @Test(expected = NullPointerException.class)
    public void testCannotPassNullIdentifiers() {
        EntityState.create(null, 1);
    }

    @Test
    public void testToString() {
        assertEquals("EntityStateDefault [ids={}, metadata=1]", create(1).toString());
    }

    private EntityState<String, String, Integer> create(Integer n) {
        return EntityState.create(Collections.emptyMap(), n);
    }

}
