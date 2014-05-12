package org.unclesniper.ogdl;

import java.util.Map;
import java.util.HashMap;

public class ClassStringClassMapper implements StringClassMapper {

	private static final Map<String, Class<?>> namedClasses;

	static {
		namedClasses = new HashMap<String, Class<?>>();
		namedClasses.put("byte", Byte.TYPE);
		namedClasses.put("char", Character.TYPE);
		namedClasses.put("short", Integer.TYPE);
		namedClasses.put("int", Integer.TYPE);
		namedClasses.put("long", Long.TYPE);
		namedClasses.put("float", Float.TYPE);
		namedClasses.put("double", Double.TYPE);
	}

	public boolean canDeserializeObject(String specifier, Class<?> desiredType, ClassLoader loader) {
		if(!Class.class.isAssignableFrom(desiredType))
			return false;
		if(ClassStringClassMapper.namedClasses.containsKey(specifier))
			return true;
		if(loader == null)
			loader = ClassStringClassMapper.class.getClassLoader();
		try {
			loader.loadClass(specifier);
		}
		catch(ClassNotFoundException cnfe) {
			return false;
		}
		return true;
	}

	public Object deserializeObject(String specifier, Class<?> desiredType, ClassLoader loader) {
		if(ClassStringClassMapper.namedClasses.containsKey(specifier))
			return ClassStringClassMapper.namedClasses.get(specifier);
		if(loader == null)
			loader = ClassStringClassMapper.class.getClassLoader();
		try {
			return loader.loadClass(specifier);
		}
		catch(ClassNotFoundException cnfe) {
			return null;
		}
	}

}
