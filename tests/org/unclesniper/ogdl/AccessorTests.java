package org.unclesniper.ogdl;

import org.junit.Test;
import java.lang.reflect.Method;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertNull;

public class AccessorTests {

	private static final Method someMethod;

	static {
		try {
			someMethod = AccessorTests.class.getDeclaredMethod("unaryAccessorCtor");
		}
		catch(NoSuchMethodException nsme) {
			throw new Error("Cannot retrieve method: " + nsme.getMessage(), nsme);
		}
	}

	@Test
	public void unaryAccessorCtor() {
		Property prop = new Property(null, null);
		Accessor a = new Accessor(prop, AccessorTests.someMethod, Integer.class);
		assertSame("property", prop, a.getProperty());
		assertSame("method", AccessorTests.someMethod, a.getMethod());
		assertNull("key type", a.getKeyType());
		assertSame("value type", Integer.class, a.getValueType());
	}

	@Test
	public void unaryAccessorCtorGivenNull() {
		Accessor a = new Accessor(null, null, null);
		assertNull("property", a.getProperty());
		assertNull("method", a.getMethod());
		assertNull("key type", a.getKeyType());
		assertNull("value type", a.getValueType());
	}

	@Test
	public void binaryAccessorCtor() {
		Property prop = new Property(null, null);
		Accessor a = new Accessor(prop, AccessorTests.someMethod, Float.class, Double.class);
		assertSame("property", prop, a.getProperty());
		assertSame("method", AccessorTests.someMethod, a.getMethod());
		assertSame("key type", Float.class, a.getKeyType());
		assertSame("value type", Double.class, a.getValueType());
	}

	@Test
	public void binaryAccessorCtorGivenNull() {
		Accessor a = new Accessor(null, null, null, null);
		assertNull("property", a.getProperty());
		assertNull("method", a.getMethod());
		assertNull("key type", a.getKeyType());
		assertNull("value type", a.getValueType());
	}

}
