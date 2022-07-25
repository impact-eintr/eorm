package org.apache.ibatis.reflection.wrapper;

public abstract class DefaultObjectWrapperFactory implements ObjectWrapper {
	@Override
	public boolean hasWrapperFor(Object object) {
		return false;
	}

	@Override
	public ObjectWrapper getWrapperFor(MetaObject metaObject, Object object) {
		throw new ReflectionException("The DefaultObjectWrapperFactory should never be called to provide an ObjectWrapper.");
	}
}
