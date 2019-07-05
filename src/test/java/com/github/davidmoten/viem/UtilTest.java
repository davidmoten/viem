package com.github.davidmoten.viem;

import org.junit.Test;

import com.github.davidmoten.junit.Asserts;
import com.github.davidmoten.viem.Util;

public class UtilTest {
    
    @Test
    public void assertUtilityClass() {
        Asserts.assertIsUtilityClass(Util.class);
    }

}
