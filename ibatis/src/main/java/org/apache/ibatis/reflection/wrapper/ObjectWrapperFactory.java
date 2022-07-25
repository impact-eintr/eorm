package org.apache.ibatis.reflection.wrapper;

public interface ObjectWrapperFactory {
	boolean hasWrapperFor(Object object);

	ObjectWrapper getWrapperFor(MetaObject metaObject, Object object);
}
