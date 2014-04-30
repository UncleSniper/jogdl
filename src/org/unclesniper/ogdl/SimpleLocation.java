package org.unclesniper.ogdl;

public class SimpleLocation implements Location {

	private String file;

	private int line;

	public SimpleLocation(String file, int line) {
		this.file = file;
		this.line = line;
	}

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}

	public int getLine() {
		return line;
	}

	public void setLine(int line) {
		this.line = line;
	}

}
