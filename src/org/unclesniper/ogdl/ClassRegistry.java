package org.unclesniper.ogdl;

import java.util.Map;
import java.util.HashMap;

public class ClassRegistry {

	private Map<Class<?>, ClassInfo> classes = new HashMap<Class<?>, ClassInfo>();

	public ClassInfo forClass(Class<?> clazz) {
		synchronized(classes) {
			ClassInfo info = classes.get(clazz);
			if(info == null) {
				info = new ClassInfo(clazz);
				classes.put(clazz, info);
			}
			return info;
		}
	}

}
