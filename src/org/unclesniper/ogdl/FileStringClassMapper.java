package org.unclesniper.ogdl;

import java.io.File;

public class FileStringClassMapper implements StringClassMapper {

	public boolean canDeserializeObject(String specifier, Class<?> desiredType, ClassLoader loader) {
		return File.class.equals(desiredType);
	}

	public Object deserializeObject(String specifier, Class<?> desiredType, ClassLoader loader) {
		return new File(FileStringClassMapper.toPlatformPath(specifier));
	}

	private static String toPlatformPath(String virtualPath) {
		StringBuilder build = new StringBuilder();
		int old = 0, pos;
		while((pos = virtualPath.indexOf('/', old)) >= 0) {
			build.append(virtualPath.substring(old, pos));
			build.append(File.separatorChar);
			old = pos + 1;
		}
		build.append(virtualPath.substring(old));
		return build.toString();
	}

}
