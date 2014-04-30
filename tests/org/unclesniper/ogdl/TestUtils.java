package org.unclesniper.ogdl;

import java.util.Set;
import java.util.List;
import java.util.HashSet;
import java.util.LinkedList;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

public class TestUtils {

	public static <T> Set<T> toSet(Iterable<T> collection) {
		Set<T> set = new HashSet<T>();
		for(T value : collection)
			set.add(value);
		return set;
	}

	public static <T> List<T> toList(Iterable<T> collection) {
		List<T> list = new LinkedList<T>();
		for(T value : collection)
			list.add(value);
		return list;
	}

	public static <T> void assertSetEquals(String name, T[] values, Iterable<T> collection) {
		Set<T> set = TestUtils.toSet(collection);
		assertEquals(name + " set size", values.length, set.size());
		for(T value : values)
			assertTrue(value + " in " + name + " set", set.contains(value));
	}

	public static <T> void assertListEquals(String name, T[] values, Iterable<T> collection) {
		List<T> list = TestUtils.toList(collection);
		assertEquals(name + " sequence length", values.length, list.size());
		for(int i = 0; i < values.length; ++i)
			assertEquals(name + " sequence element", values[i], list.get(i));
	}

}
