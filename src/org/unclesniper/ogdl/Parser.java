package org.unclesniper.ogdl;

import java.util.Deque;
import java.util.LinkedList;

public class Parser implements TokenSink {

	private enum State {
		BEFORE_CONSTANT,
		CONSTANT_BEFORE_ASSIGN,
		BEFORE_VALUE,
		TYPE_BEFORE_NAME,
		TYPE_AFTER_NAME,
		TYPE_AFTER_DOT,
		TYPE_AFTER_LESS,
		TYPE_AFTER_PARAMETER,
		TYPE_AFTER_PARAMETERS,
		OBJECT_AFTER_TYPE,
		OBJECT_EMPTY_CONSTRUCTION,
		OBJECT_AFTER_ARGUMENT,
		OBJECT_AFTER_ARGUMENTS,
		EMPTY_OBJECT,
		BEFORE_PROPERTY,
		PROPERTY_BEFORE_ASSIGN,
		PROPERTY_BEFORE_VALUE,
		PROPERTY_AFTER_VALUE,
		EMPTY_LIST,
		AFTER_LIST_ELEMENT,
		EMPTY_MAP,
		AFTER_MAP_KEY,
		AFTER_MAP_VALUE,
		AFTER_PROPERTY,
		AFTER_DOCUMENT;
	}

	private ObjectBuilder builder;

	private Deque<State> states = new LinkedList<State>();

	private Deque<TypeSpecifier> types = new LinkedList<TypeSpecifier>();

	private Deque<String> names = new LinkedList<String>();

	private Token constant;

	public Parser(ObjectBuilder builder) {
		this.builder = builder;
		states.addLast(State.AFTER_DOCUMENT);
		states.addLast(State.BEFORE_CONSTANT);
	}

	public ObjectBuilder getObjectBuilder() {
		return builder;
	}

	public void setObjectBuilder(ObjectBuilder builder) {
		this.builder = builder;
	}

	@Override
	public void feedToken(Token token) throws SyntaxException, ObjectConstructionException {
		State state = states.removeLast();
		Token.Type type = token.getType();
		switch(state) {
			case BEFORE_CONSTANT:
				if(type == null)
					throw new SyntaxException(Token.MASK_VALUE, token);
				if(type == Token.Type.CONSTANT) {
					constant = token;
					states.addLast(State.CONSTANT_BEFORE_ASSIGN);
					break;
				}
				states.addLast(State.BEFORE_VALUE);
				feedToken(token);
				break;
			case CONSTANT_BEFORE_ASSIGN:
				if(type == null) {
					builder.referenceConstant(constant.getText(), constant);
					constant = null;
					break;
				}
				if(type != Token.Type.EQUAL)
					throw new SyntaxException(Token.MASK_EQUAL, token);
				builder.defineConstant(constant.getText(), token);
				constant = null;
				states.add(State.BEFORE_CONSTANT);
				states.add(State.BEFORE_VALUE);
				break;
			case BEFORE_VALUE:
				if(type == null)
					throw new SyntaxException(Token.MASK_VALUE, token);
				switch(type) {
					case INT:
						builder.newInt(token.getText(), token);
						break;
					case FLOAT:
						builder.newFloat(token.getText(), token);
						break;
					case STRING:
						builder.newString(token.getText(), token);
						break;
					case CONSTANT:
						builder.referenceConstant(token.getText(), token);
						break;
					case TRUE:
						builder.newBoolean(true, token);
						break;
					case FALSE:
						builder.newBoolean(false, token);
						break;
					case NAME:
						{
							TypeSpecifier tspec = new TypeSpecifier(token);
							tspec.addNameComponent(token.getText());
							types.addLast(tspec);
						}
						states.addLast(State.OBJECT_AFTER_TYPE);
						states.addLast(State.TYPE_AFTER_NAME);
						break;
					default:
						throw new SyntaxException(Token.MASK_VALUE, token);
				}
				break;
			case TYPE_BEFORE_NAME:
				if(type != Token.Type.NAME)
					throw new SyntaxException(Token.MASK_NAME, token);
				{
					TypeSpecifier tspec = new TypeSpecifier(token);
					tspec.addNameComponent(token.getText());
					types.addLast(tspec);
				}
				states.addLast(State.TYPE_AFTER_NAME);
				break;
			case TYPE_AFTER_NAME:
				if(type == null)
					throw new SyntaxException(Token.MASK_DOT | Token.MASK_LESS | Token.MASK_STAR, token);
				switch(type) {
					case DOT:
						states.addLast(State.TYPE_AFTER_DOT);
						break;
					case LESS:
						states.addLast(State.TYPE_AFTER_LESS);
						break;
					case STAR:
						types.getLast().addLevelOfIndirection();
						states.addLast(State.TYPE_AFTER_PARAMETERS);
						break;
					default:
						feedToken(token);
				}
				break;
			case TYPE_AFTER_DOT:
				if(type != Token.Type.NAME)
					throw new SyntaxException(Token.MASK_NAME, token);
				types.getLast().addNameComponent(token.getText());
				states.addLast(State.TYPE_AFTER_NAME);
				break;
			case TYPE_AFTER_LESS:
				if(type == null)
					throw new SyntaxException(Token.MASK_GREATER | Token.MASK_NAME, token);
				switch(type) {
					case GREATER:
						states.addLast(State.TYPE_AFTER_PARAMETERS);
						break;
					case NAME:
						{
							TypeSpecifier tspec = new TypeSpecifier(token);
							tspec.addNameComponent(token.getText());
							types.addLast(tspec);
						}
						states.addLast(State.TYPE_AFTER_PARAMETER);
						states.addLast(State.TYPE_AFTER_NAME);
						break;
					default:
						throw new SyntaxException(Token.MASK_GREATER | Token.MASK_NAME, token);
				}
				break;
			case TYPE_AFTER_PARAMETER:
				{
					TypeSpecifier param = types.removeLast();
					types.getLast().addTypeParameter(param);
				}
				if(type == null)
					throw new SyntaxException(Token.MASK_COMMA | Token.MASK_GREATER, token);
				switch(type) {
					case COMMA:
						states.addLast(State.TYPE_AFTER_PARAMETER);
						states.addLast(State.TYPE_BEFORE_NAME);
						break;
					case GREATER:
						states.addLast(State.TYPE_AFTER_PARAMETERS);
						break;
					default:
						throw new SyntaxException(Token.MASK_COMMA | Token.MASK_GREATER, token);
				}
				break;
			case TYPE_AFTER_PARAMETERS:
				if(type == Token.Type.STAR) {
					types.getLast().addLevelOfIndirection();
					states.addLast(State.TYPE_AFTER_PARAMETERS);
				}
				else
					feedToken(token);
				break;
			case OBJECT_AFTER_TYPE:
				builder.newObject(types.removeLast());
				if(type == null)
					throw new SyntaxException(Token.MASK_LEFT_ROUND | Token.MASK_LEFT_CURLY, token);
				switch(type) {
					case LEFT_ROUND:
						states.addLast(State.OBJECT_EMPTY_CONSTRUCTION);
						break;
					case LEFT_CURLY:
						builder.endConstruction(token);
						states.addLast(State.EMPTY_OBJECT);
						break;
					default:
						throw new SyntaxException(Token.MASK_LEFT_ROUND | Token.MASK_LEFT_CURLY, token);
				}
				break;
			case OBJECT_EMPTY_CONSTRUCTION:
				if(type == null)
					throw new SyntaxException(Token.MASK_RIGHT_ROUND | Token.MASK_VALUE, token);
				if(type == Token.Type.RIGHT_ROUND) {
					builder.endConstruction(token);
					states.addLast(State.OBJECT_AFTER_ARGUMENTS);
				}
				else {
					if(!type.isOneOf(Token.MASK_VALUE))
						throw new SyntaxException(Token.MASK_RIGHT_ROUND | Token.MASK_VALUE, token);
					states.addLast(State.OBJECT_AFTER_ARGUMENT);
					states.addLast(State.BEFORE_VALUE);
					feedToken(token);
				}
				break;
			case OBJECT_AFTER_ARGUMENT:
				if(type == null)
					throw new SyntaxException(Token.MASK_COMMA | Token.MASK_RIGHT_ROUND, token);
				switch(type) {
					case COMMA:
						states.addLast(State.OBJECT_AFTER_ARGUMENT);
						states.addLast(State.BEFORE_VALUE);
						break;
					case RIGHT_ROUND:
						builder.endConstruction(token);
						states.addLast(State.OBJECT_AFTER_ARGUMENTS);
						break;
					default:
						throw new SyntaxException(Token.MASK_COMMA | Token.MASK_RIGHT_ROUND, token);
				}
				break;
			case OBJECT_AFTER_ARGUMENTS:
				if(type != Token.Type.LEFT_CURLY)
					throw new SyntaxException(Token.MASK_LEFT_CURLY, token);
				states.addLast(State.EMPTY_OBJECT);
				break;
			case EMPTY_OBJECT:
				if(type == null)
					throw new SyntaxException(Token.MASK_RIGHT_CURLY | Token.MASK_NAME, token);
				switch(type) {
					case NAME:
						names.addLast(token.getText());
						states.addLast(State.PROPERTY_BEFORE_ASSIGN);
						break;
					case RIGHT_CURLY:
						builder.endObject(token);
						break;
					default:
						throw new SyntaxException(Token.MASK_RIGHT_CURLY | Token.MASK_NAME, token);
				}
				break;
			case BEFORE_PROPERTY:
				if(type != Token.Type.NAME)
					throw new SyntaxException(Token.MASK_NAME, token);
				names.addLast(token.getText());
				states.addLast(State.PROPERTY_BEFORE_ASSIGN);
				break;
			case PROPERTY_BEFORE_ASSIGN:
				if(type != Token.Type.EQUAL)
					throw new SyntaxException(Token.MASK_EQUAL, token);
				states.addLast(State.PROPERTY_BEFORE_VALUE);
				break;
			case PROPERTY_BEFORE_VALUE:
				if(type == null)
					throw new SyntaxException(Token.MASK_VALUE | Token.MASK_LEFT_SQUARE
							| Token.MASK_LEFT_CURLY, token);
				switch(type) {
					case LEFT_SQUARE:
						states.addLast(State.EMPTY_LIST);
						break;
					case LEFT_CURLY:
						states.addLast(State.EMPTY_MAP);
						break;
					default:
						if(!type.isOneOf(Token.MASK_VALUE))
							throw new SyntaxException(Token.MASK_VALUE | Token.MASK_LEFT_SQUARE
									| Token.MASK_LEFT_CURLY, token);
						builder.setProperty(names.getLast(), token);
						states.addLast(State.PROPERTY_AFTER_VALUE);
						states.addLast(State.BEFORE_VALUE);
						feedToken(token);
						break;
				}
				break;
			case PROPERTY_AFTER_VALUE:
				names.removeLast();
				if(type == null)
					throw new SyntaxException(Token.MASK_COMMA | Token.MASK_RIGHT_CURLY, token);
				switch(type) {
					case COMMA:
						states.addLast(State.BEFORE_PROPERTY);
						break;
					case RIGHT_CURLY:
						builder.endObject(token);
						break;
					default:
						throw new SyntaxException(Token.MASK_COMMA | Token.MASK_RIGHT_CURLY, token);
				}
				break;
			case EMPTY_LIST:
				if(type == null)
					throw new SyntaxException(Token.MASK_RIGHT_SQUARE | Token.MASK_VALUE, token);
				if(type == Token.Type.RIGHT_SQUARE)
					states.addLast(State.AFTER_PROPERTY);
				else {
					builder.addListElement(names.getLast(), token);
					if(!type.isOneOf(Token.MASK_VALUE))
						throw new SyntaxException(Token.MASK_RIGHT_SQUARE | Token.MASK_VALUE, token);
					states.addLast(State.AFTER_LIST_ELEMENT);
					states.addLast(State.BEFORE_VALUE);
					feedToken(token);
				}
				break;
			case AFTER_LIST_ELEMENT:
				if(type == null)
					throw new SyntaxException(Token.MASK_COMMA | Token.MASK_RIGHT_SQUARE, token);
				switch(type) {
					case COMMA:
						builder.addListElement(names.getLast(), token);
						states.addLast(State.AFTER_LIST_ELEMENT);
						states.addLast(State.BEFORE_VALUE);
						break;
					case RIGHT_SQUARE:
						states.addLast(State.AFTER_PROPERTY);
						break;
					default:
						throw new SyntaxException(Token.MASK_COMMA | Token.MASK_RIGHT_SQUARE, token);
				}
				break;
			case EMPTY_MAP:
				if(type == null)
					throw new SyntaxException(Token.MASK_RIGHT_CURLY | Token.MASK_VALUE, token);
				if(type == Token.Type.RIGHT_CURLY)
					states.addLast(State.AFTER_PROPERTY);
				else {
					builder.addMapBinding(names.getLast(), token);
					if(!type.isOneOf(Token.MASK_VALUE))
						throw new SyntaxException(Token.MASK_RIGHT_CURLY | Token.MASK_VALUE, token);
					states.addLast(State.AFTER_MAP_KEY);
					states.addLast(State.BEFORE_VALUE);
					feedToken(token);
				}
				break;
			case AFTER_MAP_KEY:
				if(type != Token.Type.ARROW)
					throw new SyntaxException(Token.MASK_ARROW, token);
				states.addLast(State.AFTER_MAP_VALUE);
				states.addLast(State.BEFORE_VALUE);
				break;
			case AFTER_MAP_VALUE:
				if(type == null)
					throw new SyntaxException(Token.MASK_COMMA | Token.MASK_RIGHT_CURLY, token);
				switch(type) {
					case COMMA:
						builder.addMapBinding(names.getLast(), token);
						states.addLast(State.AFTER_MAP_KEY);
						states.addLast(State.BEFORE_VALUE);
						break;
					case RIGHT_CURLY:
						states.addLast(State.AFTER_PROPERTY);
						break;
					default:
						throw new SyntaxException(Token.MASK_COMMA | Token.MASK_RIGHT_CURLY, token);
				}
				break;
			case AFTER_PROPERTY:
				names.removeLast();
				if(type == null)
					throw new SyntaxException(Token.MASK_COMMA | Token.MASK_RIGHT_CURLY, token);
				switch(type) {
					case COMMA:
						states.addLast(State.BEFORE_PROPERTY);
						break;
					case RIGHT_CURLY:
						builder.endObject(token);
						break;
					default:
						throw new SyntaxException(Token.MASK_COMMA | Token.MASK_RIGHT_CURLY, token);
				}
				break;
			case AFTER_DOCUMENT:
				if(type == null)
					states.addLast(State.AFTER_DOCUMENT);
				else
					throw new SyntaxException("end of input", token);
				break;
			default:
				throw new AssertionError("Unrecognized parser state: " + state.name());
		}
	}

	@Override
	public void announceBreak() {}

}
