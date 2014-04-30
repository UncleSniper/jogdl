package org.unclesniper.ogdl;

import org.junit.Test;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertEquals;

public class PropertyTests {

	private static class Empty {}

	private static class Super {}
	private static class Sub extends Super {}

	private static class Super2 {}
	private static class Sub2 extends Super2 {}

	@Test
	public void constructor() {
		ClassInfo info = new ClassInfo(Empty.class);
		Property prop = new Property(info, "foo");
		assertSame("owning class", info, prop.getOwningClass());
		assertEquals("name", "foo", prop.getName());
	}

	@Test
	public void constructorGivenNull() {
		Property prop = new Property(null, null);
		assertNull("owning class", prop.getOwningClass());
		assertNull("name", prop.getName());
	}

	// TODO: test add*() and get*()

	// findSetterForValue

	@Test
	public void findSetterForValueUnsuitable() {
		ClassInfo info = new ClassInfo(Empty.class);
		Property prop = new Property(info, "foo");
		prop.addSetter(new Accessor(prop, null, Integer.TYPE));
		assertNull("unsuitable setter", prop.findSetterForValue("bar"));
	}

	@Test
	public void findSetterForValueExact() {
		ClassInfo info = new ClassInfo(Empty.class);
		Property prop = new Property(info, "foo");
		Accessor acc = new Accessor(prop, null, String.class);
		prop.addSetter(acc);
		Accessor found = prop.findSetterForValue("bar");
		assertSame("setter", acc, found);
	}

	@Test
	public void findSetterForValueSub() {
		ClassInfo info = new ClassInfo(Empty.class);
		Property prop = new Property(info, "foo");
		Accessor acc = new Accessor(prop, null, Super.class);
		prop.addSetter(acc);
		Accessor found = prop.findSetterForValue(new Sub());
		assertSame("setter", acc, found);
	}

	@Test
	public void findSetterForValueSuper() {
		ClassInfo info = new ClassInfo(Empty.class);
		Property prop = new Property(info, "foo");
		prop.addSetter(new Accessor(prop, null, Sub.class));
		assertNull("super setter", prop.findSetterForValue(new Super()));
	}

	@Test
	public void findSetterForValueOverloadedSub() {
		ClassInfo info = new ClassInfo(Empty.class);
		Property prop = new Property(info, "foo");
		Accessor sup = new Accessor(prop, null, Super.class);
		prop.addSetter(sup);
		Accessor sub = new Accessor(prop, null, Sub.class);
		prop.addSetter(sub);
		Accessor found = prop.findSetterForValue(new Sub());
		assertSame("sub setter", sub, found);
	}

	@Test
	public void findSetterForValueOverloadedSuper() {
		ClassInfo info = new ClassInfo(Empty.class);
		Property prop = new Property(info, "foo");
		Accessor sup = new Accessor(prop, null, Super.class);
		prop.addSetter(sup);
		Accessor sub = new Accessor(prop, null, Sub.class);
		prop.addSetter(sub);
		Accessor found = prop.findSetterForValue(new Super());
		assertSame("super setter", sup, found);
	}

	// findAdderForValue

	@Test
	public void findAdderForValueUnsuitable() {
		ClassInfo info = new ClassInfo(Empty.class);
		Property prop = new Property(info, "foo");
		prop.addAdder(new Accessor(prop, null, Integer.TYPE));
		assertNull("unsuitable adder", prop.findAdderForValue("bar"));
	}

	@Test
	public void findAdderForValueExact() {
		ClassInfo info = new ClassInfo(Empty.class);
		Property prop = new Property(info, "foo");
		Accessor acc = new Accessor(prop, null, String.class);
		prop.addAdder(acc);
		Accessor found = prop.findAdderForValue("bar");
		assertSame("adder", acc, found);
	}

	@Test
	public void findAdderForValueSub() {
		ClassInfo info = new ClassInfo(Empty.class);
		Property prop = new Property(info, "foo");
		Accessor acc = new Accessor(prop, null, Super.class);
		prop.addAdder(acc);
		Accessor found = prop.findAdderForValue(new Sub());
		assertSame("adder", acc, found);
	}

	@Test
	public void findAdderForValueSuper() {
		ClassInfo info = new ClassInfo(Empty.class);
		Property prop = new Property(info, "foo");
		prop.addAdder(new Accessor(prop, null, Sub.class));
		assertNull("super adder", prop.findAdderForValue(new Super()));
	}

	@Test
	public void findAdderForValueOverloadedSub() {
		ClassInfo info = new ClassInfo(Empty.class);
		Property prop = new Property(info, "foo");
		Accessor sup = new Accessor(prop, null, Super.class);
		prop.addAdder(sup);
		Accessor sub = new Accessor(prop, null, Sub.class);
		prop.addAdder(sub);
		Accessor found = prop.findAdderForValue(new Sub());
		assertSame("sub adder", sub, found);
	}

	@Test
	public void findAdderForValueOverloadedSuper() {
		ClassInfo info = new ClassInfo(Empty.class);
		Property prop = new Property(info, "foo");
		Accessor sup = new Accessor(prop, null, Super.class);
		prop.addAdder(sup);
		Accessor sub = new Accessor(prop, null, Sub.class);
		prop.addAdder(sub);
		Accessor found = prop.findAdderForValue(new Super());
		assertSame("super adder", sup, found);
	}

	// findPutterForBinding

	@Test
	public void findPutterForBindingUnsuitable() {
		ClassInfo info = new ClassInfo(Empty.class);
		Property prop = new Property(info, "foo");
		prop.addPutter(new Accessor(prop, null, Empty.class, Sub.class));
		assertNull("unsuitable putter", prop.findPutterForBinding("nope", new Sub()));
		assertNull("unsuitable putter", prop.findPutterForBinding(new Empty(), "nope"));
	}

	@Test
	public void findPutterForBindingExact() {
		ClassInfo info = new ClassInfo(Empty.class);
		Property prop = new Property(info, "foo");
		Accessor acc = new Accessor(prop, null, Empty.class, Sub.class);
		prop.addPutter(acc);
		Accessor found = prop.findPutterForBinding(new Empty(), new Sub());
		assertSame("putter", acc, found);
	}

	@Test
	public void findPutterForBindingSub() {
		ClassInfo info = new ClassInfo(Empty.class);
		Property prop = new Property(info, "foo");
		Accessor acc = new Accessor(prop, null, Super.class, Super2.class);
		prop.addPutter(acc);
		Accessor found = prop.findPutterForBinding(new Sub(), new Super2());
		assertSame("sub/super putter", acc, found);
		found = prop.findPutterForBinding(new Super(), new Sub2());
		assertSame("super/sub putter", acc, found);
		found = prop.findPutterForBinding(new Sub(), new Sub2());
		assertSame("sub/sub putter", acc, found);
	}

	@Test
	public void findPutterForBindingSuper() {
		ClassInfo info = new ClassInfo(Empty.class);
		Property prop = new Property(info, "foo");
		Accessor acc = new Accessor(prop, null, Sub.class, Sub2.class);
		prop.addPutter(acc);
		assertNull("super/sub putter", prop.findPutterForBinding(new Super(), new Sub2()));
		assertNull("sub/super putter", prop.findPutterForBinding(new Sub(), new Super2()));
		assertNull("super/super putter", prop.findPutterForBinding(new Super(), new Super2()));
	}

	@Test
	public void findPutterForBindingOverloaded() {
		ClassInfo info = new ClassInfo(Empty.class);
		Property prop = new Property(info, "foo");
		Accessor supSub = new Accessor(prop, null, Super.class, Sub2.class);
		prop.addPutter(supSub);
		Accessor subSup = new Accessor(prop, null, Sub.class, Super2.class);
		prop.addPutter(subSup);
		Accessor found = prop.findPutterForBinding(new Super(), new Super2());
		assertNull("super/super putter", found);
		found = prop.findPutterForBinding(new Super(), new Sub2());
		assertSame("super/sub putter", supSub, found);
		found = prop.findPutterForBinding(new Sub(), new Super2());
		assertSame("sub/super putter", subSup, found);
		found = prop.findPutterForBinding(new Sub(), new Sub2());
		assertSame("sub/sub putter", subSup, found);
	}

	@Test
	public void minisculize() {
		assertEquals("initial capital", "foo!", Property.minisculize("Foo!"));
		assertEquals("initial miniscule", "foo!", Property.minisculize("foo!"));
		assertEquals("initial digit", "123", Property.minisculize("123"));
		assertEquals("initial underscore", "_Foo", Property.minisculize("_Foo"));
		assertEquals("initial symbol", "!Foo", Property.minisculize("!Foo"));
	}

	@Test
	public void minisculizeWithIndex() {
		assertEquals("capital", "bar", Property.minisculize("fooBar", 3));
		assertEquals("miniscule", "barBaz", Property.minisculize("!barBaz", 1));
	}

}
