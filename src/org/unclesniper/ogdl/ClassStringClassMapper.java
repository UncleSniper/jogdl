package org.unclesniper.ogdl;

public class ClassStringClassMapper implements StringClassMapper {

	public boolean canDeserializeObject(String specifier, Class<?> desiredType, ClassLoader loader) {
		if(!Class.class.isAssignableFrom(desiredType))
			return false;
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
