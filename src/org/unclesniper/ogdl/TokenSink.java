package org.unclesniper.ogdl;

public interface TokenSink {

	void feedToken(Token token) throws SyntaxException, ObjectConstructionException;

}
