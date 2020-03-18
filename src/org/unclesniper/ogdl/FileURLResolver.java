package org.unclesniper.ogdl;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.io.FileInputStream;

public class FileURLResolver implements DirectiveTokenSink.URLResolver {

	private static final File DEFAULT_BASEDIR = new File(".").getAbsoluteFile();

	private File basedir;

	public FileURLResolver(File basedir) {
		this.basedir = basedir;
	}

	public File getBasedir() {
		return basedir;
	}

	public void setBasedir(File basedir) {
		this.basedir = basedir;
	}

	@Override
	public InputStream resolveURL(String text, int startIndex) throws IOException {
		File file = new File(basedir == null ? FileURLResolver.DEFAULT_BASEDIR : basedir,
				text.substring(startIndex));
		return new FileInputStream(file);
	}

}
