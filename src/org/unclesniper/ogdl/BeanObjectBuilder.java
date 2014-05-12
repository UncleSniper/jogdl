package org.unclesniper.ogdl;

import java.net.URL;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.net.URLClassLoader;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class BeanObjectBuilder implements ObjectBuilder, ObjectGraphDocument, CoalescingLibraryManager {

	private enum State {
		DEFINE_CONSTANT,
		ADD_ARGUMENT,
		SET_VALUE,
		ADD_VALUE,
		PUT_KEY,
		PUT_VALUE,
		PUT_VALUE_FOR_PENDING_KEY;
	}

	private static class ConstantInterdependency {

		final String definition;

		final Location location;

		ConstantInterdependency(String definition, Location location) {
			this.definition = definition;
			this.location = location;
		}

	}

	private static class PendingResolution {

		final String constant;

		final ResolutionSink sink;

		final Location location;

		PendingResolution(String constant, ResolutionSink sink, Location location) {
			this.constant = constant;
			this.sink = sink;
			this.location = location;
		}

	}

	private static abstract class AccessorDispatcher {

		protected final Object base;

		protected final Property property;

		protected final Location location;

		AccessorDispatcher(Object base, Property property, Location location) {
			this.base = base;
			this.property = property;
			this.location = location;
		}

	}

	private static class BindingResolver extends AccessorDispatcher {

		private static final int HAVE_KEY   = 01;
		private static final int HAVE_VALUE = 02;
		private static final int HAVE_BOTH  = 03;

		private int have;

		private Object key;

		private Object value;

		private final AccessorDispatchInfo info;

		BindingResolver(Object base, Property property, Location location, AccessorDispatchInfo info) {
			super(base, property, location);
			this.info = info;
		}

		void setKey(Object key) throws PropertyInjectionException {
			this.key = key;
			if((have |= BindingResolver.HAVE_KEY) == BindingResolver.HAVE_BOTH)
				BeanObjectBuilder.dispatchAccessor(Accessor.Type.PUTTER, base, property, key, value, location, info);
		}

		void setValue(Object value) throws PropertyInjectionException {
			this.value = value;
			if((have |= BindingResolver.HAVE_VALUE) == BindingResolver.HAVE_BOTH)
				BeanObjectBuilder.dispatchAccessor(Accessor.Type.PUTTER, base, property, key, value, location, info);
		}

	}

	private static class BindingKeyResolver implements ResolutionSink {

		private final BindingResolver resolver;

		BindingKeyResolver(BindingResolver resolver) {
			this.resolver = resolver;
		}

		public void setPendingObject(Object object) throws PropertyInjectionException {
			resolver.setKey(object);
		}

	}

	private static class BindingValueResolver implements ResolutionSink {

		private final BindingResolver resolver;

		BindingValueResolver(BindingResolver resolver) {
			this.resolver = resolver;
		}

		public void setPendingObject(Object object) throws PropertyInjectionException {
			resolver.setValue(object);
		}

	}

	private static class AccessorResolver extends AccessorDispatcher implements ResolutionSink {

		private final Accessor.Type access;

		private final AccessorDispatchInfo info;

		AccessorResolver(Accessor.Type access, Object base, Property property, Location location,
				AccessorDispatchInfo info) {
			super(base, property, location);
			this.access = access;
			this.info = info;
		}

		public void setPendingObject(Object object) throws PropertyInjectionException {
			BeanObjectBuilder.dispatchAccessor(access, base, property, null, object, location, info);
		}

	}

	private static class AccessorDispatchInfo {

		final Iterable<StringClassMapper> stringClassMappers;

		final ClassLoader loader;

		final CoalescingLibraryManager libraryManager;

		AccessorDispatchInfo(Iterable<StringClassMapper> stringClassMappers, ClassLoader loader,
				CoalescingLibraryManager libraryManager) {
			this.stringClassMappers = stringClassMappers;
			this.loader = loader;
			this.libraryManager = libraryManager;
		}

	}

	private static final Object[] OBJECT_ARRAY_TEMPLATE = new Object[0];

	private static final URL[] URL_ARRAY_TEMPLATE = new URL[0];

	private ClassRegistry classes;

	private ClassLoader loader;

	private Map<String, Object> constants = new HashMap<String, Object>();

	private Object root;

	private Deque<State> states = new LinkedList<State>();

	private String constName;

	private Deque<List<Object>> arguments = new LinkedList<List<Object>>();

	private Deque<ClassInfo> types = new LinkedList<ClassInfo>();

	private Deque<Object> objects = new LinkedList<Object>();

	private Deque<Property> properties = new LinkedList<Property>();

	private Set<PendingResolution> resolutions = new HashSet<PendingResolution>();

	private Map<String, ConstantInterdependency> constInterdeps = new HashMap<String, ConstantInterdependency>();

	private Deque<BindingResolver> bindingResolvers = new LinkedList<BindingResolver>();

	private Set<StringClassMapper> stringClassMappers = new HashSet<StringClassMapper>();

	private List<URL> coalescingLibraries;

	private boolean coalescingLoaderTainted;

	private ClassLoader coalescingLoader;

	public BeanObjectBuilder(ClassRegistry classes) {
		this.classes = classes;
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

	public Object getRootObject() {
		return root;
	}

	public Iterable<String> getNamedObjects() {
		return constants.keySet();
	}

	public boolean hasNamedObject(String name) {
		return constants.containsKey(name);
	}

	public Object getNamedObject(String name) {
		return constants.get(name);
	}

	public void addCoalescingLibrary(URL library) {
		if(library != null) {
			if(coalescingLibraries == null)
				coalescingLibraries = new LinkedList<URL>();
			coalescingLibraries.add(library);
			coalescingLoaderTainted = true;
		}
	}

	public ClassLoader getCoalescingLoader() {
		ClassLoader parent = loader == null ? BeanObjectBuilder.class.getClassLoader() : loader;
		if(coalescingLoaderTainted) {
			URL[] urls = coalescingLibraries.toArray(BeanObjectBuilder.URL_ARRAY_TEMPLATE);
			coalescingLoader = new URLClassLoader(urls, parent);
			return coalescingLoader;
		}
		return coalescingLoader == null ? parent : coalescingLoader;
	}

	public void defineConstant(String name, Location location) {
		constName = name;
		states.addLast(State.DEFINE_CONSTANT);
	}

	public void newInt(String value, Location location) throws ObjectConstructionException {
		long l = Long.parseLong(value);
		int i = (int)l;
		if((long)i == l)
			popObject(i, location);
		else
			popObject(l, location);
	}

	public void newFloat(String value, Location location) throws ObjectConstructionException {
		double d = Double.parseDouble(value);
		float f = (float)d;
		if((double)f == d)
			popObject(f, location);
		else
			popObject(d, location);
	}

	public void newString(String value, Location location) throws ObjectConstructionException {
		popObject(value, location);
	}

	public void newBoolean(boolean value, Location location) throws ObjectConstructionException {
		popObject(value, location);
	}

	public void referenceConstant(String name, Location location) throws ObjectConstructionException {
		if(constants.containsKey(name)) {
			popObject(constants.get(name), location);
			return;
		}
		if(states.isEmpty())
			throw new UndefinedConstantException(name, location);
		State state = states.removeLast();
		BindingResolver resolver;
		Object key;
		switch(state) {
			case DEFINE_CONSTANT:
				if(constants.containsKey(constName) || constInterdeps.containsKey(constName))
					throw new RedefinedConstantException(constName, location);
				constInterdeps.put(constName, new ConstantInterdependency(name, location));
				constName = null;
				break;
			case ADD_ARGUMENT:
				throw new UndefinedConstantException(name, location);
			case SET_VALUE:
				resolutions.add(new PendingResolution(name, new AccessorResolver(Accessor.Type.SETTER,
						objects.getLast(), properties.removeLast(), location,
						new AccessorDispatchInfo(stringClassMappers, getCoalescingLoader(), this)), location));
				break;
			case ADD_VALUE:
				resolutions.add(new PendingResolution(name, new AccessorResolver(Accessor.Type.ADDER,
						objects.getLast(), properties.removeLast(), location,
						new AccessorDispatchInfo(stringClassMappers, getCoalescingLoader(), this)), location));
				break;
			case PUT_KEY:
				resolver = new BindingResolver(objects.getLast(), properties.removeLast(), location,
						new AccessorDispatchInfo(stringClassMappers, getCoalescingLoader(), this));
				resolutions.add(new PendingResolution(name, new BindingKeyResolver(resolver), location));
				bindingResolvers.addLast(resolver);
				states.addLast(State.PUT_VALUE_FOR_PENDING_KEY);
				break;
			case PUT_VALUE:
				key = objects.removeLast();
				resolver = new BindingResolver(objects.getLast(), properties.removeLast(), location,
						new AccessorDispatchInfo(stringClassMappers, getCoalescingLoader(), this));
				resolutions.add(new PendingResolution(name, new BindingValueResolver(resolver), location));
				resolver.setKey(key);
				break;
			case PUT_VALUE_FOR_PENDING_KEY:
				resolutions.add(new PendingResolution(name,
						new BindingValueResolver(bindingResolvers.removeLast()), location));
				break;
			default:
				throw new AssertionError("Unrecognized state: " + state.name());
		}
	}

	public void newObject(TypeSpecifier type) throws ObjectCreationException {
		Class<?> clazz;
		try {
			clazz = getCoalescingLoader().loadClass(type.getJavaneseName());
		}
		catch(ClassNotFoundException cnfe) {
			throw new ObjectCreationException(type.getName(), type.getLocation(), cnfe);
		}
		ClassInfo info = classes.forClass(clazz);
		types.addLast(info);
		arguments.addLast(new LinkedList<Object>());
		states.addLast(State.ADD_ARGUMENT);
	}

	public void endConstruction(Location location) throws ObjectCreationException {
		if(states.isEmpty() || states.getLast() != State.ADD_ARGUMENT)
			throw new IllegalStateException("Call to endConstruction() not within scope of newObject()");
		states.removeLast();
		ClassInfo type = types.removeLast();
		Object[] args = arguments.removeLast().toArray(BeanObjectBuilder.OBJECT_ARRAY_TEMPLATE);
		Constructor ctor = type.findConstructorForArguments(args);
		if(ctor == null)
			throw new ObjectCreationException(type.getSubject().getName(),
					"Class '" + type.getSubject().getName() + "' does not exhibit a constructor matching "
					+ "the requested argument types", location);
		Object obj;
		try {
			obj = ctor.newInstance(args);
		}
		catch(InstantiationException ie) {
			throw new ObjectCreationException(type.getSubject().getName(),
					"Failed to instantiate class '" + type.getSubject().getName(), location, ie);
		}
		catch(IllegalAccessException iae) {
			throw new ObjectCreationException(type.getSubject().getName(),
					"Failed to instantiate class '" + type.getSubject().getName(), location, iae);
		}
		catch(InvocationTargetException ite) {
			throw new ObjectCreationException(type.getSubject().getName(),
					"Failed to instantiate class '" + type.getSubject().getName(), location, ite.getCause());
		}
		objects.addLast(obj);
	}

	public void setProperty(String property, Location location) throws NoSuchPropertyException {
		pushProperty(property, location);
		states.addLast(State.SET_VALUE);
	}

	public void addListElement(String property, Location location) throws NoSuchPropertyException {
		pushProperty(property, location);
		states.addLast(State.ADD_VALUE);
	}

	public void addMapBinding(String property, Location location) throws NoSuchPropertyException {
		pushProperty(property, location);
		states.addLast(State.PUT_KEY);
	}

	private void pushProperty(String property, Location location) throws NoSuchPropertyException {
		ClassInfo type = classes.forClass(objects.getLast().getClass());
		Property prop = type.getProperty(property);
		if(prop == null)
			throw new NoSuchPropertyException(type.getSubject(), property, location);
		properties.addLast(prop);
	}

	public void endObject(Location location) throws ObjectConstructionException {
		Object obj = objects.removeLast();
		getCoalescingLoader();
		if(coalescingLoader != null)
			injectCoalescingLoader(obj, location);
		popObject(obj, location);
	}

	private void popObject(Object object, Location location) throws ObjectConstructionException {
		if(states.isEmpty()) {
			root = object;
			resolveConstantReferences();
			return;
		}
		State state = states.removeLast();
		switch(state) {
			case DEFINE_CONSTANT:
				if(constants.containsKey(constName) || constInterdeps.containsKey(constName))
					throw new RedefinedConstantException(constName, location);
				constants.put(constName, object);
				constName = null;
				break;
			case ADD_ARGUMENT:
				arguments.getLast().add(object);
				states.addLast(State.ADD_ARGUMENT);
				break;
			case SET_VALUE:
				dispatchAccessor(Accessor.Type.SETTER, object, location);
				break;
			case ADD_VALUE:
				dispatchAccessor(Accessor.Type.ADDER, object, location);
				break;
			case PUT_KEY:
				objects.addLast(object);
				states.addLast(State.PUT_VALUE);
				break;
			case PUT_VALUE:
				dispatchAccessor(Accessor.Type.PUTTER, object, location);
				break;
			case PUT_VALUE_FOR_PENDING_KEY:
				bindingResolvers.removeLast().setValue(object);
				break;
			default:
				throw new AssertionError("Unrecognized state: " + state.name());
		}
	}

	private void dispatchAccessor(Accessor.Type access, Object object, Location location)
			throws PropertyInjectionException {
		Object key = access == Accessor.Type.PUTTER ? objects.removeLast() : null;
		Object base = objects.getLast();
		Property prop = properties.removeLast();
		BeanObjectBuilder.dispatchAccessor(access, base, prop, key, object, location,
				new AccessorDispatchInfo(stringClassMappers, getCoalescingLoader(), this));
	}

	private static void dispatchAccessor(Accessor.Type access, Object base, Property property,
			Object key, Object value, Location location, AccessorDispatchInfo info)
			throws PropertyInjectionException {
		Accessor a;
		switch(access) {
			case SETTER:
				a = property.findSetterForValue(value);
				break;
			case ADDER:
				a = property.findAdderForValue(value);
				break;
			case PUTTER:
				a = property.findPutterForBinding(key, value);
				break;
			default:
				throw new AssertionError("Unrecognized accessor type: " + access.name());
		}
		if(a == null && value != null && value.getClass().equals(String.class)) {
			Property.MappingAccessor ma = null;
			String specifier = (String)value;
			switch(access) {
				case SETTER:
					ma = property.findMappingSetterForValue(specifier, info.stringClassMappers, info.loader);
					break;
				case ADDER:
					ma = property.findMappingAdderForValue(specifier, info.stringClassMappers, info.loader);
					break;
			}
			if(ma != null) {
				a = ma.accessor;
				value = ma.mapper.deserializeObject(specifier, a.getValueType(), info.loader);
			}
		}
		if(a == null)
			throw new PropertyInjectionException(base.getClass(), property.getName(),
					"Class '" + base.getClass().getName() + "' does not exhibit a " + access.name().toLowerCase()
					+ " for property '" + property.getName() + '\'', location);
		Method method = a.getMethod();
		try {
			if(access == Accessor.Type.PUTTER)
				method.invoke(base, key, value);
			else
				method.invoke(base, value);
		}
		catch(IllegalAccessException iae) {
			throw new PropertyInjectionException(base.getClass(), property.getName(), location, iae);
		}
		catch(InvocationTargetException ite) {
			throw new PropertyInjectionException(base.getClass(), property.getName(), location, ite);
		}
		if(access == Accessor.Type.ADDER && info.libraryManager != null && URL.class.equals(a.getValueType())
				&& method.getAnnotation(CoalescingLibraryProperty.class) != null)
			info.libraryManager.addCoalescingLibrary((URL)value);
	}

	private void injectCoalescingLoader(Object base, Location location) throws PropertyInjectionException {
		ClassInfo info = classes.forClass(base.getClass());
		Method setter = info.getCoalescingLoaderSetter();
		if(setter == null)
			return;
		try {
			setter.invoke(base, coalescingLoader);
		}
		catch(IllegalAccessException iae) {
			throw new PropertyInjectionException(base.getClass(), info.getCoalescingLoaderPropertyName(),
					location, iae);
		}
		catch(InvocationTargetException ite) {
			throw new PropertyInjectionException(base.getClass(), info.getCoalescingLoaderPropertyName(),
					location, ite);
		}
	}

	private void resolveConstantReferences() throws ObjectConstructionException {
		Set<String> ariadne = new HashSet<String>();
		for(Map.Entry<String, ConstantInterdependency> entry : constInterdeps.entrySet())
			resolveConstantInterdependency(entry.getKey(), entry.getValue().location, ariadne);
		for(PendingResolution pending : resolutions) {
			if(!constants.containsKey(pending.constant))
				throw new UndefinedConstantException(pending.constant, pending.location);
			pending.sink.setPendingObject(constants.get(pending.constant));
		}
	}

	private Object resolveConstantInterdependency(String name, Location location, Set<String> ariadne)
			throws UndefinedConstantException, CyclicConstantDependencyException {
		if(constants.containsKey(name))
			return constants.get(name);
		if(ariadne.contains(name))
			throw new CyclicConstantDependencyException(name, location);
		ConstantInterdependency dep = constInterdeps.get(name);
		if(dep == null)
			throw new UndefinedConstantException(name, location);
		ariadne.add(name);
		Object object = resolveConstantInterdependency(dep.definition, dep.location, ariadne);
		ariadne.remove(name);
		constants.put(name, object);
		return object;
	}

}
