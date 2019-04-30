package org.unclesniper.ogdl;

import java.util.Set;
import java.util.HashSet;

public class Property {

	public static class MappingAccessor {

		public final Accessor accessor;

		public final StringClassMapper mapper;

		public MappingAccessor(Accessor accessor, StringClassMapper mapper) {
			this.accessor = accessor;
			this.mapper = mapper;
		}

	}

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
		Class<?> keyType = key == null ? null : key.getClass();
		Class<?> valueType = value == null ? null : value.getClass();
		for(Accessor a : accessors) {
			Class<?> rawKeyParam = a.getKeyType();
			Class<?> keyParam = ClassUtils.getCompoundTypeOf(rawKeyParam);
			Class<?> rawValueParam = a.getValueType();
			Class<?> valueParam = ClassUtils.getCompoundTypeOf(rawValueParam);
			boolean better = true;
			if(keyParam == null) {
				if(valueType == null ? rawValueParam.isPrimitive()
						: !ClassUtils.isExtendedAssignable(valueParam, valueType))
					continue;
				if(bestValueParam != null) {
					boolean fromBest = ClassUtils.isExtendedAssignable(valueParam, bestValueParam);
					boolean fromThis = ClassUtils.isExtendedAssignable(bestValueParam, valueParam);
					better = fromThis && !fromBest;
				}
				if(better) {
					bestAccessor = a;
					bestValueParam = valueParam;
				}
			}
			else {
				if(keyType == null ? rawKeyParam.isPrimitive()
						: !ClassUtils.isExtendedAssignable(keyParam, keyType))
					continue;
				if(valueType == null ? rawValueParam.isPrimitive()
						: !ClassUtils.isExtendedAssignable(valueParam, valueType))
					continue;
				if(bestKeyParam != null) {
					boolean keyFromBest = ClassUtils.isExtendedAssignable(keyParam, bestKeyParam);
					boolean keyFromThis = ClassUtils.isExtendedAssignable(bestKeyParam, keyParam);
					if(keyFromThis && !keyFromBest)
						better = true;
					else if(keyFromThis == keyFromBest) {
						boolean valueFromBest = ClassUtils.isExtendedAssignable(valueParam, bestValueParam);
						boolean valueFromThis = ClassUtils.isExtendedAssignable(bestValueParam, valueParam);
						better = valueFromThis && !valueFromBest;
					}
					else
						better = false;
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

	public MappingAccessor findMappingSetterForValue(String value,
			Iterable<StringClassMapper> stringClassMappers, ClassLoader loader) {
		return Property.findMappingAccessorForValue(setters, value, stringClassMappers, loader);
	}

	public MappingAccessor findMappingAdderForValue(String value,
			Iterable<StringClassMapper> stringClassMappers, ClassLoader loader) {
		return Property.findMappingAccessorForValue(adders, value, stringClassMappers, loader);
	}

	private static MappingAccessor findMappingAccessorForValue(Set<Accessor> accessors, String value,
			Iterable<StringClassMapper> stringClassMappers, ClassLoader loader) {
		MappingAccessor ma = null;
		for(Accessor accessor : accessors) {
			for(StringClassMapper mapper : stringClassMappers) {
				if(mapper.canDeserializeObject(value, accessor.getValueType(), loader)) {
					if(ma == null)
						ma = new MappingAccessor(accessor, mapper);
					else
						return null;
				}
			}
		}
		return ma;
	}

	public static String minisculize(String name) {
		return Property.minisculize(name, 0);
	}

	public static String minisculize(String name, int index) {
		if(index < 0 || index >= name.length())
			return name;
		char c = name.charAt(index);
		if(c < 'A' || c > 'Z')
			return name.substring(index);
		return (char)(c - 'A' + 'a') + name.substring(index + 1);
	}

}
