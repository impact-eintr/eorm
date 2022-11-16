package org.apache.ibatis.reflection.wrapper;

import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.ReflectionException;

public abstract class DefaultObjectWrapperFactory implements ObjectWrapperFactory {
	@Override
	public boolean hasWrapperFor(Object object) {
		return false;
	}

	@Override
	public ObjectWrapper getWrapperFor(MetaObject metaObject, Object object) {
		throw new ReflectionException("The DefaultObjectWrapperFactory should never be called to provide an ObjectWrapper.");
	}
}
