package org.unclesniper.ogdl;

public class RedefinedConstantException extends ObjectConstructionException {

	private final String constant;

	public RedefinedConstantException(String constant, Location location) {
		super("Constant '" + constant + (location == null ? "' redefined"
				: "' redefined at " + Lexer.formatLocation(location)), location);
		this.constant = constant;
	}

	public RedefinedConstantException(String constant, Location location, Throwable cause) {
		super("Constant '" + constant
				+ (location == null ? "' redefined" : "' redefined at " + Lexer.formatLocation(location))
				+ (cause == null ? "" : ": " + cause.getMessage()), location, cause);
		this.constant = constant;
	}

	public String getConstantName() {
		return constant;
	}

}
