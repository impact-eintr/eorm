package org.apache.ibatis.reflection;

public interface ReflectorFactory {

	boolean isClassCachedEnabled();

	void setClassCacheEnabled(boolean classCacheEnabled);

	Reflector findForClass(Class<?> type);
}
