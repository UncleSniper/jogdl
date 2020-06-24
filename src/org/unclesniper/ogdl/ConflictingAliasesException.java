package org.unclesniper.ogdl;

public class ConflictingAliasesException extends ObjectConstructionException {

	private final String aliasName;

	private final String oldValue;

	private final Location oldLocation;

	private final String newValue;

	public ConflictingAliasesException(String aliasName, String oldValue, Location oldLocation,
			String newValue, Location newLocation) {
		super("Conflicting alias '" + aliasName + "': Previously defined as '" + oldValue
				+ (oldLocation == null ? "'" : "' at " + Lexer.formatLocation(oldLocation))
				+ ", then redefined as '" + newValue
				+ (newLocation == null ? "'" : "' at " + Lexer.formatLocation(newLocation)),
				newLocation);
		this.aliasName = aliasName;
		this.oldValue = oldValue;
		this.oldLocation = oldLocation;
		this.newValue = newValue;
	}

	public String getAliasName() {
		return aliasName;
	}

	public String getOldValue() {
		return oldValue;
	}

	public Location getOldLocation() {
		return oldLocation;
	}

	public String getNewValue() {
		return newValue;
	}

}
