package org.apache.ibatis.reflection.wrapper;

import org.apache.ibatis.reflection.MetaObject;

import java.util.Map;

public class MapWrapper extends BaseWrapper {

	private final Map<String, Object> map;
	public MapWrapper(MetaObject metaObject, Map<String, Object> map) {
		super(metaObject);
		this.map = map;
	}
}
