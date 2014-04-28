package org.unclesniper.ogdl;

public class CyclicConstantDependencyException extends ObjectConstructionException {

	private final String constant;

	public CyclicConstantDependencyException(String constant, Location location) {
		super("Constant '" + constant + "' is ultimately defined as itsself" + (location == null ? ""
				: " at " + Lexer.formatLocation(location)), location);
		this.constant = constant;
	}

	public CyclicConstantDependencyException(String constant, Location location, Throwable cause) {
		super("Constant '" + constant + "' is ultimately defined as itsself"
				+ (location == null ? "" : " at " + Lexer.formatLocation(location))
				+ (cause == null ? "" : ": " + cause.getMessage()), location, cause);
		this.constant = constant;
	}

	public String getConstantName() {
		return constant;
	}

}
