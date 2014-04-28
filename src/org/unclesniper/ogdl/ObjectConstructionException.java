package org.unclesniper.ogdl;

public class ObjectConstructionException extends ObjectDescriptionException {

	private final Location location;

	public ObjectConstructionException(String message, Location location) {
		super(message);
		this.location = location;
	}

	public ObjectConstructionException(String message, Location location, Throwable cause) {
		super(message, cause);
		this.location = location;
	}

	public Location getLocation() {
		return location;
	}

}
