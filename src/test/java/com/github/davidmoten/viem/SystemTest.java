package com.github.davidmoten.viem;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.github.davidmoten.viem.EntityState;

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
        SystemImpl sys = system(es(1, "A1"));
        sys.mergeable = false;
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

    private EntityStateImpl es(String... s) {
        return EntityStateImpl.create(1, s);
    }

    private EntityStateImpl es(long timestamp, String... s) {
        return EntityStateImpl.create(timestamp, s);
    }

    private SystemImpl system(EntityStateImpl... entityStates) {
        return SystemImpl.create(entityStates);
    }

    @SafeVarargs
    private static Set<EntityState<String, String, Long>> set(EntityState<String, String, Long>... entityStates) {
        return new HashSet<EntityState<String, String, Long>>(Arrays.asList(entityStates));
    }

}
