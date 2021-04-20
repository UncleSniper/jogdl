package org.unclesniper.ogdl;

import java.net.URL;
import java.io.File;
import java.util.Set;
import java.io.Reader;
import java.util.List;
import java.util.HashSet;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class Injection {

	private ClassRegistry classes;

	private ClassLoader loader;

	private Set<StringClassMapper> stringClassMappers = new HashSet<StringClassMapper>();

	private final List<TokenSinkWrapper> sinkWrappers = new LinkedList<TokenSinkWrapper>();

	public Injection(ClassRegistry registry) {
		classes = registry;
	}

	public ClassRegistry getClassRegistry() {
		return classes;
	}

	public void setClassRegistry(ClassRegistry registry) {
		classes = registry;
	}

	public ClassLoader getConstructionClassLoader() {
		return loader;
	}

	public void setConstructionClassLoader(ClassLoader loader) {
		this.loader = loader;
	}

	public Iterable<StringClassMapper> getStringClassMappers() {
		return stringClassMappers;
	}

	public void addStringClassMapper(StringClassMapper mapper) {
		if(mapper != null)
			stringClassMappers.add(mapper);
	}

	public void removeStringClassMapper(StringClassMapper mapper) {
		stringClassMappers.remove(mapper);
	}

	public void registerBuiltinStringClassMappers() {
		addStringClassMapper(new EnumStringClassMapper());
		addStringClassMapper(new ClassStringClassMapper());
		addStringClassMapper(new URLStringClassMapper());
		addStringClassMapper(new FileStringClassMapper());
		addStringClassMapper(new CharacterStringClassMapper());
	}

	public Iterable<TokenSinkWrapper> getTokenSinkWrappers() {
		return sinkWrappers;
	}

	public void addTokenSinkWrapper(TokenSinkWrapper wrapper) {
		if(wrapper != null)
			sinkWrappers.add(wrapper);
	}

	public ObjectGraphDescriptor readDescription(Reader stream) throws IOException, ObjectDescriptionException {
		return readDescription(stream, null);
	}

	public ObjectGraphDescriptor readDescription(Reader stream, String file)
			throws IOException, ObjectDescriptionException {
		BeanObjectBuilder builder = new BeanObjectBuilder(classes);
		builder.setConstructionClassLoader(loader);
		for(StringClassMapper mapper : stringClassMappers)
			builder.addStringClassMapper(mapper);
		Parser parser = new Parser(builder);
		TokenSinkWrapperChain chain = new TokenSinkWrapperChain(builder, sinkWrappers);
		TokenSink mbparser = chain.rewrapTokenSink(parser, null);
		Lexer lexer = new Lexer(mbparser);
		lexer.setFile(file);
		lexer.pushStream(stream);
		return new ObjectGraphDescriptor(builder);
	}

	public ObjectGraphDescriptor readDescription(InputStream stream)
			throws IOException, ObjectDescriptionException {
		return readDescription(stream, null, null);
	}

	public ObjectGraphDescriptor readDescription(InputStream stream, String charset, String file)
			throws IOException, ObjectDescriptionException {
		InputStreamReader reader = new InputStreamReader(stream, charset == null ? "UTF-8" : charset);
		return readDescription(reader, file);
	}

	public ObjectGraphDescriptor readDescription(File file) throws IOException, ObjectDescriptionException {
		return readDescription(file, null);
	}

	public ObjectGraphDescriptor readDescription(File file, String charset)
			throws IOException, ObjectDescriptionException {
		FileInputStream fis = new FileInputStream(file);
		try {
			return readDescription(fis, charset, file.getPath());
		}
		finally {
			fis.close();
		}
	}

	public ObjectGraphDescriptor readDescription(URL url) throws IOException, ObjectDescriptionException {
		return readDescription(url, null);
	}

	public ObjectGraphDescriptor readDescription(URL url, String charset)
			throws IOException, ObjectDescriptionException {
		InputStream is = url.openStream();
		try {
			return readDescription(is, charset, url.getPath());
		}
		finally {
			is.close();
		}
	}

	public ObjectGraphDescriptor readDescription(String path) throws IOException, ObjectDescriptionException {
		return readDescription(path, null);
	}

	public ObjectGraphDescriptor readDescription(String path, String charset)
			throws IOException, ObjectDescriptionException {
		return readDescription(new File(path), charset);
	}

}
