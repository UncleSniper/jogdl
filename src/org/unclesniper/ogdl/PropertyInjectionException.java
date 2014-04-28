package org.unclesniper.ogdl;

public class PropertyInjectionException extends PropertyAccessException {

	public PropertyInjectionException(Class<?> type, String property, String message, Location location) {
		super(type, property, location == null ? message : message + " at " + Lexer.formatLocation(location),
				location);
	}

	public PropertyInjectionException(Class<?> type, String property, String message, Location location,
			Throwable cause) {
		super(type, property, location == null ? message : message + " at " + Lexer.formatLocation(location),
				location, cause);
	}

	public PropertyInjectionException(Class<?> type, String property, Location location) {
		super(type, property, "Failed to inject property '" + property + "' of object of type '"
				+ type.getName() + (location == null ? "'" : "' at " + Lexer.formatLocation(location)), location);
	}

	public PropertyInjectionException(Class<?> type, String property, Location location, Throwable cause) {
		super(type, property, "Failed to inject property '" + property + "' of object of type '"
				+ type.getName() + (location == null ? "'" : "' at " + Lexer.formatLocation(location))
				+ (cause == null ? "" : ": " + cause.getMessage()), location, cause);
	}

}
