package org.unclesniper.ogdl;

import java.net.URL;
import java.util.Map;
import java.util.HashMap;
import java.io.InputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;

public class DirectiveTokenSink implements TokenSink {

	public static class Wrapper implements TokenSinkWrapper {

		public Wrapper() {}

		@Override
		public TokenSink wrapTokenSink(TokenSink sink) {
			return new DirectiveTokenSink(sink);
		}

	}

	private enum State {
		NONE,
		INCLUDE_URL
	}

	public static final TokenSinkWrapper WRAPPER = new Wrapper();

	private static final Map<String, State> STATE_MAP;

	static {
		STATE_MAP = new HashMap<String, State>();
		STATE_MAP.put("%includeURL", State.INCLUDE_URL);
	}

	private TokenSink slave;

	private State state = State.NONE;

	public DirectiveTokenSink(TokenSink slave) {
		this.slave = slave;
	}

	public TokenSink getSlave() {
		return slave;
	}

	public void setSlave(TokenSink slave) {
		this.slave = slave;
	}

	@Override
	public void feedToken(Token token) throws SyntaxException, ObjectConstructionException {
		State nextState;
		switch(state) {
			case NONE:
				if(token.getType() != Token.Type.DIRECTIVE) {
					slave.feedToken(token);
					break;
				}
				nextState = DirectiveTokenSink.STATE_MAP.get(token.getText());
				if(nextState == null) {
					slave.feedToken(token);
					break;
				}
				state = nextState;
				break;
			case INCLUDE_URL:
				if(token.getType() != Token.Type.STRING)
					throw new SyntaxException(Token.MASK_STRING, token);
				state = State.NONE;
				doIncludeURL(token);
				break;
			default:
				throw new AssertionError("Unrecognized directive processor state: " + state.name());
		}
	}

	private void doIncludeURL(Token token) throws SyntaxException, ObjectConstructionException {
		URL url;
		try {
			url = new URL(token.getText());
		}
		catch(MalformedURLException mue) {
			throw new SyntaxException("valid URL", token);
		}
		Lexer lexer = new Lexer(slave);
		lexer.setFile(token.getText());
		try(InputStream is = url.openStream()) {
			InputStreamReader isr = new InputStreamReader(is, "UTF-8");
			lexer.pushStream(isr);
		}
		catch(IOException ioe) {
			throw new DescriptorInclusionException(token.getText(), token, ioe);
		}
		catch(LexicalException le) {
			throw new DescriptorInclusionException(token.getText(), token, le);
		}
	}

}
