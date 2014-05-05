package org.unclesniper.ogdl;

public interface ObjectBuilder {

	void defineConstant(String name, Location location) throws ObjectConstructionException;

	void newInt(String value, Location location) throws ObjectConstructionException;

	void newFloat(String value, Location location) throws ObjectConstructionException;

	void newString(String value, Location location) throws ObjectConstructionException;

	void newBoolean(boolean value, Location location) throws ObjectConstructionException;

	void referenceConstant(String name, Location location) throws ObjectConstructionException;

	void newObject(TypeSpecifier type) throws ObjectConstructionException;

	void endConstruction(Location location) throws ObjectConstructionException;

	void setProperty(String property, Location location) throws ObjectConstructionException;

	void addListElement(String property, Location location) throws ObjectConstructionException;

	void addMapBinding(String property, Location location) throws ObjectConstructionException;

	void endObject(Location location) throws ObjectConstructionException;

}
