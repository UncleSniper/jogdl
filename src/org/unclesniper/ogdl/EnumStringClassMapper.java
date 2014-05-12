package org.unclesniper.ogdl;

public class EnumStringClassMapper implements StringClassMapper {

	@SuppressWarnings("unchecked")
	private static Object getConstant(String specifier, Class type) {
		return Enum.valueOf(type, specifier);
	}

	public boolean canDeserializeObject(String specifier, Class<?> desiredType, ClassLoader loader) {
		if(!Enum.class.isAssignableFrom(desiredType))
			return false;
		try {
			EnumStringClassMapper.getConstant(specifier, desiredType);
		}
		catch(IllegalArgumentException iae) {
			return false;
		}
		return true;
	}

	public Object deserializeObject(String specifier, Class<?> desiredType, ClassLoader loader) {
		return EnumStringClassMapper.getConstant(specifier, desiredType);
	}

}
