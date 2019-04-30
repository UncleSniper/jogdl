package org.unclesniper.ogdl;

import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;

public class ClassUtils {

	private static class ClassPair {

		final Class<?> from;

		final Class<?> to;

		ClassPair(Class<?> from, Class<?> to) {
			this.from = from;
			this.to = to;
		}

		public int hashCode() {
			int fhc = from.hashCode();
			return ((fhc << 7) | (fhc >>> 25)) ^ to.hashCode();
		}

		public boolean equals(Object other) {
			if(!(other instanceof ClassPair))
				return false;
			ClassPair pair = (ClassPair)other;
			return from.equals(pair.from) && to.equals(pair.to);
		}

	}

	private ClassUtils() {}

	private static final Map<Class<?>, Class<?>> primitiveToCompound;

	private static final Map<Class<?>, Class<?>> compoundToPrimitive;

	private static final Set<ClassPair> extendedAssignable;

	static {
		primitiveToCompound = new HashMap<Class<?>, Class<?>>();
		primitiveToCompound.put(Byte.TYPE, Byte.class);
		primitiveToCompound.put(Character.TYPE, Character.class);
		primitiveToCompound.put(Integer.TYPE, Integer.class);
		primitiveToCompound.put(Integer.TYPE, Integer.class);
		primitiveToCompound.put(Long.TYPE, Long.class);
		primitiveToCompound.put(Float.TYPE, Float.class);
		primitiveToCompound.put(Double.TYPE, Double.class);
		primitiveToCompound.put(Boolean.TYPE, Boolean.class);
		compoundToPrimitive = new HashMap<Class<?>, Class<?>>();
		compoundToPrimitive.put(Byte.class, Byte.TYPE);
		compoundToPrimitive.put(Character.class, Character.TYPE);
		compoundToPrimitive.put(Integer.class, Integer.TYPE);
		compoundToPrimitive.put(Integer.class, Integer.TYPE);
		compoundToPrimitive.put(Long.class, Long.TYPE);
		compoundToPrimitive.put(Float.class, Float.TYPE);
		compoundToPrimitive.put(Double.class, Double.TYPE);
		compoundToPrimitive.put(Boolean.class, Boolean.TYPE);
		extendedAssignable = new HashSet<ClassPair>();
		// primitive -> primitive
		extendedAssignable.add(new ClassPair(Byte.TYPE, Short.TYPE));
		extendedAssignable.add(new ClassPair(Byte.TYPE, Integer.TYPE));
		extendedAssignable.add(new ClassPair(Byte.TYPE, Long.TYPE));
		extendedAssignable.add(new ClassPair(Short.TYPE, Integer.TYPE));
		extendedAssignable.add(new ClassPair(Short.TYPE, Long.TYPE));
		extendedAssignable.add(new ClassPair(Integer.TYPE, Long.TYPE));
		// primitive -> compound
		extendedAssignable.add(new ClassPair(Byte.TYPE, Short.class));
		extendedAssignable.add(new ClassPair(Byte.TYPE, Integer.class));
		extendedAssignable.add(new ClassPair(Byte.TYPE, Long.class));
		extendedAssignable.add(new ClassPair(Short.TYPE, Integer.class));
		extendedAssignable.add(new ClassPair(Short.TYPE, Long.class));
		extendedAssignable.add(new ClassPair(Integer.TYPE, Long.class));
		// compound -> primitive
		extendedAssignable.add(new ClassPair(Byte.class, Short.TYPE));
		extendedAssignable.add(new ClassPair(Byte.class, Integer.TYPE));
		extendedAssignable.add(new ClassPair(Byte.class, Long.TYPE));
		extendedAssignable.add(new ClassPair(Short.class, Integer.TYPE));
		extendedAssignable.add(new ClassPair(Short.class, Long.TYPE));
		extendedAssignable.add(new ClassPair(Integer.class, Long.TYPE));
		// compound -> compound
		extendedAssignable.add(new ClassPair(Byte.class, Short.class));
		extendedAssignable.add(new ClassPair(Byte.class, Integer.class));
		extendedAssignable.add(new ClassPair(Byte.class, Long.class));
		extendedAssignable.add(new ClassPair(Short.class, Integer.class));
		extendedAssignable.add(new ClassPair(Short.class, Long.class));
		extendedAssignable.add(new ClassPair(Integer.class, Long.class));
	}

	public static Class<?> getPrimitiveTypeOf(Class<?> clazz) {
		Class<?> p = ClassUtils.compoundToPrimitive.get(clazz);
		return p == null ? clazz : p;
	}

	public static Class<?> getCompoundTypeOf(Class<?> clazz) {
		Class<?> c = ClassUtils.primitiveToCompound.get(clazz);
		return c == null ? clazz : c;
	}

	public static boolean isExtendedAssignable(Class<?> to, Class<?> from) {
		return to.isAssignableFrom(from) || ClassUtils.extendedAssignable.contains(new ClassPair(from, to));
	}

}
