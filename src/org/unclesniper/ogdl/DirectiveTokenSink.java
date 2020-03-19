package org.unclesniper.ogdl;

import java.net.URL;
import java.util.Map;
import java.util.HashMap;
import java.io.InputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;

public class DirectiveTokenSink implements TokenSink {

	public interface URLResolver {

		InputStream resolveURL(String text, int startIndex) throws IOException;

	}

	public static class Wrapper implements TokenSinkWrapper {

		private final Map<String, URLResolver> includeURLResolvers = new HashMap<String, URLResolver>();

		public Wrapper() {}

		public URLResolver getIncludeURLResolver(String schema) {
			return includeURLResolvers.get(schema);
		}

		public void setIncludeURLResolver(String schema, URLResolver resolver) {
			if(schema == null)
				return;
			if(resolver == null)
				includeURLResolvers.remove(schema);
			else
				includeURLResolvers.put(schema, resolver);
		}

		@Override
		public TokenSink wrapTokenSink(TokenSink sink) {
			DirectiveTokenSink dts = new DirectiveTokenSink(sink);
			for(Map.Entry<String, URLResolver> entry : includeURLResolvers.entrySet())
				dts.setIncludeURLResolver(entry.getKey(), entry.getValue());
			return dts;
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

	private final Map<String, URLResolver> includeURLResolvers = new HashMap<String, URLResolver>();

	public DirectiveTokenSink(TokenSink slave) {
		this.slave = slave;
	}

	public TokenSink getSlave() {
		return slave;
	}

	public void setSlave(TokenSink slave) {
		this.slave = slave;
	}

	public URLResolver getIncludeURLResolver(String schema) {
		return includeURLResolvers.get(schema);
	}

	public void setIncludeURLResolver(String schema, URLResolver resolver) {
		if(schema == null)
			return;
		if(resolver == null)
			includeURLResolvers.remove(schema);
		else
			includeURLResolvers.put(schema, resolver);
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
		String utext = token.getText();
		int pos = utext.indexOf(':');
		if(pos > 0) {
			URLResolver resolver = includeURLResolvers.get(utext.substring(0, pos));
			if(resolver != null) {
				try {
					includeStream(resolver.resolveURL(utext, pos + 1), token);
					return;
				}
				catch(IOException ioe) {
					throw new DescriptorInclusionException(utext, token, ioe);
				}
			}
		}
		URL url;
		try {
			url = new URL(utext);
		}
		catch(MalformedURLException mue) {
			throw new SyntaxException("valid URL", token);
		}
		try(InputStream is = url.openStream()) {
			includeStream(is, token);
		}
		catch(IOException ioe) {
			throw new DescriptorInclusionException(utext, token, ioe);
		}
	}

	private void includeStream(InputStream stream, Token token) throws SyntaxException, ObjectConstructionException {
		try(InputStream is = stream) {
			Lexer lexer = new Lexer(slave);
			lexer.setFile(token.getText());
			lexer.setEmitEOF(false);
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
