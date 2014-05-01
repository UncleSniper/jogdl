package org.unclesniper.ogdl;

public class ObjectSerializationIOException extends ObjectConstructionException {

	public ObjectSerializationIOException(String message, Location location) {
		super(message, location);
	}

	public ObjectSerializationIOException(String message, Location location, Throwable cause) {
		super(message, location, cause);
	}

}
