package com.github.davidmoten.viem;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A ViemSystem for testing purposes only. Doesn't scale well because O(N)
 * lookups are used. A proper implementation would use O(1) lookups.
 *
 */
class ViemSystem2 implements ViemSystem<String, String, TimedPoint> {

	private final Set<EntityState<String, String, TimedPoint>> set;

	public ViemSystem2(Set<EntityState<String, String, TimedPoint>> set) {
		this.set = set;
	}

	@SafeVarargs
	static ViemSystem2 create(EntityState<String, String, TimedPoint>... states) {
		Set<EntityState<String, String, TimedPoint>> set = new HashSet<>();
		for (EntityState<String, String, TimedPoint> state : states) {
			set.add(state);
		}
		return new ViemSystem2(set);
	}

	@Override
	public Set<EntityState<String, String, TimedPoint>> matches(Map<String, String> identifiers) {
		return set.stream() //
				.filter(e -> e.identifiers() //
						.entrySet() //
						.stream() //
						.filter(entry -> entry.getValue().equals(identifiers.get(entry.getKey()))) //
						.findFirst().isPresent())
				.collect(Collectors.toSet());
	}

	@Override
	public boolean keyGreaterThan(String a, String b) {
		return a.compareTo(b) < 0; // A > B
	}

	@Override
	public boolean metadataGreaterThan(TimedPoint a, TimedPoint b) {
		return a.time > b.time;
	}

	@Override
	public boolean mergeable(TimedPoint a, TimedPoint b) {
		return Math.abs((b.position - a.position) / Math.max(1, b.time - a.time)) <= 2;
	}

	@Override
	public TimedPoint merge(TimedPoint a, TimedPoint b) {
		if (a.time >= b.time) {
			return a;
		} else {
			return b;
		}
	}

	@Override
	public Iterable<EntityState<String, String, TimedPoint>> entityStates() {
		return set;
	}

	@Override
	public ViemSystem<String, String, TimedPoint> update(List<EntityState<String, String, TimedPoint>> matches,
			Set<EntityState<String, String, TimedPoint>> newEntityStates) {
		set.removeAll(matches);
		set.addAll(newEntityStates);
		return this;
	}

}
