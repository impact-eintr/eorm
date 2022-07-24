package org.apache.ibatis.reflection.property;

import java.util.Iterator;

public class PropertyTokenizer implements Iterator<PropertyTokenizer>{

	private String name;

	private final String indexName;

	private String index;

	private final String children;

	public PropertyTokenizer(String fullname) {
		int delim = fullname.indexOf('.');
		if (delim > -1) {
			name = fullname.substring(0, delim);
			children = fullname.substring(delim + 1);
		} else {
			name = fullname;
			children = null;
		}
		indexName = name;
		delim = name.indexOf('[');
		if (delim > -1) {
			index = name.substring(delim + 1, name.length() - 1);
			name = name.substring(0, delim);
		}
	}

	public String getName() {
		return name;
	}

	public String getIndex() {
		return index;
	}

	public String getIndexName() {
		return indexName;
	}

	public String getChildren() {
		return children;
	}

	public boolean hasNext() {
		return children != null;
	}

	public PropertyTokenizer next() {
		return new PropertyTokenizer(children);
	}

	public void remove() {
		throw new UnsupportedOperationException("Remove is not supported, as it has no meaning in the context of properties.");
	}

}
