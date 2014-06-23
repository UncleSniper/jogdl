package org.unclesniper.ogdl;

import java.util.Map;
import java.util.HashMap;

public class ClassUtils {

	private ClassUtils() {}

	private static final Map<Class<?>, Class<?>> primitiveToCompound;

	private static final Map<Class<?>, Class<?>> compoundToPrimitive;

	static {
		primitiveToCompound = new HashMap<Class<?>, Class<?>>();
		primitiveToCompound.put(Byte.TYPE, Byte.class);
		primitiveToCompound.put(Character.TYPE, Character.class);
		primitiveToCompound.put(Integer.TYPE, Integer.class);
		primitiveToCompound.put(Integer.TYPE, Integer.class);
		primitiveToCompound.put(Long.TYPE, Long.class);
		primitiveToCompound.put(Float.TYPE, Float.class);
		primitiveToCompound.put(Double.TYPE, Double.class);
		compoundToPrimitive = new HashMap<Class<?>, Class<?>>();
		compoundToPrimitive.put(Byte.class, Byte.TYPE);
		compoundToPrimitive.put(Character.class, Character.TYPE);
		compoundToPrimitive.put(Integer.class, Integer.TYPE);
		compoundToPrimitive.put(Integer.class, Integer.TYPE);
		compoundToPrimitive.put(Long.class, Long.TYPE);
		compoundToPrimitive.put(Float.class, Float.TYPE);
		compoundToPrimitive.put(Double.class, Double.TYPE);
	}

	public static Class<?> getPrimitiveTypeOf(Class<?> clazz) {
		Class<?> p = ClassUtils.compoundToPrimitive.get(clazz);
		return p == null ? clazz : p;
	}

	public static Class<?> getCompoundTypeOf(Class<?> clazz) {
		Class<?> c = ClassUtils.primitiveToCompound.get(clazz);
		return c == null ? clazz : c;
	}

}
