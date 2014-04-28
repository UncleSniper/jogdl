package org.unclesniper.ogdl;

public class PropertyAccessException extends ObjectConstructionException {

	private final Class<?> type;

	private final String property;

	public PropertyAccessException(Class<?> type, String property, String message, Location location) {
		super(message, location);
		this.type = type;
		this.property = property;
	}

	public PropertyAccessException(Class<?> type, String property, String message, Location location,
			Throwable cause) {
		super(message, location, cause);
		this.type = type;
		this.property = property;
	}

	public Class<?> getTargetClass() {
		return type;
	}

	public String getPropertyName() {
		return property;
	}

}
