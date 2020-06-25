package org.unclesniper.ogdl;

public interface TokenSink {

	void feedToken(Token token) throws SyntaxException, ObjectConstructionException;

	void announceBreak() throws SyntaxException, ObjectConstructionException;

}
