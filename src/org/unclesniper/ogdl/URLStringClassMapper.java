package org.unclesniper.ogdl;

import java.net.URL;
import java.io.File;
import java.net.MalformedURLException;

public class URLStringClassMapper implements StringClassMapper {

	private static URL makeURL(String specifier) {
		try {
			return new URL(specifier);
		}
		catch(MalformedURLException mue) {}
		try {
			return new File(specifier).toURI().toURL();
		}
		catch(MalformedURLException mue) {}
		return null;
	}

	public boolean canDeserializeObject(String specifier, Class<?> desiredType, ClassLoader loader) {
		return URL.class.equals(desiredType) && URLStringClassMapper.makeURL(specifier) != null;
	}

	public Object deserializeObject(String specifier, Class<?> desiredType, ClassLoader loader) {
		return URLStringClassMapper.makeURL(specifier);
	}

}
