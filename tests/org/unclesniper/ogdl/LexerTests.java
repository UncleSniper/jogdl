package org.unclesniper.ogdl;

import org.junit.Test;
import java.util.List;
import java.util.Arrays;
import java.util.LinkedList;
//import static org.junit.Assert.assertEquals;

public class LexerTests {

	private static class RecordingTokenSink implements TokenSink {

		private final List<Token> tokens = new LinkedList<Token>();

		public void feedToken(Token token) {
			tokens.add(token);
		}

		void assertTokensEmitted(Token[] expected) {
			TestUtils.assertListEquals("emitted token", expected, tokens);
		}

	}

	@Test
	public void noneSingle() throws ObjectDescriptionException {
		RecordingTokenSink rec = new RecordingTokenSink();
		Lexer lexer = new Lexer(rec);
		lexer.pushChars(".<>{}[\n],=*()");
		Token[] expected = new Token[] {
			new Token(null, 1, Token.Type.DOT, "."),
			new Token(null, 1, Token.Type.LESS, "<"),
			new Token(null, 1, Token.Type.GREATER, ">"),
			new Token(null, 1, Token.Type.LEFT_CURLY, "{"),
			new Token(null, 1, Token.Type.RIGHT_CURLY, "}"),
			new Token(null, 1, Token.Type.LEFT_SQUARE, "["),
			new Token(null, 2, Token.Type.RIGHT_SQUARE, "]"),
			new Token(null, 2, Token.Type.COMMA, ","),
			new Token(null, 2, Token.Type.EQUAL, "="),
			new Token(null, 2, Token.Type.STAR, "*"),
			new Token(null, 2, Token.Type.LEFT_ROUND, "("),
			new Token(null, 2, Token.Type.RIGHT_ROUND, ")"),
		};
		rec.assertTokensEmitted(expected);
		Token[] exp2 = Arrays.copyOf(expected, expected.length + 1);
		exp2[expected.length] = new Token(null, 2, null, null);
		lexer.endInput();
		rec.assertTokensEmitted(exp2);
	}

}
