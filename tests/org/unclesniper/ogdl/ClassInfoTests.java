package org.unclesniper.ogdl;

import java.util.Set;
import org.junit.Test;
import java.lang.reflect.Method;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ClassInfoTests {

	private static class Empty {}

	private static class VariousAccessors {

		// yes: setter
		public void setProp0(String foo) {}

		// no: static
		public static void setProp1(String foo) {}

		// no: nilary
		public void setProp2() {}

		// no: ternary
		public void setProp3(String a, String b, String c) {}

		// no: package visibility
		void setProp4(String foo) {}

		// yes: setter (primitive)
		public void setProp5(int bar) {}

		// yes: putter
		public void setProp6(int index, String value) {}

		// yes: adder
		public void addProp7(String foo) {}

		// no: nilary
		public void addProp8() {}

		// no: ternary
		public void addProp9(String a, String b, String c) {}

		// yes: putter
		public void putProp10(String key, String value) {}

		// no: nilary
		public void putProp11() {}

		// no: ternary
		public void putProp12(String a, String b, String c) {}

		static final Method m_setProp0;
		static final Method m_setProp5;
		static final Method m_setProp6;
		static final Method m_addProp7;
		static final Method m_putProp10;

		static {
			try {
				m_setProp0 = VariousAccessors.class.getDeclaredMethod("setProp0", String.class);
				m_setProp5 = VariousAccessors.class.getDeclaredMethod("setProp5", Integer.TYPE);
				m_setProp6 = VariousAccessors.class.getDeclaredMethod("setProp6", Integer.TYPE, String.class);
				m_addProp7 = VariousAccessors.class.getDeclaredMethod("addProp7", String.class);
				m_putProp10 = VariousAccessors.class.getDeclaredMethod("putProp10", String.class, String.class);
			}
			catch(NoSuchMethodException nsme) {
				throw new Error("Cannot retrieve methods: " + nsme.getMessage(), nsme);
			}
		}

	}

	@Test
	public void ctor() {
		ClassInfo info = new ClassInfo(Empty.class);
		assertSame("subject class", Empty.class, info.getSubject());
	}

	@Test
	public void emptyClass() {
		ClassInfo info = new ClassInfo(Empty.class);
		TestUtils.assertSetEquals("property name", new String[0], info.getPropertyNames());
	}

	@Test
	public void variousAccessors() {
		ClassInfo info = new ClassInfo(VariousAccessors.class);
		TestUtils.assertSetEquals("property name", new String[] {
			"prop0", "prop5", "prop6", "prop7", "prop10",
		}, info.getPropertyNames());
		Property prop;
		Set<Accessor> accs;
		Accessor acc;
		// prop0
		prop = info.getProperty("prop0");
		assertNotNull("prop0", prop);
		accs = TestUtils.toSet(prop.getSetters());
		assertEquals("prop0 setter set size", 1, accs.size());
		assertEquals("prop0 adder set size", 0, TestUtils.toSet(prop.getAdders()).size());
		assertEquals("prop0 putter set size", 0, TestUtils.toSet(prop.getPutters()).size());
		acc = accs.iterator().next();
		assertNotNull("prop0 setter present", acc);
		assertEquals("prop0 setter method", VariousAccessors.m_setProp0, acc.getMethod());
		// prop5
		prop = info.getProperty("prop5");
		assertNotNull("prop5", prop);
		accs = TestUtils.toSet(prop.getSetters());
		assertEquals("prop5 setter set size", 1, accs.size());
		assertEquals("prop5 adder set size", 0, TestUtils.toSet(prop.getAdders()).size());
		assertEquals("prop5 putter set size", 0, TestUtils.toSet(prop.getPutters()).size());
		acc = accs.iterator().next();
		assertNotNull("prop5 setter present", acc);
		assertEquals("prop5 setter method", VariousAccessors.m_setProp5, acc.getMethod());
		// prop6
		prop = info.getProperty("prop6");
		assertNotNull("prop6", prop);
		assertEquals("prop6 setter set size", 0, TestUtils.toSet(prop.getSetters()).size());
		assertEquals("prop6 adder set size", 0, TestUtils.toSet(prop.getAdders()).size());
		accs = TestUtils.toSet(prop.getPutters());
		assertEquals("prop6 putter set size", 1, accs.size());
		acc = accs.iterator().next();
		assertNotNull("prop6 putter present", acc);
		assertEquals("prop6 putter method", VariousAccessors.m_setProp6, acc.getMethod());
		// prop7
		prop = info.getProperty("prop7");
		assertNotNull("prop7", prop);
		assertEquals("prop7 setter set size", 0, TestUtils.toSet(prop.getSetters()).size());
		accs = TestUtils.toSet(prop.getAdders());
		assertEquals("prop7 adder set size", 1, accs.size());
		assertEquals("prop7 putter set size", 0, TestUtils.toSet(prop.getPutters()).size());
		acc = accs.iterator().next();
		assertNotNull("prop7 adder present", acc);
		assertEquals("prop7 adder method", VariousAccessors.m_addProp7, acc.getMethod());
		// prop10
		prop = info.getProperty("prop10");
		assertNotNull("prop10", prop);
		assertEquals("prop10 setter set size", 0, TestUtils.toSet(prop.getSetters()).size());
		assertEquals("prop10 adder set size", 0, TestUtils.toSet(prop.getAdders()).size());
		accs = TestUtils.toSet(prop.getPutters());
		assertEquals("prop10 putter set size", 1, accs.size());
		acc = accs.iterator().next();
		assertNotNull("prop10 putter present", acc);
		assertEquals("prop10 putter method", VariousAccessors.m_putProp10, acc.getMethod());
	}

}
