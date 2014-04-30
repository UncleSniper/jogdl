package org.unclesniper.ogdl;

import org.junit.Test;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertEquals;

public class CyclicConstantDependencyExceptionTests {

	@Test
	public void binaryCtorWithLocation() {
		Location loc = new SimpleLocation("foo", 42);
		CyclicConstantDependencyException ex = new CyclicConstantDependencyException("bar", loc);
		assertEquals("constant name", "bar", ex.getConstantName());
		assertSame("location", loc, ex.getLocation());
		assertEquals("message", "Constant 'bar' is ultimately defined as itsself at foo:42", ex.getMessage());
		assertNull("no cause", ex.getCause());
	}

	@Test
	public void binaryCtorWithoutLocation() {
		CyclicConstantDependencyException ex = new CyclicConstantDependencyException("bar", null);
		assertEquals("constant name", "bar", ex.getConstantName());
		assertNull("no location", ex.getLocation());
		assertEquals("message", "Constant 'bar' is ultimately defined as itsself", ex.getMessage());
		assertNull("no cause", ex.getCause());
	}

	@Test
	public void ternaryCtorWithLocationAndCause() {
		Exception cause = new Exception("reason");
		Location loc = new SimpleLocation("foo", 42);
		CyclicConstantDependencyException ex = new CyclicConstantDependencyException("bar", loc, cause);
		assertEquals("constant name", "bar", ex.getConstantName());
		assertSame("location", loc, ex.getLocation());
		assertEquals("message", "Constant 'bar' is ultimately defined as itsself at foo:42: reason",
				ex.getMessage());
		assertSame("cause", cause, ex.getCause());
	}

	@Test
	public void ternaryCtorWithLocationButWithoutCause() {
		Location loc = new SimpleLocation("foo", 42);
		CyclicConstantDependencyException ex = new CyclicConstantDependencyException("bar", loc, null);
		assertEquals("constant name", "bar", ex.getConstantName());
		assertSame("location", loc, ex.getLocation());
		assertEquals("message", "Constant 'bar' is ultimately defined as itsself at foo:42", ex.getMessage());
		assertNull("no cause", ex.getCause());
	}

	@Test
	public void ternaryCtorWithoutLocationButWithCause() {
		Exception cause = new Exception("reason");
		CyclicConstantDependencyException ex = new CyclicConstantDependencyException("bar", null, cause);
		assertEquals("constant name", "bar", ex.getConstantName());
		assertNull("no location", ex.getLocation());
		assertEquals("message", "Constant 'bar' is ultimately defined as itsself: reason", ex.getMessage());
		assertSame("cause", cause, ex.getCause());
	}

	@Test
	public void ternaryCtorWithoutLocationAndCause() {
		CyclicConstantDependencyException ex = new CyclicConstantDependencyException("bar", null, null);
		assertEquals("constant name", "bar", ex.getConstantName());
		assertNull("no location", ex.getLocation());
		assertEquals("message", "Constant 'bar' is ultimately defined as itsself", ex.getMessage());
		assertNull("no cause", ex.getCause());
	}

}
