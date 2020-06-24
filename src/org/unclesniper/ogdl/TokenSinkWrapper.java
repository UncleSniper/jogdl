package org.unclesniper.ogdl;

public interface TokenSinkWrapper {

	public interface WrapperChain {

		TokenSink rewrapTokenSink(TokenSink sink, TokenSinkWrapper upTo);

	}

	TokenSink wrapTokenSink(TokenSink sink, ObjectBuilder target, WrapperChain chain);

}
