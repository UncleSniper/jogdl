package org.unclesniper.ogdl;

public class Lexer implements Location {

	private enum State {

		NONE,
		DOLLAR,
		NAME,
		CONSTANT,
		PLUS,
		MINUS,
		INT,
		POINT,
		FLOAT,
		STRING,
		ESCAPE,
		HEX_CODE;

	}

	public static final String DEFAULT_FILE = "-";

	private String file;

	private int line = 1;

	private TokenSink sink;

	private State state = State.NONE;

	private StringBuilder buffer;

	private int code;

	private int digits;

	public Lexer(TokenSink sink) {
		this.sink = sink;
	}

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}

	public int getLine() {
		return line;
	}

	public void setLine(int line) {
		this.line = line;
	}

	public TokenSink getTokenSink() {
		return sink;
	}

	public void setTokenSink(TokenSink sink) {
		this.sink = sink;
	}

	public void pushChars(char[] data) throws LexicalException, SyntaxException, ObjectConstructionException {
		pushChars(data, 0, data.length);
	}

	public void pushChars(char[] data, int offset, int count)
			throws LexicalException, SyntaxException, ObjectConstructionException {
		for(int i = 0; i < count; ++i) {
			char c = data[i + offset];
			switch(state) {
				case NONE:
					switch(c) {
						case '.':
							pushToken(Token.Type.DOT, ".");
							break;
						case '<':
							pushToken(Token.Type.LESS, "<");
							break;
						case '>':
							pushToken(Token.Type.GREATER, ">");
							break;
						case '{':
							pushToken(Token.Type.LEFT_CURLY, "{");
							break;
						case '}':
							pushToken(Token.Type.RIGHT_CURLY, "}");
							break;
						case '[':
							pushToken(Token.Type.LEFT_SQUARE, "[");
							break;
						case ']':
							pushToken(Token.Type.RIGHT_SQUARE, "]");
							break;
						case ',':
							pushToken(Token.Type.COMMA, ",");
							break;
						case '=':
							pushToken(Token.Type.EQUAL, "=");
							break;
						case '*':
							pushToken(Token.Type.STAR, "*");
							break;
						case '(':
							pushToken(Token.Type.LEFT_ROUND, "(");
							break;
						case ')':
							pushToken(Token.Type.RIGHT_ROUND, ")");
							break;
						case '-':
							buffer = new StringBuilder("-");
							state = State.MINUS;
							break;
						case '+':
							buffer = new StringBuilder("+");
							state = State.PLUS;
							break;
						case '"':
							buffer = new StringBuilder();
							state = State.STRING;
							break;
						case '_':
							buffer = new StringBuilder("_");
							state = State.NAME;
							break;
						case '$':
							buffer = new StringBuilder();
							state = State.DOLLAR;
							break;
						default:
							if((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
								buffer = new StringBuilder();
								buffer.append(c);
								state = State.NAME;
							}
							else if(c >= '0' && c <= '9') {
								buffer = new StringBuilder();
								buffer.append(c);
								state = State.INT;
							}
							else
								throw new LexicalException(c, file, line);
							break;
					}
					break;
				case NAME:
					if((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '_')
						buffer.append(c);
					else {
						--i;
						pushToken(Token.Type.NAME);
					}
					break;
				case CONSTANT:
					if((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '_')
						buffer.append(c);
					else {
						--i;
						pushToken(Token.Type.CONSTANT);
					}
					break;
				case DOLLAR:
					if((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '_') {
						buffer.append(c);
						state = State.CONSTANT;
					}
					else
						throw new LexicalException(c, "identifier", file, line);
					break;
				case PLUS:
					if(c >= '0' && c <= '9') {
						buffer.append(c);
						state = State.INT;
					}
					else
						throw new LexicalException(c, "digit", file, line);
					break;
				case MINUS:
					if(c >= '0' && c <= '9') {
						buffer.append(c);
						state = State.INT;
					}
					else if(c == '>')
						pushToken(Token.Type.ARROW);
					else
						throw new LexicalException(c, "digit or '>'", file, line);
					break;
				case INT:
					if(c == '.') {
						buffer.append('.');
						state = State.POINT;
					}
					else if(c >= '0' && c <= '9')
						buffer.append(c);
					else {
						--i;
						pushToken(Token.Type.INT);
					}
					break;
				case POINT:
					if(c >= '0' && c <= '9') {
						buffer.append(c);
						state = State.FLOAT;
					}
					else
						throw new LexicalException(c, "digit", file, line);
					break;
				case FLOAT:
					if(c >= '0' && c <= '9')
						buffer.append(c);
					else {
						--i;
						pushToken(Token.Type.FLOAT);
					}
					break;
				case STRING:
					switch(c) {
						case '"':
							pushToken(Token.Type.STRING);
							break;
						case '\\':
							state = State.ESCAPE;
							break;
						default:
							buffer.append(c);
							break;
					}
					break;
				case ESCAPE:
					switch(c) {
						case '\\':
							buffer.append('\\');
							state = State.STRING;
							break;
						case 't':
							buffer.append('\t');
							state = State.STRING;
							break;
						case 'r':
							buffer.append('\r');
							state = State.STRING;
							break;
						case 'n':
							buffer.append('\n');
							state = State.STRING;
							break;
						case 'b':
							buffer.append('\b');
							state = State.STRING;
							break;
						case 'x':
							state = State.HEX_CODE;
							digits = code = 0;
							break;
					}
					break;
				case HEX_CODE:
					if(c >= 'A' && c <= 'F') {
						++digits;
						code = (char)(code * 16 + (c - 'A'));
					}
					else if(c >= 'a' && c <= 'f') {
						++digits;
						code = (char)(code * 16 + (c - 'a'));
					}
					else if(c >= '0' && c <= '9') {
						++digits;
						code = (char)(code * 16 + (c - '0'));
					}
					else {
						if(digits == 0)
							throw new LexicalException(c, "hexadecimal digit", file, line);
						digits = 4;
						--i;
					}
					if(digits >= 4) {
						buffer.append((char)code);
						state = State.STRING;
					}
					break;
				default:
					throw new AssertionError("Unrecognized lexer state: " + state.name());
			}
		}
	}

	private void pushToken(Token.Type type, String text) throws SyntaxException, ObjectConstructionException {
		state = State.NONE;
		sink.feedToken(new Token(file, line, type, text));
	}

	private void pushToken(Token.Type type) throws SyntaxException, ObjectConstructionException {
		state = State.NONE;
		String text = buffer.toString();
		buffer = null;
		sink.feedToken(new Token(file, line, type, text));
	}

	public void endInput() throws LexicalException, SyntaxException, ObjectConstructionException {
		switch(state) {
			case NONE:
				break;
			case DOLLAR:
				throw new LexicalException("Unexpected end of input, expected identifier", file, line);
			case NAME:
				pushToken(Token.Type.NAME);
				break;
			case CONSTANT:
				pushToken(Token.Type.CONSTANT);
				break;
			case PLUS:
			case POINT:
				throw new LexicalException("Unexpected end of input, expected digit", file, line);
			case MINUS:
				throw new LexicalException("Unexpected end of input, expected digit or '>'", file, line);
			case INT:
				pushToken(Token.Type.INT);
				break;
			case FLOAT:
				pushToken(Token.Type.FLOAT);
				break;
			case HEX_CODE:
				if(digits == 0)
					throw new LexicalException("Unexpected end of input, expected hexadecimal digit", file, line);
			case STRING:
				throw new LexicalException("Unexpected end of input, expected '\"'", file, line);
			case ESCAPE:
				throw new LexicalException("Unexpected end of input, expected escape sequence", file, line);
			default:
				throw new AssertionError("Unrecognized lexer state: " + state.name());
		}
		sink.feedToken(new Token(file, line, null, null));
	}

	public static String formatLocation(String file, int line) {
		if(file == null)
			file = Lexer.DEFAULT_FILE;
		if(line > 0)
			return file + ':' + line;
		return file + " (unknown line number)";
	}

	public static String formatLocation(Location location) {
		return Lexer.formatLocation(location.getFile(), location.getLine());
	}

}
