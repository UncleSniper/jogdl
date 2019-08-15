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
		RIGHT_ROUND,
		TRUE,
		FALSE,
		DIRECTIVE;

		public boolean isOneOf(int mask) {
			return ((1 << ordinal()) & mask) != 0;
		}

	}

	public static final int MASK_NAME         = 00000001;
	public static final int MASK_DOT          = 00000002;
	public static final int MASK_LESS         = 00000004;
	public static final int MASK_GREATER      = 00000010;
	public static final int MASK_LEFT_CURLY   = 00000020;
	public static final int MASK_RIGHT_CURLY  = 00000040;
	public static final int MASK_LEFT_SQUARE  = 00000100;
	public static final int MASK_RIGHT_SQUARE = 00000200;
	public static final int MASK_COMMA        = 00000400;
	public static final int MASK_EQUAL        = 00001000;
	public static final int MASK_STRING       = 00002000;
	public static final int MASK_INT          = 00004000;
	public static final int MASK_FLOAT        = 00010000;
	public static final int MASK_CONSTANT     = 00020000;
	public static final int MASK_ARROW        = 00040000;
	public static final int MASK_STAR         = 00100000;
	public static final int MASK_LEFT_ROUND   = 00200000;
	public static final int MASK_RIGHT_ROUND  = 00400000;
	public static final int MASK_TRUE         = 01000000;
	public static final int MASK_FALSE        = 02000000;

	public static final int MASK_VALUE        = MASK_INT | MASK_FLOAT | MASK_STRING | MASK_TRUE | MASK_FALSE
	                                            | MASK_CONSTANT | MASK_NAME;

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

	public boolean equals(Object obj) {
		if(!(obj instanceof Token))
			return false;
		Token t = (Token)obj;
		return
				(file == null ? t.file == null : file.equals(t.file))
				&& (line <= 0 ? t.line <= 0 : line == t.line)
				&& type == t.type
				&& (text == null ? t.text == null : text.equals(t.text));
	}

	public int hashCode() {
		return
				(((file == null ? 0 : file.hashCode()) * 13
				+ (line <= 0 ? 0 : line)) * 13
				+ (type == null ? -1 : type.ordinal())) * 13
				+ (text == null ? 0 : text.hashCode());
	}

	public String toString() {
		return "<token(" + (type == null ? "<end>" : type.name().toLowerCase()) + ") '" + text
				+ "' at " + formatLocation() + '>';
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
