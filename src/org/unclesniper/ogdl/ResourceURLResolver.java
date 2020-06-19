package org.unclesniper.ogdl;

import java.io.IOException;
import java.io.InputStream;
import java.io.FileNotFoundException;

public class ResourceURLResolver implements DirectiveTokenSink.URLResolver {

	private Class<?> baseClass;

	public ResourceURLResolver(Class<?> baseClass) {
		this.baseClass = baseClass;
	}

	public Class<?> getBaseClass() {
		return baseClass;
	}

	public void setBaseClass(Class<?> baseClass) {
		this.baseClass = baseClass;
	}

	@Override
	public InputStream resolveURL(String text, int startIndex) throws IOException {
		String resname = text.substring(startIndex);
		Class<?> c = baseClass == null ? ResourceURLResolver.class : baseClass;
		InputStream is = c.getResourceAsStream(resname);
		if(is == null)
			throw new FileNotFoundException("Class '" + c.getName() + "' has no resource '" + resname + '\'');
		return is;
	}

}
