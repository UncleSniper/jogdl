package org.unclesniper.ogdl;

public class ObjectGraphDescriptor implements ObjectGraphDocument {

	private final ObjectGraphDocument document;

	public ObjectGraphDescriptor(ObjectGraphDocument document) {
		this.document = document;
	}

	public ObjectGraphDocument getDocument() {
		return document;
	}

	public Object getRootObject() {
		return document.getRootObject();
	}

	public <T> T getRootObjectAs(Class<T> desiredType) {
		return desiredType.cast(document.getRootObject());
	}

	public Iterable<String> getNamedObjects() {
		return document.getNamedObjects();
	}

	public boolean hasNamedObject(String name) {
		return document.hasNamedObject(name);
	}

	public Object getNamedObject(String name) {
		return name == null ? document.getRootObject() : document.getNamedObject(name);
	}

	public <T> T getNamedObjectAs(String name, Class<T> desiredType) {
		return desiredType.cast(getNamedObject(name));
	}

}
