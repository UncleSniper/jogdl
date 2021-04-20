package org.unclesniper.ogdl;

import java.util.Set;
import java.util.List;
import java.util.HashSet;
import java.util.LinkedList;

public class NullObjectBuilder implements ObjectBuilder {

	private static class PendingConstant {

		private final String name;

		private final Location location;

		public PendingConstant(String name, Location location) {
			this.name = name;
			this.location = location;
		}

		public String getName() {
			return name;
		}

		public Location getLocation() {
			return location;
		}

	}

	private boolean checkConstants;

	private final Set<String> constants = new HashSet<String>();

	private boolean inConstantDefinition;

	private int objectDepth;

	private final List<PendingConstant> pendingConstants = new LinkedList<PendingConstant>();

	public NullObjectBuilder() {}

	public boolean isCheckConstants() {
		return checkConstants;
	}

	public void setCheckConstants(boolean checkConstants) {
		this.checkConstants = checkConstants;
	}

	private void finishObject() throws UndefinedConstantException {
		if(objectDepth > 0)
			return;
		if(inConstantDefinition) {
			inConstantDefinition = false;
			return;
		}
		if(!checkConstants)
			return;
		for(PendingConstant pc : pendingConstants) {
			if(!constants.contains(pc.getName()))
				throw new UndefinedConstantException(pc.getName(), pc.getLocation());
		}
	}

	@Override
	public void defineConstant(String name, Location location) throws RedefinedConstantException {
		inConstantDefinition = true;
		if(!checkConstants)
			return;
		if(constants.contains(name))
			throw new RedefinedConstantException(name, location);
		constants.add(name);
	}

	@Override
	public void newInt(String value, Location location) throws UndefinedConstantException {
		finishObject();
	}

	@Override
	public void newFloat(String value, Location location) throws UndefinedConstantException {
		finishObject();
	}

	@Override
	public void newString(String value, Location location) throws UndefinedConstantException {
		finishObject();
	}

	@Override
	public void newBoolean(boolean value, Location location) throws UndefinedConstantException {
		finishObject();
	}

	@Override
	public void referenceConstant(String name, Location location) throws UndefinedConstantException {
		pendingConstants.add(new PendingConstant(name, location));
		finishObject();
	}

	@Override
	public void newObject(TypeSpecifier type) {
		++objectDepth;
	}

	@Override
	public void endConstruction(Location location) {}

	@Override
	public void setProperty(String property, Location location) {}

	@Override
	public void addListElement(String property, Location location) {}

	@Override
	public void addMapBinding(String property, Location location) {}

	@Override
	public void endObject(Location location) throws UndefinedConstantException {
		--objectDepth;
		finishObject();
	}

	@Override
	public void defineAlias(String oldName, String newName, Location location) {}

}
