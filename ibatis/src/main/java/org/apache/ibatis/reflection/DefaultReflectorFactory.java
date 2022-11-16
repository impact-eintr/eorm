package org.apache.ibatis.reflection;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class DefaultReflectorFactory implements ReflectorFactory {

	private boolean classCacheEnabled = true;

	private final ConcurrentMap<Class<?>, Reflector> reflectorMap = new ConcurrentHashMap<>();

	public DefaultReflectorFactory() {}

	@Override
	public boolean isClassCachedEnabled() {
		return classCacheEnabled;
	}
	@Override
	public void setClassCacheEnabled(boolean classCacheEnabled) {
		this.classCacheEnabled = classCacheEnabled;
	}
	@Override
	public Reflector findForClass(Class<?> type) {
		if (classCacheEnabled) {
			// 生产入参type的反射器对象，并放入缓存
			return reflectorMap.computeIfAbsent(type, Reflector::new);
		} else {
			return new Reflector(type);
		}
	}
}