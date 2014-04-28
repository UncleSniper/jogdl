package org.unclesniper.ogdl;

public class SyntaxException extends ObjectDescriptionException {

	private final Token near;

	public SyntaxException(String expected, Token near) {
		super("Expected " + expected + ' ' + SyntaxException.renderToken(near));
		this.near = near;
	}

	public SyntaxException(int expected, Token near) {
		this(SyntaxException.renderExpectedMask(expected), near);
	}

	private static String renderToken(Token token) {
		if(token == null)
			return "at unknown location";
		StringBuilder build = new StringBuilder();
		Token.Type type = token.getType();
		if(type == null)
			build.append("near end of input");
		else if(type == Token.Type.STRING)
			build.append("near \"" + Token.escapeString(token.getText()) + '"');
		else
			build.append("near '" + token.getText() + '\'');
		build.append(" at ");
		build.append(token.formatLocation());
		return build.toString();
	}

	private static final String[] RENDITIONS = new String[] {
		"name",
		"'.'",
		"'<'",
		"'>'",
		"'{'",
		"'}'",
		"'['",
		"']'",
		"','",
		"'='",
		"string literal",
		"integer literal",
		"floating-point literal",
		"constant name",
		"'->'",
		"'*'",
		"'('",
		"')'",
	};

	private static String renderExpectedMask(int mask) {
		StringBuilder build = new StringBuilder();
		boolean first = true;
		for(int i = 0; i < SyntaxException.RENDITIONS.length; ++i) {
			if((mask & (1 << i)) > 0) {
				if(first)
					first = false;
				else
					build.append(", ");
			}
			build.append(SyntaxException.RENDITIONS[i]);
		}
		return build.toString();
	}

}
