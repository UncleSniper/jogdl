package org.unclesniper.ogdl;

public interface ObjectGraphDocument {

	Object getRootObject();

	Iterable<String> getNamedObjects();

	boolean hasNamedObject(String name);

	Object getNamedObject(String name);

}
