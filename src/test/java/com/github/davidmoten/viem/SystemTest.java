package com.github.davidmoten.viem;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Test;

public class SystemTest {

	// Notation
	// A1 is a key value that is parsed as A=1 in terms of key-value

	@Test
	public void testSimple() {
		assertEquals(set(es(2, "A1")), system(es("A1")).merge(es(2, "A1")).toSet());
	}

	@Test
	public void testNoMatch() {
		assertEquals(set(es("A2"), es("A1")), system(es("A1")).merge(es("A2")).toSet());
	}

	@Test
	public void testSimpleNewAfter() {
		assertEquals(set(es(2, "A1")), system(es(1, "A1")).merge(es(2, "A1")).toSet());
	}

	@Test
	public void testSimpleNewAfterNoMerge() {
		ViemSystem1 sys = system(es(1, "A1"));
		sys.mergeable = (a, b) -> false;
		assertEquals(set(es(2, "A1")), sys.merge(es(2, "A1")).toSet());
	}

	@Test
	public void testSimpleNewBefore() {
		assertEquals(set(es(2, "A1")), system(es(2, "A1")).merge(es(1, "A1")).toSet());
	}

	@Test
	public void testSecondaryIdCarriedInMerge() {
		assertEquals(set(es(2, "A1", "B1")), system(es(1, "A1")).merge(es(2, "A1", "B1")).toSet());
	}

	@Test
	public void testSecondaryIdCarriedInMergeTimesSwapped() {
		assertEquals(set(es(2, "A1", "B1")), system(es(2, "A1")).merge(es(1, "A1", "B1")).toSet());
	}

	@Test
	public void testSecondaryIdOnOlderChuckedInMerge() {
		assertEquals(set(es(2, "A1", "B2")), system(es(2, "A1", "B2")).merge(es(1, "A1", "B1")).toSet());
	}

	@Test
	public void testSecondaryIdOnOlderChuckedInMergeArrivalBefore() {
		assertEquals(set(es(2, "A1", "B1")), system(es(1, "A1", "B2")).merge(es(2, "A1", "B1")).toSet());
	}

	@Test
	public void testNewIdCarriedThrough() {
		assertEquals(set(es(2, "A1", "B1", "C1")), system(es(1, "A1", "B1")).merge(es(2, "A1", "B1", "C1")).toSet());
	}

	@Test
	public void testNewIdCarriedThrough2() {
		assertEquals(set(es(2, "A1", "B1", "C1")), system(es(2, "A1", "B1")).merge(es(1, "A1", "B1", "C1")).toSet());
	}

	@Test
	public void testMergeOfSecondaryMatchIsRejected() {
		assertEquals(set(es(0, "A1"), es(1, "A2", "B1")), system(es(1, "A2", "B1")).merge(es(0, "A1", "B1")).toSet());
	}

	@Test
	public void testMergeGathersUpMany() {
		assertEquals(set( //
				es(3, "A1", "B1", "C1", "D1", "E1", "F1")),
				system(es(3, "A1", "E1"), es(0, "A1", "D1"), es(1, "B1", "E1"), es(2, "C1", "F1"))
						.merge(es(2, "A1", "B1", "C1")).toSet());
	}

	@Test
	public void testMergeWithRejection() {
		EntityState<String, String, TimedPoint> es1 = es2(1, 0, "A1", "B1");
		EntityState<String, String, TimedPoint> es2 = es2(2, 2, "C1", "D1");
		EntityState<String, String, TimedPoint> es3 = es2(3, 100, "E1", "F1");
		ViemSystem2 sys = system2(es1, es2, es3);
		EntityState<String, String, TimedPoint> esNew = es2(1, 1, "A1", "D1", "F1");
		Set<EntityState<String, String, TimedPoint>> actual = sys.merge(esNew).toSet();
		assertEquals(set( //
				es2(2, 2, "A1", "B1", "C1", "D1"), es2(3, 6, "E1", "F1")), actual);
	}

	private static EntityState<String, String, Long> es(long timestamp, String... strings) {
		Map<String, String> map = Arrays.stream(strings)
				.collect(Collectors.toMap(s -> s.substring(0, 1), s -> s.substring(1, 2)));
		return EntityState.create(map, timestamp);
	}

	private static EntityState<String, String, TimedPoint> es2(long timestamp, int position, String... strings) {
		Map<String, String> map = Arrays.stream(strings)
				.collect(Collectors.toMap(s -> s.substring(0, 1), s -> s.substring(1, 2)));
		return EntityState.create(map, new TimedPoint(timestamp, position));
	}

	private static EntityState<String, String, Long> es(String... strings) {
		return es(0, strings);
	}

	@SafeVarargs
	private static ViemSystem1 system(EntityState<String, String, Long>... entityStates) {
		ViemSystem<String, String, Long> s = ViemSystem1.create();
		for (EntityState<String, String, Long> e : entityStates) {
			s = s.merge(e);
		}
		return (ViemSystem1) s;
	}

	@SafeVarargs
	private static ViemSystem2 system2(EntityState<String, String, TimedPoint>... entityStates) {
		ViemSystem<String, String, TimedPoint> s = ViemSystem2.create();
		for (EntityState<String, String, TimedPoint> e : entityStates) {
			s = s.merge(e);
		}
		return (ViemSystem2) s;
	}

	@SafeVarargs
	private static <T> Set<EntityState<String, String, T>> set(EntityState<String, String, T>... entityStates) {
		return new HashSet<EntityState<String, String, T>>(Arrays.asList(entityStates));
	}

}
