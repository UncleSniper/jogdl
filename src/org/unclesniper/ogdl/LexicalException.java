package org.unclesniper.ogdl;

public class LexicalException extends ObjectDescriptionException {

	private static final char[] HEX_DIGITS = "0123456789ABCDEF".toCharArray();

	private final String file;

	private final int line;

	public LexicalException(String message, String file, int line) {
		super(message + " at " + Lexer.formatLocation(file, line));
		this.file = file;
		this.line = line;
	}

	public LexicalException(char unexpected, String file, int line) {
		this("Unexpected character " + LexicalException.renderCharacter(unexpected), file, line);
	}

	public LexicalException(char unexpected, String expected, String file, int line) {
		this("Unexpected character " + LexicalException.renderCharacter(unexpected)
				+ ", expected " + expected, file, line);
	}

	public String getFile() {
		return file;
	}

	public int getLine() {
		return line;
	}

	public static String renderCharacter(char c) {
		if(c >= ' ' && c <= '~')
			return "'" + c + "'";
		switch(c) {
			case '\\':
				return "'\\\\'";
			case '\t':
				return "'\\t'";
			case '\r':
				return "'\\r'";
			case '\n':
				return "'\\n'";
			case '\b':
				return "'\\b'";
		}
		int code = (int)c;
		StringBuilder build = new StringBuilder("'\\x");
		for(int i = 12; i >= 0; i -= 4)
			build.append(LexicalException.HEX_DIGITS[(code >> i) & 0xFF]);
		build.append('\'');
		return build.toString();
	}

}
