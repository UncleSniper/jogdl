package org.unclesniper.ogdl;

import java.lang.reflect.Method;

public class Accessor {

	public enum Type {
		SETTER,
		ADDER,
		PUTTER;
	}

	private final Property property;

	private final Method method;

	private final Class<?> keyType;

	private final Class<?> valueType;

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

	public String toString() {
		String name = property == null ? null : property.getName();
		return "<accessor(" + (name == null ? "?" : name) + "): "
				+ (method == null ? "?" : method.getName()) + ": "
				+ (keyType == null ? "?" : keyType.getName()) + " -> "
				+ (valueType == null ? "?" : valueType.getName()) + ">";
	}

}
