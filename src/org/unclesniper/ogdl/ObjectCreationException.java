package org.unclesniper.ogdl;

public class ObjectCreationException extends ObjectConstructionException {

	private final String clazz;

	public ObjectCreationException(String clazz, String message, Location location) {
		super(location == null ? message : message + " at " + Lexer.formatLocation(location), location);
		this.clazz = clazz;
	}

	public ObjectCreationException(String clazz, String message, Location location, Throwable cause) {
		super(location == null ? message : message + " at " + Lexer.formatLocation(location), location, cause);
		this.clazz = clazz;
	}

	public ObjectCreationException(String clazz, Location location) {
		super("Failed to create object of type '" + clazz
				+ (location == null ? "'" : "' at " + Lexer.formatLocation(location)), location);
		this.clazz = clazz;
	}

	public ObjectCreationException(String clazz, Location location, Throwable cause) {
		super("Failed to create object of type '" + clazz
				+ (location == null
					? (cause == null ? "'" : "': " + cause.getMessage())
					: "' at " + Lexer.formatLocation(location)
					+ (cause == null ? "" : ": " + cause.getMessage())), location, cause);
		this.clazz = clazz;
	}

	public String getTargetClassName() {
		return clazz;
	}

}
