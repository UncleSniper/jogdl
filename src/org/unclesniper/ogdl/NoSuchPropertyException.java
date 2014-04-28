package org.unclesniper.ogdl;

public class NoSuchPropertyException extends PropertyAccessException {

	public NoSuchPropertyException(Class<?> type, String property, Location location) {
		super(type, property, "Class '" + type.getName() + "' does not exhibit a property '" + property + '\'',
				location);
	}

	public NoSuchPropertyException(Class<?> type, String property, Location location, Throwable cause) {
		super(type, property, "Class '" + type.getName() + "' does not exhibit a property '" + property + '\'',
				location, cause);
	}

}
