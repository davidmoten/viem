package com.github.davidmoten.viem;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class PairTest {
    
    @Test
    public void testToString() {
        assertEquals("Pair [a=1, b=2]", new Pair<Integer>(1,2).toString());
    }

}
