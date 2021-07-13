package com.github.davidmoten.viem;

import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

public class AlgorithmTest {

	@Test
	public void test() {
		Set<String> a = new HashSet<>();
		a.add("A");
		a.add("Z");
		Set<String> b = new HashSet<>();
		b.add("B");
		b.add("C");
		assertTrue(Algorithm.greaterThan(new ViemSystem1(new HashSet<>()), a, b));
	}

}
