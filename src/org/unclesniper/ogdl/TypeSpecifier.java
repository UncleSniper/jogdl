package org.unclesniper.ogdl;

import java.util.List;
import java.util.LinkedList;

public class TypeSpecifier {

	private Location location;

	private StringBuilder name;

	private List<TypeSpecifier> parameters;

	private int indirections;

	public TypeSpecifier(Location location) {
		this.location = location;
	}

	public TypeSpecifier(String qname) {
		name = new StringBuilder(qname);
	}

	public Location getLocation() {
		return location;
	}

	public void addNameComponent(String component) {
		if(name == null)
			name = new StringBuilder();
		else
			name.append('.');
		name.append(component);
	}

	public String getName() {
		return name == null ? null : name.toString();
	}

	public String getJavaneseName() {
		if(name == null)
			return null;
		boolean lastWasClass = false;
		int old = 0, pos;
		String n = name.toString();
		StringBuilder j = null;
		while((pos = n.indexOf('.', old)) > -1) {
			if(pos > old) {
				if(j == null)
					j = new StringBuilder();
				else
					j.append(lastWasClass ? '$' : '.');
				j.append(n.substring(old, pos));
				char c = n.charAt(old);
				lastWasClass = c >= 'A' && c <= 'Z';
			}
			pos = old + 1;
		}
		if(old < n.length()) {
			if(j == null)
				j = new StringBuilder();
			else
				j.append(lastWasClass ? '$' : '.');
			j.append(n.substring(old));
		}
		return j == null ? null : j.toString();
	}

	public void addTypeParameter(TypeSpecifier parameter) {
		if(parameters == null)
			parameters = new LinkedList<TypeSpecifier>();
		parameters.add(parameter);
	}

	public Iterable<TypeSpecifier> getTypeParameters() {
		return parameters;
	}

	public int getTypeParameterCount() {
		return parameters == null ? 0 : parameters.size();
	}

	public void addLevelOfIndirection() {
		++indirections;
	}

	public int getLevelOfIndirection() {
		return indirections;
	}

}
