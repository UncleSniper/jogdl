package org.unclesniper.ogdl;

public interface StringClassMapper {

	boolean canDeserializeObject(String specifier, Class<?> desiredType, ClassLoader loader);

	Object deserializeObject(String specifier, Class<?> desiredType, ClassLoader loader);

}
