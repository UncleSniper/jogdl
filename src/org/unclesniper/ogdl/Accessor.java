package org.unclesniper.ogdl;

import java.lang.reflect.Method;

public class Accessor {

	public enum Type {
		SETTER,
		ADDER,
		PUTTER;
	}

	private Property property;

	private Method method;

	private Class<?> keyType;

	private Class<?> valueType;

	public Accessor(Property property, Method method, Class<?> valueType) {
		this(property, method, null, valueType);
	}

	public Accessor(Property property, Method method, Class<?> keyType, Class<?> valueType) {
		this.property = property;
		this.method = method;
		this.keyType = keyType;
		this.valueType = valueType;
	}

	public Property getProperty() {
		return property;
	}

	public Method getMethod() {
		return method;
	}

	public Class<?> getKeyType() {
		return keyType;
	}

	public Class<?> getValueType() {
		return valueType;
	}

}
