package org.unclesniper.ogdl;

import org.junit.Test;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ClassRegistryTests {

	private static class Empty {}

	@Test
	public void forClass() {
		ClassRegistry reg = new ClassRegistry();
		ClassInfo first = reg.forClass(Empty.class);
		assertNotNull("created class info", first);
		assertEquals("class info subject class", Empty.class, first.getSubject());
		ClassInfo second = reg.forClass(Empty.class);
		assertSame("repeated class info", first, second);
	}

	@Test
	public void forNullClass() {
		ClassRegistry reg = new ClassRegistry();
		assertNull("class info", reg.forClass(null));
	}

}
