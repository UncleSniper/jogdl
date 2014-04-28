package org.unclesniper.ogdl;

import org.junit.Test;
import static org.junit.Assert.assertSame;

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
	}

}
