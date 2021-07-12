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
        assertEquals(set(es(2, "A1", "B2")),
                system(es(2, "A1", "B2")).merge(es(1, "A1", "B1")).toSet());
    }

    @Test
    public void testSecondaryIdOnOlderChuckedInMergeArrivalBefore() {
        assertEquals(set(es(2, "A1", "B1")),
                system(es(1, "A1", "B2")).merge(es(2, "A1", "B1")).toSet());
    }

    @Test
    public void testNewIdCarriedThrough() {
        assertEquals(set(es(2, "A1", "B1", "C1")),
                system(es(1, "A1", "B1")).merge(es(2, "A1", "B1", "C1")).toSet());
    }

    @Test
    public void testNewIdCarriedThrough2() {
        assertEquals(set(es(2, "A1", "B1", "C1")),
                system(es(2, "A1", "B1")).merge(es(1, "A1", "B1", "C1")).toSet());
    }

    @Test
    public void testMergeOfSecondaryMatchIsRejected() {
        assertEquals(set(es(0, "A1"), es(1, "A2", "B1")),
                system(es(1, "A2", "B1")).merge(es(0, "A1", "B1")).toSet());
    }

    @Test
    public void testMergeGathersUpMany() {
        assertEquals(set( //
                es(3, "A1", "B1", "C1", "D1", "E1", "F1")),
                system(es(3, "A1", "E1"), es(0, "A1", "D1"), es(1, "B1", "E1"), es(2, "C1", "F1"))
                        .merge(es(2, "A1", "B1", "C1")).toSet());
    }

    // TODO for this test case we need to get the system to reject
    @Test
    public void testMergeWithRejection() {
        EntityState<String, String, Long> es = es(1, "A1", "D1", "F1");
        EntityState<String, String, Long> es1 = es(1, "A1", "B1");
        EntityState<String, String, Long> es2 = es(2, "C1", "D1");
        EntityState<String, String, Long> es3 = es(3, "E1", "F1");
        ViemSystem1 sys = system(es1, es2, es3);
        Set<EntityState<String, String, Long>> pair = new HashSet<>();
        pair.add(es1);
        pair.add(es2);
        sys.mergeable = (a, b) -> {
            System.out.println(sys.es1 + " and " + sys.es2);
            return sys.es1 != es || sys.es2 != es3;
        };
        assertEquals(set( //
                es(2, "A1", "B1", "C1", "D1"), es3), sys.merge(es).toSet());
    }

    private static EntityState<String, String, Long> es(long timestamp, String... strings) {
        Map<String, String> map = Arrays.stream(strings)
                .collect(Collectors.toMap(s -> s.substring(0, 1), s -> s.substring(1, 2)));
        return EntityState.create(map, timestamp);
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
    private static Set<EntityState<String, String, Long>> set(
            EntityState<String, String, Long>... entityStates) {
        return new HashSet<EntityState<String, String, Long>>(Arrays.asList(entityStates));
    }

}
