package org.unclesniper.ogdl;

import java.util.Map;
import java.util.HashMap;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Constructor;

public class ClassInfo {

	private Class<?> clazz;

	private Map<String, Property> properties = new HashMap<String, Property>();

	private Method coalescingLoaderSetter;

	private String coalescingLoaderPropertyName;

	private Method definitionLocationSetter;

	private String definitionLocationPropertyName;

	public ClassInfo(Class<?> clazz) {
		this.clazz = clazz;
		buildProperties();
	}

	public Class<?> getSubject() {
		return clazz;
	}

	public Property getProperty(String name) {
		return properties.get(name);
	}

	public Iterable<String> getPropertyNames() {
		return properties.keySet();
	}

	public Method getCoalescingLoaderSetter() {
		return coalescingLoaderSetter;
	}

	public String getCoalescingLoaderPropertyName() {
		return coalescingLoaderPropertyName;
	}

	public Method getDefinitionLocationSetter() {
		return definitionLocationSetter;
	}

	public String getDefinitionLocationPropertyName() {
		return definitionLocationPropertyName;
	}

	private void buildProperties() {
		for(Class<?> c = clazz; c != null; c = c.getSuperclass()) {
			for(Method m : c.getDeclaredMethods()) {
				int mods = m.getModifiers();
				if(!Modifier.isPublic(mods) || Modifier.isStatic(mods))
					continue;
				String name = m.getName();
				Class<?>[] params = m.getParameterTypes();
				boolean set = name.startsWith("set");
				boolean add = name.startsWith("add");
				boolean put = name.startsWith("put");
				boolean isSetter = set && params.length == 1;
				boolean isAdder = add && params.length == 1;
				boolean isPutter = (set || put) && params.length == 2;
				if(isSetter || isAdder || isPutter) {
					String propName = Property.minisculize(name, 3);
					Property prop = properties.get(propName);
					if(prop == null) {
						prop = new Property(this, propName);
						properties.put(propName, prop);
					}
					if(isSetter) {
						prop.addSetter(new Accessor(prop, m, params[0]));
						if(params[0].equals(ClassLoader.class)
								&& m.getAnnotation(CoalescingLoaderProperty.class) != null) {
							coalescingLoaderSetter = m;
							coalescingLoaderPropertyName = propName;
						}
					}
					else if(isAdder)
						prop.addAdder(new Accessor(prop, m, params[0]));
					else {
						prop.addPutter(new Accessor(prop, m, params[0], params[1]));
						if(params[0].equals(String.class) && params[1].equals(Integer.TYPE)
								&& m.getAnnotation(ObjectDefinitionLocationProperty.class) != null) {
							definitionLocationSetter = m;
							definitionLocationPropertyName = propName;
						}
					}
				}
			}
		}
	}

	public Constructor<?> findConstructorForArguments(Object[] args) {
		if(args == null)
			args = new Object[0];
		Constructor bestCtor = null;
		Class<?>[] bestParams = null;
		for(Constructor c : clazz.getConstructors()) {
			Class<?>[] params = c.getParameterTypes();
			if(params.length != args.length)
				continue;
			boolean matches = true;
			int better = params.length, worse = params.length;
			for(int i = 0; i < params.length; ++i) {
				Class<?> argType = args[i] == null ? null : args[i].getClass();
				if(argType == null ? params[i].isPrimitive()
						: !ClassUtils.isExtendedAssignable(params[i], argType)) {
					matches = false;
					break;
				}
				if(bestParams != null) {
					boolean fromBest = ClassUtils.isExtendedAssignable(params[i], bestParams[i]);
					boolean fromThis = ClassUtils.isExtendedAssignable(bestParams[i], params[i]);
					if(fromBest) {
						if(!fromThis && i < worse)
							worse = i;
					}
					else {
						if(fromThis && i < better)
							better = i;
					}
				}
			}
			if(matches && better <= worse) {
				bestCtor = c;
				bestParams = params;
			}
		}
		return bestCtor;
	}

}
