package org.unclesniper.ogdl;

import java.util.Deque;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.LinkedList;

public class ObjectWriter implements ObjectBuilder {

	private enum State {

		BEFORE_ROOT,
		AFTER_CONSTANT,
		BEFORE_VALUE,
		OBJECT_AFTER_TYPE,
		OBJECT_AFTER_ARGUMENT,
		EMPTY_OBJECT,
		BEFORE_PROPERTY,
		AFTER_LIST_ELEMENT,
		AFTER_MAP_KEY,
		AFTER_MAP_VALUE;

	};

	public static final String DEFAULT_INDENT_STRING = "\t";

	private PrintWriter stream;

	private Deque<State> states = new LinkedList<State>();

	private Deque<String> properties = new LinkedList<String>();

	private int indentLevel;

	private String indentString = ObjectWriter.DEFAULT_INDENT_STRING;

	public ObjectWriter(PrintWriter stream) {
		this.stream = stream;
		states.addLast(State.BEFORE_ROOT);
	}

	public PrintWriter getStream() {
		return stream;
	}

	public void setStream(PrintWriter stream) {
		this.stream = stream;
	}

	private void ensureState(State expected) throws IOException {
		if(states.isEmpty())
			throw new IllegalStateException("Cannot emit elements after end of document");
		if(expected == State.BEFORE_VALUE) {
			switch(states.getLast()) {
				case BEFORE_ROOT:
					states.removeLast();
					return;
				case OBJECT_AFTER_TYPE:
					stream.print('(');
					states.removeLast();
					states.addLast(State.OBJECT_AFTER_ARGUMENT);
					return;
				case OBJECT_AFTER_ARGUMENT:
					stream.print(", ");
					return;
				case AFTER_MAP_KEY:
					stream.print(" -> ");
					states.removeLast();
					states.addLast(State.AFTER_MAP_VALUE);
					return;
			}
		}
		if(states.getLast() != expected)
			throw new IllegalStateException("Out of sequence event call");
		states.removeLast();
	}

	private void finishObject(Location location) throws IOException {
		if(states.isEmpty()) {
			stream.println();
			return;
		}
		switch(states.getLast()) {
			case AFTER_CONSTANT:
				stream.println();
				states.removeLast();
				states.addLast(State.BEFORE_ROOT);
			case OBJECT_AFTER_ARGUMENT:
			case BEFORE_PROPERTY:
			case AFTER_LIST_ELEMENT:
			case AFTER_MAP_KEY:
			case AFTER_MAP_VALUE:
				break;
			default:
				throw new AssertionError("Unexpected state: " + states.getLast().name());
		}
	}

	private void indent() throws IOException {
		if(indentString == null)
			return;
		for(int i = 0; i < indentLevel; ++i)
			stream.print(indentString);
	}

	public void defineConstant(String name, Location location) throws ObjectSerializationIOException {
		try {
			ensureState(State.BEFORE_ROOT);
			stream.print('$');
			stream.print(name);
			stream.print(" = ");
		}
		catch(IOException ioe) {
			throw new ObjectSerializationIOException(ioe.getMessage(), location, ioe);
		}
		states.addLast(State.AFTER_CONSTANT);
		states.addLast(State.BEFORE_VALUE);
	}

	public void newInt(String value, Location location) throws ObjectSerializationIOException {
		try {
			ensureState(State.BEFORE_VALUE);
			stream.print(value);
			finishObject(location);
		}
		catch(IOException ioe) {
			throw new ObjectSerializationIOException(ioe.getMessage(), location, ioe);
		}
	}

	public void newFloat(String value, Location location) throws ObjectSerializationIOException {
		try {
			ensureState(State.BEFORE_VALUE);
			stream.print(value);
			finishObject(location);
		}
		catch(IOException ioe) {
			throw new ObjectSerializationIOException(ioe.getMessage(), location, ioe);
		}
	}

	public void newString(String value, Location location) throws ObjectSerializationIOException {
		try {
			ensureState(State.BEFORE_VALUE);
			stream.print('"');
			stream.print(Token.escapeString(value));
			stream.print('"');
			finishObject(location);
		}
		catch(IOException ioe) {
			throw new ObjectSerializationIOException(ioe.getMessage(), location, ioe);
		}
	}

	public void newBoolean(boolean value, Location location) throws ObjectSerializationIOException {
		try {
			ensureState(State.BEFORE_VALUE);
			stream.print(value ? "true" : "false");
			finishObject(location);
		}
		catch(IOException ioe) {
			throw new ObjectSerializationIOException(ioe.getMessage(), location, ioe);
		}
	}

	public void referenceConstant(String name, Location location) throws ObjectSerializationIOException {
		try {
			ensureState(State.BEFORE_VALUE);
			stream.print('$');
			stream.print(name);
			finishObject(location);
		}
		catch(IOException ioe) {
			throw new ObjectSerializationIOException(ioe.getMessage(), location, ioe);
		}
	}

	public void newObject(TypeSpecifier type) throws ObjectSerializationIOException {
		try {
			ensureState(State.BEFORE_VALUE);
			stream.print(type.getName());
		}
		catch(IOException ioe) {
			throw new ObjectSerializationIOException(ioe.getMessage(), null, ioe);
		}
		states.addLast(State.OBJECT_AFTER_TYPE);
		++indentLevel;
	}

	public void endConstruction(Location location) {
		if(states.isEmpty())
			throw new IllegalStateException("Cannot emit elements after end of document");
		switch(states.getLast()) {
			case OBJECT_AFTER_TYPE:
				stream.print(" {");
				break;
			case OBJECT_AFTER_ARGUMENT:
				stream.print(") {");
				break;
			default:
				throw new IllegalStateException("Out of sequence event call");
		}
		states.removeLast();
		states.addLast(State.EMPTY_OBJECT);
		properties.addLast(null);
	}

	private boolean enterProperty(String property, Accessor.Type mode) throws IOException {
		if(states.isEmpty())
			throw new IllegalStateException("Cannot emit elements after end of document");
		switch(states.getLast()) {
			case EMPTY_OBJECT:
				stream.println();
				indent();
				stream.print(property);
				stream.print(" = ");
				properties.removeLast();
				properties.addLast(property);
				states.removeLast();
				return true;
			case BEFORE_PROPERTY:
				stream.println(',');
				indent();
				stream.print(property);
				stream.print(" = ");
				properties.removeLast();
				properties.addLast(property);
				states.removeLast();
				return true;
			case AFTER_LIST_ELEMENT:
				if(mode != Accessor.Type.ADDER || !property.equals(properties.getLast())) {
					stream.println();
					--indentLevel;
					indent();
					stream.println("],");
					indent();
					stream.print(property);
					stream.print(" = ");
					properties.removeLast();
					properties.addLast(property);
					states.removeLast();
					return true;
				}
				else {
					stream.println(',');
					indent();
					states.removeLast();
					return false;
				}
			case AFTER_MAP_VALUE:
				if(mode != Accessor.Type.PUTTER || !property.equals(properties.getLast())) {
					stream.println();
					--indentLevel;
					indent();
					stream.println("},");
					indent();
					stream.print(property);
					stream.print(" = ");
					properties.removeLast();
					properties.addLast(property);
					states.removeLast();
					return true;
				}
				else {
					stream.println(',');
					indent();
					states.removeLast();
					return false;
				}
			default:
				throw new IllegalStateException("Out of sequence event call");
		}
	}

	public void setProperty(String property, Location location) throws ObjectSerializationIOException {
		try {
			enterProperty(property, Accessor.Type.SETTER);
		}
		catch(IOException ioe) {
			throw new ObjectSerializationIOException(ioe.getMessage(), location, ioe);
		}
		states.addLast(State.BEFORE_PROPERTY);
		states.addLast(State.BEFORE_VALUE);
	}

	public void addListElement(String property, Location location) throws ObjectSerializationIOException {
		try {
			if(enterProperty(property, Accessor.Type.ADDER)) {
				stream.println('[');
				++indentLevel;
				indent();
			}
		}
		catch(IOException ioe) {
			throw new ObjectSerializationIOException(ioe.getMessage(), location, ioe);
		}
		states.addLast(State.AFTER_LIST_ELEMENT);
		states.addLast(State.BEFORE_VALUE);
	}

	public void addMapBinding(String property, Location location) throws ObjectSerializationIOException {
		try {
			if(enterProperty(property, Accessor.Type.PUTTER)) {
				stream.println('{');
				++indentLevel;
				indent();
			}
		}
		catch(IOException ioe) {
			throw new ObjectSerializationIOException(ioe.getMessage(), location, ioe);
		}
		states.addLast(State.AFTER_MAP_KEY);
		states.addLast(State.BEFORE_VALUE);
	}

	public void endObject(Location location) throws ObjectSerializationIOException {
		if(states.isEmpty())
			throw new IllegalStateException("Cannot emit elements after end of document");
		try {
			switch(states.getLast()) {
				case EMPTY_OBJECT:
					stream.print('}');
					--indentLevel;
					states.removeLast();
					break;
				case BEFORE_PROPERTY:
					stream.println();
					--indentLevel;
					indent();
					stream.print('}');
					states.removeLast();
					break;
				case AFTER_LIST_ELEMENT:
					stream.println();
					--indentLevel;
					indent();
					stream.println(']');
					--indentLevel;
					indent();
					stream.print('}');
					states.removeLast();
					break;
				case AFTER_MAP_VALUE:
					stream.println();
					--indentLevel;
					indent();
					stream.println('}');
					--indentLevel;
					indent();
					stream.print('}');
					states.removeLast();
					break;
				default:
					throw new IllegalStateException("Out of sequence event call");
			}
		}
		catch(IOException ioe) {
			throw new ObjectSerializationIOException(ioe.getMessage(), location, ioe);
		}
		properties.removeLast();
	}

}
