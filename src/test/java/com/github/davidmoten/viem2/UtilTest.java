package com.github.davidmoten.viem2;

import org.junit.Test;

import com.github.davidmoten.junit.Asserts;

public class UtilTest {
    
    @Test
    public void assertUtilityClass() {
        Asserts.assertIsUtilityClass(Util.class);
    }

}
