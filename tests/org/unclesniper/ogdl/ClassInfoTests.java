package org.unclesniper.ogdl;

import java.util.Set;
import org.junit.Test;
import java.util.Iterator;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import static org.junit.Assert.assertNull;
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

	private static class ClashingAccessors {

		public void setProp(String foo) {}

		public void setProp(int bar) {}

		public void addProp(String foo) {}

		public void putProp(String key, String value) {}

		public void setOther(String baz) {}

		static final Method m_setProp_String;
		static final Method m_setProp_int;
		static final Method m_addProp;
		static final Method m_putProp;
		static final Method m_setOther;

		static {
			try {
				m_setProp_String = ClashingAccessors.class.getDeclaredMethod("setProp", String.class);
				m_setProp_int = ClashingAccessors.class.getDeclaredMethod("setProp", Integer.TYPE);
				m_addProp = ClashingAccessors.class.getDeclaredMethod("addProp", String.class);
				m_putProp = ClashingAccessors.class.getDeclaredMethod("putProp", String.class, String.class);
				m_setOther = ClashingAccessors.class.getDeclaredMethod("setOther", String.class);
			}
			catch(NoSuchMethodException nsme) {
				throw new Error("Cannot retrieve methods: " + nsme.getMessage(), nsme);
			}
		}

	}

	private static class Super {}
	private static class Sub extends Super {}

	private static class VariousConstructors {

		public VariousConstructors() {}

		public VariousConstructors(String s) {}

		public VariousConstructors(Super s) {}

		public VariousConstructors(Sub s) {}

		public VariousConstructors(Super sup, Sub sub) {}

		public VariousConstructors(Sub sub, Super sup) {}

		static final Constructor<VariousConstructors> c_nil;
		static final Constructor<VariousConstructors> c_String;
		static final Constructor<VariousConstructors> c_Super;
		static final Constructor<VariousConstructors> c_Sub;
		static final Constructor<VariousConstructors> c_Super_Sub;
		static final Constructor<VariousConstructors> c_Sub_Super;

		static {
			try {
				c_nil = VariousConstructors.class.getConstructor();
				c_String = VariousConstructors.class.getConstructor(String.class);
				c_Super = VariousConstructors.class.getConstructor(Super.class);
				c_Sub = VariousConstructors.class.getConstructor(Sub.class);
				c_Super_Sub = VariousConstructors.class.getConstructor(Super.class, Sub.class);
				c_Sub_Super = VariousConstructors.class.getConstructor(Sub.class, Super.class);
			}
			catch(NoSuchMethodException nsme) {
				throw new Error("Cannot retrieve constructors: " + nsme.getMessage(), nsme);
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
		assertEquals("subject class", Empty.class, info.getSubject());
		TestUtils.assertSetEquals("property name", new String[0], info.getPropertyNames());
	}

	@Test
	public void variousAccessors() {
		ClassInfo info = new ClassInfo(VariousAccessors.class);
		assertEquals("subject class", VariousAccessors.class, info.getSubject());
		TestUtils.assertSetEquals("property name", new String[] {
			"prop0", "prop5", "prop6", "prop7", "prop10",
		}, info.getPropertyNames());
		Property prop;
		Set<Accessor> accs;
		Accessor acc;
		// prop0
		prop = info.getProperty("prop0");
		assertNotNull("prop0", prop);
		assertEquals("prop0 name", "prop0", prop.getName());
		assertSame("prop0 owning class", info, prop.getOwningClass());
		accs = TestUtils.toSet(prop.getSetters());
		assertEquals("prop0 setter set size", 1, accs.size());
		assertEquals("prop0 adder set size", 0, TestUtils.toSet(prop.getAdders()).size());
		assertEquals("prop0 putter set size", 0, TestUtils.toSet(prop.getPutters()).size());
		acc = accs.iterator().next();
		assertNotNull("prop0 setter present", acc);
		assertSame("prop0 setter owning property", prop, acc.getProperty());
		assertEquals("prop0 setter method", VariousAccessors.m_setProp0, acc.getMethod());
		assertNull("prop0 key type", acc.getKeyType());
		assertEquals("prop0 value type", String.class, acc.getValueType());
		// prop5
		prop = info.getProperty("prop5");
		assertNotNull("prop5", prop);
		assertEquals("prop5 name", "prop5", prop.getName());
		assertSame("prop5 owning class", info, prop.getOwningClass());
		accs = TestUtils.toSet(prop.getSetters());
		assertEquals("prop5 setter set size", 1, accs.size());
		assertEquals("prop5 adder set size", 0, TestUtils.toSet(prop.getAdders()).size());
		assertEquals("prop5 putter set size", 0, TestUtils.toSet(prop.getPutters()).size());
		acc = accs.iterator().next();
		assertNotNull("prop5 setter present", acc);
		assertSame("prop5 setter owning property", prop, acc.getProperty());
		assertEquals("prop5 setter method", VariousAccessors.m_setProp5, acc.getMethod());
		assertNull("prop5 key type", acc.getKeyType());
		assertEquals("prop5 value type", Integer.TYPE, acc.getValueType());
		// prop6
		prop = info.getProperty("prop6");
		assertNotNull("prop6", prop);
		assertEquals("prop6 name", "prop6", prop.getName());
		assertSame("prop6 owning class", info, prop.getOwningClass());
		assertEquals("prop6 setter set size", 0, TestUtils.toSet(prop.getSetters()).size());
		assertEquals("prop6 adder set size", 0, TestUtils.toSet(prop.getAdders()).size());
		accs = TestUtils.toSet(prop.getPutters());
		assertEquals("prop6 putter set size", 1, accs.size());
		acc = accs.iterator().next();
		assertNotNull("prop6 putter present", acc);
		assertSame("prop6 putter owning property", prop, acc.getProperty());
		assertEquals("prop6 putter method", VariousAccessors.m_setProp6, acc.getMethod());
		assertEquals("prop6 key type", Integer.TYPE, acc.getKeyType());
		assertEquals("prop6 value type", String.class, acc.getValueType());
		// prop7
		prop = info.getProperty("prop7");
		assertNotNull("prop7", prop);
		assertEquals("prop7 name", "prop7", prop.getName());
		assertSame("prop7 owning class", info, prop.getOwningClass());
		assertEquals("prop7 setter set size", 0, TestUtils.toSet(prop.getSetters()).size());
		accs = TestUtils.toSet(prop.getAdders());
		assertEquals("prop7 adder set size", 1, accs.size());
		assertEquals("prop7 putter set size", 0, TestUtils.toSet(prop.getPutters()).size());
		acc = accs.iterator().next();
		assertNotNull("prop7 adder present", acc);
		assertSame("prop7 adder owning property", prop, acc.getProperty());
		assertEquals("prop7 adder method", VariousAccessors.m_addProp7, acc.getMethod());
		assertNull("prop7 key type", acc.getKeyType());
		assertEquals("prop7 value type", String.class, acc.getValueType());
		// prop10
		prop = info.getProperty("prop10");
		assertNotNull("prop10", prop);
		assertEquals("prop10 name", "prop10", prop.getName());
		assertSame("prop10 owning class", info, prop.getOwningClass());
		assertEquals("prop10 setter set size", 0, TestUtils.toSet(prop.getSetters()).size());
		assertEquals("prop10 adder set size", 0, TestUtils.toSet(prop.getAdders()).size());
		accs = TestUtils.toSet(prop.getPutters());
		assertEquals("prop10 putter set size", 1, accs.size());
		acc = accs.iterator().next();
		assertNotNull("prop10 putter present", acc);
		assertSame("prop10 putter owning property", prop, acc.getProperty());
		assertEquals("prop10 putter method", VariousAccessors.m_putProp10, acc.getMethod());
		assertEquals("prop10 key type", String.class, acc.getKeyType());
		assertEquals("prop10 value type", String.class, acc.getValueType());
	}

	@Test
	public void clashingAccessors() {
		ClassInfo info = new ClassInfo(ClashingAccessors.class);
		assertEquals("subject class", ClashingAccessors.class, info.getSubject());
		TestUtils.assertSetEquals("property name", new String[] {
			"prop", "other",
		}, info.getPropertyNames());
		Property prop;
		Set<Accessor> accs;
		Accessor acc, acc2;
		Iterator<Accessor> iter;
		// prop
		prop = info.getProperty("prop");
		assertNotNull("prop", prop);
		assertEquals("prop name", "prop", prop.getName());
		assertSame("prop owning class", info, prop.getOwningClass());
		// setters
		accs = TestUtils.toSet(prop.getSetters());
		assertEquals("prop setter set size", 2, accs.size());
		iter = accs.iterator();
		acc = iter.next();
		assertNotNull("prop setter present", acc);
		acc2 = iter.next();
		assertNotNull("prop setter present", acc2);
		if(ClashingAccessors.m_setProp_int.equals(acc.getMethod())) {
			// acc = setProp(int), acc2 = setProp(String)
			Accessor tmp = acc;
			acc = acc2;
			acc2 = tmp;
		}
		assertSame("prop setter owning property", prop, acc.getProperty());
		assertEquals("prop setter method", ClashingAccessors.m_setProp_String, acc.getMethod());
		assertNull("prop key type", acc.getKeyType());
		assertEquals("prop value type", String.class, acc.getValueType());
		assertSame("prop setter owning property", prop, acc2.getProperty());
		assertEquals("prop setter method", ClashingAccessors.m_setProp_int, acc2.getMethod());
		assertNull("prop key type", acc2.getKeyType());
		assertEquals("prop value type", Integer.TYPE, acc2.getValueType());
		// adder
		accs = TestUtils.toSet(prop.getAdders());
		assertEquals("prop adder set size", 1, accs.size());
		acc = accs.iterator().next();
		assertNotNull("prop adder present", acc);
		assertSame("prop adder owning property", prop, acc.getProperty());
		assertEquals("prop adder method", ClashingAccessors.m_addProp, acc.getMethod());
		assertNull("prop key type", acc.getKeyType());
		assertEquals("prop value type", String.class, acc.getValueType());
		// putter
		accs = TestUtils.toSet(prop.getPutters());
		assertEquals("prop putter set size", 1, accs.size());
		acc = accs.iterator().next();
		assertNotNull("prop putter present", acc);
		assertSame("prop putter owning property", prop, acc.getProperty());
		assertEquals("prop putter method", ClashingAccessors.m_putProp, acc.getMethod());
		assertEquals("prop key type", String.class, acc.getKeyType());
		assertEquals("prop value type", String.class, acc.getValueType());
		// other
		prop = info.getProperty("other");
		assertNotNull("other", prop);
		assertEquals("other name", "other", prop.getName());
		assertSame("other owning class", info, prop.getOwningClass());
		accs = TestUtils.toSet(prop.getSetters());
		assertEquals("other setter set size", 1, accs.size());
		assertEquals("other adder set size", 0, TestUtils.toSet(prop.getAdders()).size());
		assertEquals("other putter set size", 0, TestUtils.toSet(prop.getPutters()).size());
		acc = accs.iterator().next();
		assertNotNull("other setter present", acc);
		assertSame("other setter owning property", prop, acc.getProperty());
		assertEquals("other setter method", ClashingAccessors.m_setOther, acc.getMethod());
		assertNull("other key type", acc.getKeyType());
		assertEquals("other value type", String.class, acc.getValueType());
	}

	@Test
	public void forNullClass() {
		ClassInfo info = new ClassInfo(null);
		assertNull("subject class", info.getSubject());
		TestUtils.assertSetEquals("property name", new String[0], info.getPropertyNames());
	}

	@Test
	public void getNonexistentProperty() {
		ClassInfo info = new ClassInfo(VariousAccessors.class);
		assertEquals("subject class", VariousAccessors.class, info.getSubject());
		assertNull("property", info.getProperty("nonexistent"));
	}

	@Test
	public void getNullProperty() {
		ClassInfo info = new ClassInfo(VariousAccessors.class);
		assertEquals("subject class", VariousAccessors.class, info.getSubject());
		assertNull("property", info.getProperty(null));
	}

	@Test
	public void findConstructor() {
		ClassInfo info = new ClassInfo(VariousConstructors.class);
		assertEquals("subject class", VariousConstructors.class, info.getSubject());
		Constructor<?> ctor;
		// nilary
		ctor = info.findConstructorForArguments(new Object[0]);
		assertNotNull("nilary ctor found", ctor);
		assertEquals("nilary ctor", VariousConstructors.c_nil, ctor);
		// unary mismatch
		ctor = info.findConstructorForArguments(new Object[] {new Object()});
		assertNull("mismatched ctor found", ctor);
		// unary unrelated
		ctor = info.findConstructorForArguments(new Object[] {"foo"});
		assertNotNull("unary unrelated ctor found", ctor);
		assertEquals("unary unrelated ctor", VariousConstructors.c_String, ctor);
		// unary super
		ctor = info.findConstructorForArguments(new Object[] {new Super()});
		assertNotNull("unary super ctor found", ctor);
		assertEquals("unary super ctor", VariousConstructors.c_Super, ctor);
		// unary sub
		ctor = info.findConstructorForArguments(new Object[] {new Sub()});
		assertNotNull("unary sub ctor found", ctor);
		assertEquals("unary sub ctor", VariousConstructors.c_Sub, ctor);
		// binary super/super
		ctor = info.findConstructorForArguments(new Object[] {new Super(), new Super()});
		assertNull("binary super/super ctor found", ctor);
		// binary super/sub
		ctor = info.findConstructorForArguments(new Object[] {new Super(), new Sub()});
		assertNotNull("binary super/sub ctor found", ctor);
		assertEquals("binary super/sub ctor", VariousConstructors.c_Super_Sub, ctor);
		// binary sub/super
		ctor = info.findConstructorForArguments(new Object[] {new Sub(), new Super()});
		assertNotNull("binary sub/super ctor found", ctor);
		assertEquals("binary sub/super ctor", VariousConstructors.c_Sub_Super, ctor);
		// binary sub/sub
		ctor = info.findConstructorForArguments(new Object[] {new Sub(), new Sub()});
		assertNotNull("binary sub/sub ctor", ctor);
		assertEquals("binary sub/sub ctor", VariousConstructors.c_Sub_Super, ctor);
	}

	@Test
	public void findConstructorForNullArguments() {
		ClassInfo info = new ClassInfo(VariousConstructors.class);
		assertEquals("subject class", VariousConstructors.class, info.getSubject());
		Constructor<?> ctor = info.findConstructorForArguments(null);
		assertNotNull("nilary ctor found", ctor);
		assertEquals("nilary ctor", VariousConstructors.c_nil, ctor);
	}

}
