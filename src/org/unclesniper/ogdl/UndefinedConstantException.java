package org.unclesniper.ogdl;

public class UndefinedConstantException extends ObjectConstructionException {

	private final String constant;

	public UndefinedConstantException(String constant, Location location) {
		super("Undefined constant '" + constant + (location == null ? "'"
				: "' at " + Lexer.formatLocation(location)), location);
		this.constant = constant;
	}

	public UndefinedConstantException(String constant, Location location, Throwable cause) {
		super("Undefined constant '" + constant
				+ (location == null ? "' " : "' at " + Lexer.formatLocation(location))
				+ (cause == null ? "" : ": " + cause.getMessage()), location, cause);
		this.constant = constant;
	}

	public String getConstantName() {
		return constant;
	}

}
