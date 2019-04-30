package org.unclesniper.ogdl;

public class CharacterStringClassMapper implements StringClassMapper {

	public boolean canDeserializeObject(String specifier, Class<?> desiredType, ClassLoader loader) {
		return (Character.class.equals(desiredType) || Character.TYPE.equals(desiredType))
				&& specifier.length() == 1;
	}

	public Object deserializeObject(String specifier, Class<?> desiredType, ClassLoader loader) {
		return specifier.charAt(0);
	}

}
