package org.unclesniper.ogdl;

public class Token implements Location {

	public enum Type {

		NAME,
		DOT,
		LESS,
		GREATER,
		LEFT_CURLY,
		RIGHT_CURLY,
		LEFT_SQUARE,
		RIGHT_SQUARE,
		COMMA,
		EQUAL,
		STRING,
		INT,
		FLOAT,
		CONSTANT,
		ARROW,
		STAR,
		LEFT_ROUND,
		RIGHT_ROUND;

		public boolean isOneOf(int mask) {
			return ((1 << ordinal()) & mask) != 0;
		}

	}

	public static final int MASK_NAME         = 0000001;
	public static final int MASK_DOT          = 0000002;
	public static final int MASK_LESS         = 0000004;
	public static final int MASK_GREATER      = 0000010;
	public static final int MASK_LEFT_CURLY   = 0000020;
	public static final int MASK_RIGHT_CURLY  = 0000040;
	public static final int MASK_LEFT_SQUARE  = 0000100;
	public static final int MASK_RIGHT_SQUARE = 0000200;
	public static final int MASK_COMMA        = 0000400;
	public static final int MASK_EQUAL        = 0001000;
	public static final int MASK_STRING       = 0002000;
	public static final int MASK_INT          = 0004000;
	public static final int MASK_FLOAT        = 0010000;
	public static final int MASK_CONSTANT     = 0020000;
	public static final int MASK_ARROW        = 0040000;
	public static final int MASK_STAR         = 0100000;
	public static final int MASK_LEFT_ROUND   = 0200000;
	public static final int MASK_RIGHT_ROUND  = 0400000;

	public static final int MASK_VALUE        = MASK_INT | MASK_FLOAT | MASK_STRING | MASK_CONSTANT | MASK_NAME;

	private static final char[] HEX_DIGITS = "0123456789ABCDEF".toCharArray();

	private String file;

	private int line;

	private Type type;

	private String text;

	public Token(String file, int line, Type type, String text) {
		this.file = file;
		this.line = line;
		this.type = type;
		this.text = text;
	}

	public String getFile() {
		return file;
	}

	public int getLine() {
		return line;
	}

	public Type getType() {
		return type;
	}

	public String getText() {
		return text;
	}

	public String formatLocation() {
		return Lexer.formatLocation(file, line);
	}

	public static String escapeString(String value) {
		StringBuilder build = new StringBuilder();
		int length = value.length();
		for(int i = 0; i < length; ++i) {
			char c = value.charAt(i);
			if(c >= ' ' && c <= '~')
				build.append(c);
			else {
				switch(c) {
					case '\\':
						build.append("\\\\");
						break;
					case '\t':
						build.append("\\t");
						break;
					case '\r':
						build.append("\\r");
						break;
					case '\n':
						build.append("\\n");
						break;
					case '\b':
						build.append("\\b");
						break;
					default:
						{
							int code = (int)c;
							build.append("\\x");
							for(int j = 12; j >= 0; j -= 4)
								build.append(Token.HEX_DIGITS[(code >> j) & 0xFF]);
						}
						break;
				}
			}
		}
		return build.toString();
	}

}
