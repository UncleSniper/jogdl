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
		public TokenSink wrapTokenSink(TokenSink sink, ObjectBuilder target, WrapperChain chain) {
			DirectiveTokenSink dts = new DirectiveTokenSink(sink, target, chain);
			dts.setMyWrapper(this);
			for(Map.Entry<String, URLResolver> entry : includeURLResolvers.entrySet())
				dts.setIncludeURLResolver(entry.getKey(), entry.getValue());
			return dts;
		}

	}

	private enum State {
		NONE,
		INCLUDE_URL,
		ALIAS,
		ALIAS_NEW_NAME,
		ALIAS_EQUAL,
		ALIAS_OLD_NAME
	}

	public static final TokenSinkWrapper WRAPPER = new Wrapper();

	private static final Map<String, State> STATE_MAP;

	static {
		STATE_MAP = new HashMap<String, State>();
		STATE_MAP.put("%includeURL", State.INCLUDE_URL);
		STATE_MAP.put("%alias", State.ALIAS);
	}

	private TokenSink slave;

	private ObjectBuilder target;

	private TokenSinkWrapper.WrapperChain wrapperChain;

	private TokenSinkWrapper myWrapper;

	private State state = State.NONE;

	private final Map<String, URLResolver> includeURLResolvers = new HashMap<String, URLResolver>();

	private final StringBuilder buffer = new StringBuilder();

	private String cachedString;

	private Location cachedLocation;

	private Location directiveStartLocation;

	public DirectiveTokenSink(TokenSink slave, ObjectBuilder target, TokenSinkWrapper.WrapperChain wrapperChain) {
		this.slave = slave;
		this.target = target;
		this.wrapperChain = wrapperChain;
	}

	public TokenSink getSlave() {
		return slave;
	}

	public void setSlave(TokenSink slave) {
		this.slave = slave;
	}

	public ObjectBuilder getTarget() {
		return target;
	}

	public void setTarget(ObjectBuilder target) {
		this.target = target;
	}

	public TokenSinkWrapper.WrapperChain getWrapperChain() {
		return wrapperChain;
	}

	public void setWrapperChain(TokenSinkWrapper.WrapperChain wrapperChain) {
		this.wrapperChain = wrapperChain;
	}

	public TokenSinkWrapper getMyWrapper() {
		return myWrapper;
	}

	public void setMyWrapper(TokenSinkWrapper myWrapper) {
		this.myWrapper = myWrapper;
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
				directiveStartLocation = token;
				buffer.setLength(0);
				break;
			case INCLUDE_URL:
				if(token.getType() != Token.Type.STRING)
					throw new SyntaxException(Token.MASK_STRING, token);
				state = State.NONE;
				doIncludeURL(token);
				break;
			case ALIAS:
				if(token.getType() != Token.Type.NAME)
					throw new SyntaxException(Token.MASK_NAME, token);
				buffer.append(token.getText());
				state = State.ALIAS_NEW_NAME;
				break;
			case ALIAS_NEW_NAME:
				switch(token.getType()) {
					case DOT:
						buffer.append('.');
						state = State.ALIAS;
						break;
					case EQUAL:
						cachedString = buffer.toString();
						cachedLocation = token;
						buffer.setLength(0);
						state = State.ALIAS_EQUAL;
						break;
					default:
						throw new SyntaxException(Token.MASK_DOT | Token.MASK_EQUAL, token);
				}
				break;
			case ALIAS_EQUAL:
				if(token.getType() != Token.Type.NAME)
					throw new SyntaxException(Token.MASK_NAME, token);
				buffer.append(token.getText());
				state = State.ALIAS_OLD_NAME;
				break;
			case ALIAS_OLD_NAME:
				if(token.getType() == Token.Type.DOT) {
					buffer.append('.');
					state = State.ALIAS_EQUAL;
					break;
				}
				target.defineAlias(buffer.toString(), cachedString, cachedLocation);
				state = State.NONE;
				feedToken(token);
				break;
			default:
				throw new AssertionError("Unrecognized directive processor state: " + state.name());
		}
	}

	@Override
	public void announceBreak() throws SyntaxException, ObjectConstructionException {
		switch(state) {
			case NONE:
				break;
			case INCLUDE_URL:
				noBreak(Token.MASK_STRING);
			case ALIAS:
				noBreak(Token.MASK_NAME);
			case ALIAS_NEW_NAME:
				noBreak(Token.MASK_DOT | Token.MASK_EQUAL);
			case ALIAS_EQUAL:
				noBreak(Token.MASK_NAME);
			case ALIAS_OLD_NAME:
				target.defineAlias(buffer.toString(), cachedString, cachedLocation);
				state = State.NONE;
				break;
			default:
				throw new AssertionError("Unrecognized directive processor state: " + state.name());
		}
		slave.announceBreak();
	}

	private void noBreak(int expected) throws SyntaxException {
		throw new SyntaxException(expected, new Token(directiveStartLocation.getFile(),
				directiveStartLocation.getLine(), null, null));
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
			TokenSink mySlave = slave;
			if(wrapperChain != null)
				mySlave = wrapperChain.rewrapTokenSink(mySlave, myWrapper);
			Lexer lexer = new Lexer(mySlave);
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
