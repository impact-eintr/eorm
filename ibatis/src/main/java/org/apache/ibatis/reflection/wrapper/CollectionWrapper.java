package org.apache.ibatis.reflection.wrapper;

import org.apache.ibatis.reflection.MetaObject;

import java.util.Collection;

public class CollectionWrapper implements ObjectWrapper {

	private final Collection<Object> object;
	public CollectionWrapper(MetaObject metaObject, Collection<Object> object) {
		this.object = object;
	}
}
