package org.unclesniper.ogdl;

public class DescriptorInclusionException extends ObjectConstructionException {

	private final String included;

	public DescriptorInclusionException(String included, Location location, Throwable cause) {
		super("Including descriptor" + (included != null && included.length() > 0 ? ' ' + included : "")
				+ (location != null ? " at " + Lexer.formatLocation(location) : "") + ": " + cause,
				location, cause);
		this.included = included;
	}

	public String getIncluded() {
		return included;
	}

}
