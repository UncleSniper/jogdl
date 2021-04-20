package org.unclesniper.ogdl;

public class TokenSinkWrapperChain implements TokenSinkWrapper.WrapperChain {

	private final ObjectBuilder target;

	private final Iterable<TokenSinkWrapper> sinkWrappers;

	public TokenSinkWrapperChain(ObjectBuilder target, Iterable<TokenSinkWrapper> sinkWrappers) {
		if(target == null)
			throw new IllegalArgumentException("Target object builder cannot be null");
		this.target = target;
		this.sinkWrappers = sinkWrappers;
	}

	@Override
	public TokenSink rewrapTokenSink(TokenSink sink, TokenSinkWrapper upTo) {
		if(upTo != null) {
			boolean found = false;
			if(sinkWrappers != null) {
				for(TokenSinkWrapper wrapper : sinkWrappers) {
					if(wrapper == upTo)
						found = true;
					if(found)
						sink = wrapper.wrapTokenSink(sink, target, this);
				}
			}
			if(found)
				return sink;
		}
		if(sinkWrappers != null) {
			for(TokenSinkWrapper wrapper : sinkWrappers)
				sink = wrapper.wrapTokenSink(sink, target, this);
		}
		return sink;
	}

}
