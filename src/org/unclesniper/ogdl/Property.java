package org.unclesniper.ogdl;

import java.util.Set;
import java.util.HashSet;

public class Property {

	private ClassInfo clazz;

	private String name;

	private Set<Accessor> setters = new HashSet<Accessor>();

	private Set<Accessor> adders = new HashSet<Accessor>();

	private Set<Accessor> putters = new HashSet<Accessor>();

	public Property(ClassInfo clazz, String name) {
		this.clazz = clazz;
		this.name = name;
	}

	public ClassInfo getOwningClass() {
		return clazz;
	}

	public String getName() {
		return name;
	}

	public Iterable<Accessor> getSetters() {
		return setters;
	}

	public void addSetter(Accessor setter) {
		if(setter == null)
			throw new NullPointerException("Cannot add null setter");
		setters.add(setter);
	}

	public Iterable<Accessor> getAdders() {
		return adders;
	}

	public void addAdder(Accessor adder) {
		if(adder == null)
			throw new NullPointerException("Cannot add null adder");
		adders.add(adder);
	}

	public Iterable<Accessor> getPutters() {
		return putters;
	}

	public void addPutter(Accessor putter) {
		if(putter == null)
			throw new NullPointerException("Cannot add null putter");
		putters.add(putter);
	}

	public Accessor findSetterForValue(Object value) {
		return Property.findAccessorForValues(setters, null, value);
	}

	public Accessor findAdderForValue(Object value) {
		return Property.findAccessorForValues(adders, null, value);
	}

	public Accessor findPutterForBinding(Object key, Object value) {
		return Property.findAccessorForValues(putters, key, value);
	}

	private static Accessor findAccessorForValues(Set<Accessor> accessors, Object key, Object value) {
		Accessor bestAccessor = null;
		Class<?> bestKeyParam = null, bestValueParam = null;
		for(Accessor a : accessors) {
			Class<?> keyParam = a.getKeyType(), valueParam = a.getValueType();
			boolean better = true;
			Class<?> keyType, valueType;
			if(keyParam == null) {
				valueType = value == null ? null : value.getClass();
				if(valueType == null ? keyParam.isPrimitive() : !keyParam.isAssignableFrom(valueType))
					continue;
				if(bestValueParam != null) {
					boolean fromBest = keyParam.isAssignableFrom(bestValueParam);
					boolean fromThis = bestValueParam.isAssignableFrom(keyParam);
					better = fromThis && !fromBest;
				}
				if(better) {
					bestAccessor = a;
					bestValueParam = keyParam;
				}
			}
			else {
				keyType = key == null ? null : key.getClass();
				valueType = value == null ? null : value.getClass();
				if(keyType == null ? keyParam.isPrimitive() : !keyParam.isAssignableFrom(keyType))
					continue;
				if(valueType == null ? valueParam.isPrimitive() : !valueParam.isAssignableFrom(valueType))
					continue;
				if(bestKeyParam != null) {
					boolean keyFromBest = keyParam.isAssignableFrom(bestKeyParam);
					boolean keyFromThis = bestKeyParam.isAssignableFrom(keyParam);
					if(keyFromBest)
						better = keyFromThis;
					else if(!keyFromThis)
						better = false;
					else {
						boolean valueFromBest = valueParam.isAssignableFrom(bestValueParam);
						boolean valueFromThis = bestValueParam.isAssignableFrom(valueParam);
						better = valueFromThis && !valueFromBest;
					}
				}
				if(better) {
					bestAccessor = a;
					bestKeyParam = keyParam;
					bestValueParam = valueParam;
				}
			}
		}
		return bestAccessor;
	}

	public static String minisculize(String name) {
		return Property.minisculize(name, 0);
	}

	public static String minisculize(String name, int index) {
		if(index < 0 || index >= name.length())
			return name;
		char c = name.charAt(index);
		if(c < 'A' || c > 'Z')
			return name;
		return (char)(c - 'A' + 'a') + name.substring(index + 1);
	}

}
