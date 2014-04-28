package org.unclesniper.ogdl;

import java.util.Set;
import java.util.HashSet;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

public class TestUtils {

	public static <T> void assertSetEquals(String name, T[] values, Iterable<T> collection) {
		Set<T> set = new HashSet<T>();
		for(T value : collection)
			set.add(value);
		assertEquals(name + " set size", values.length, set.size());
		for(T value : values)
			assertTrue(value + " in " + name + " set", set.contains(value));
	}

}
