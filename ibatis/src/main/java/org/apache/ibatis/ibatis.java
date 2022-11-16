package org.apache.ibatis;

import java.lang.reflect.Field;

public class ibatis {
	public static void diff(Object a, Object b) {
		Class clazzA = a.getClass();
		Class clazzB = b.getClass();

		try {
			if (clazzA.equals(clazzB)) {
				Field fields[] = clazzA.getDeclaredFields();
				for (Field field : fields) {
					// 设置属性可以被反射访问
					field.setAccessible(true);
					Object valueA = field.get(a);
					Object valueB = field.get(b);
					if ((valueA == null && valueB != null) || valueA != null && !valueA.equals(valueB)) {
						System.out.println(field.getName()+": "+"from"+valueA+"to"+valueB);
					}
				}
			}
		} catch (Exception e) {
			System.err.println(e);
		}

	}
	public static void main(String[] arg) {
		diff("aaa", "bbb");
	}
}
