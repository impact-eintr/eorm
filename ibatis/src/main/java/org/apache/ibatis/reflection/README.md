# 反射
Java反射机制主要提供了以下功能
- 在运行时判断任意一个对象所属的类;
- 在运行时构造任意一个类的对象;
- 在运行时修改任意一个对象的成员变量;
- 在运行时调用任意一个对象的方法;

于是，我们可以先通过反射获取对象的类，从而判断两个对象是否属于同一个类；然后获取对象的成员变量，轮番比较两个对象的成员变量是否一致

```java
public class ibatis {
	public static void diff(Object a, Object b) {
		Class clazzA = a.getClass(); // 从局部变量表中获取this指针 this指针中获取类指针 类指针中获取类的类对象(java/lang/Class的一个实例)
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
```

## 对象工厂子包
```java
public interface ObjectFactory {
	// 设置工厂的属性
	default void setProperties(Properties properties) {
		// NOP
	}

	// 传入一个类型 采用无参构造函数生成实例
	<T> T create(Class<T> type);

	// 传入一个目标类型、一个参数类型列表、一个参数值列表，根据参数列表找到相应的含参构造方法生成这个类型的实例
	<T> T create(Class<T> type, List<Class<?>> constructorArgTypes, List<Object> constructorArgs);

	// 判断这个类是否是集合类
	<T> boolean isCollection(Class<T> type);
}
```

```java
public class DefaultObjectFactory implements ObjectFactory, Serializable {
	private static final long serialVersionUID = -8855120656740914948L;

	public <T> T create(Class<T> type) {
		return create(type, null, null);
	}

	// class A 的有参构造函数需要2个参数 分别是 int 123 和 String "this is A"
	// 那么后面对应的参数就是 List<Class<?>>{Integer, String} List<Object>{123, "this is A"}
	public <T> T create(Class<T> type, List<Class<?>> constructorArgTypes, List<Object> constructorArgs){
		Class<?> classToCreate = resolveInterface(type);
		// 创建类型实例
		return (T) instantiateClass(classToCreate, constructorArgTypes, constructorArgs);
	}

	private  <T> T instantiateClass(Class<T> type, List<Class<?>> constructorArgTypes, List<Object> constructorArgs) {
		try {
			Constructor<T> constructor;
			if (constructorArgTypes == null || constructorArgs == null) { // 参数类型列表为空或者参数列表为空
				constructor = type.getDeclaredConstructor();
				try {
					return constructor.newInstance();
				} catch (IllegalAccessException e) {
					// 如果发生异常 则修改构造函数的访问属性后再次尝试
					if (Reflector.canControlMemberAccessible()) {
						constructor.setAccessible(true);
						return constructor.newInstance();
					} else {
						throw e;
					}
				}
			}

			// 根据入参类型查找对应的构造器
			constructor = type.getDeclaredConstructor(constructorArgTypes.toArray(new Class[constructorArgTypes.size()]));
			try {
				// 采用有参构造函数创建实例
				return constructor.newInstance(constructorArgs.toArray(new Object[constructorArgs.size()]));
			} catch (IllegalAccessException e) {
				if (Reflector.canControlMemberAccessible()) {
					// 如果发生异常，则修改构造函数的访问属性后再次尝试
					constructor.setAccessible(true);
					return constructor.newInstance(constructorArgs.toArray(new Object[constructorArgs.size()]));
				} else {
					throw e;
				}
			}
		} catch (Exception e) {
			// 收集所有的参数类型与参数值
			String argTypes = Optional.ofNullable(constructorArgTypes).
							orElseGet(Collections::emptyList).stream().
							map(String::valueOf).collect(Collectors.joining(","));
			String argValues = Optional.ofNullable(constructorArgs).
							orElseGet(Collections::emptyList).stream().
							map(String::valueOf).collect(Collectors.joining(","));
			throw new ReflectionException("Error instantiating " + type + "with invalid types (" + argTypes + ") or values ("+ argValues+"), Cause: " + e, e);
		}
	}

		// 判断要创建的目标对象的类型 即如果传入的是接口则给出它的一种实现
	protected Class<?> resolveInterface(Class<?> type) {
		Class<?> classToCreate;
		if (type == List.class || type == Collection.class || type == Iterable.class) {
			classToCreate = ArrayList.class;
		} else if (type == Map.class) {
			classToCreate = HashMap.class;
		} else if (type == SortedSet.class) {
			classToCreate = TreeSet.class;
		} else if (type == Set.class) {
			classToCreate = HashSet.class;
		} else {
			classToCreate = type;
		}
		return classToCreate;
	}


	public <T> boolean isCollection(Class<T> type) {
		return Collection.class.isAssignableFrom(type);
	}
}
```

## 执行器子包
reflection 包下的 invoker 子包是执行器子包，该子包中的类能够基于反射实现对象方法的调用和对象属性的读写

```java
public interface Invoker {
	// 方法执行调用器
	Object invoke(Object target, Object[] args) throws IllegalAccessException, InvocationTargetException;
	// 传入参数或者传出参数的类型(一个参数是入参，否则就是出参)
	Class<?> getType();
}

```

Invoker接口的三个实现分别用来处理三种不同情况。

- GetFieldInvoker：负责对象属性的读操作；
- SetFieldInvoker：负责对象属性的写操作；
- MethodInvoker：负责对象其他方法的操作。

```java
public class SetFieldInvoker implements Invoker{
	private final Field field;

	public SetFieldInvoker(Field field) {
		this.field = field;
	}

	@Override
	public Object invoke(Object target, Object[] args) throws IllegalAccessException {
		try {
			// 直接给属性赋值就可以
			field.set(target, args[0]);
		} catch (IllegalAccessException e) {
			if (Reflector.canControlMemberAccessible()) {
				field.setAccessible(true);
				field.set(target, args[0]);
			} else {
				throw e;
			}
		}
		return null;
	}

	@Override
	public Class<?> getType() {
		return field.getType();
	}
}

```

## 属性子包
reflection包下的 property子包是属性子包，该子包中的类用来完成与对象属性相关的操作

借助于属性复制器PropertyCopier，我们可以方便地将一个对象的属性复制到另一个对象中,属性复制器 PropertyCopier的属性复制工作在 copyBeanProperties方法中完成

**总的来说，就是使用一个java/lang/Class的指针，在方法区获取持有它的类，遍历这个类的所有字段，逐个构造java/lang/Reflect.Field实例，并将其Extra设置为当前字段。在使用时，在传入的类实例中使用field.Extra().Name 以及 Descriptor 查找到类实例的对应字段，然后修改其值**

```java
// 属性拷贝器
public final class PropertyCopier { // A final class is simply a class that can't be extended.
	private PropertyCopier() {
	}

	public static void copyBeanProperties(Class<?> type, Object srcBean, Object dstBean) {
		Class<?> parent = type;
		while (parent != null) { // 直到parent为Object的null超类
			// getDeclareFields()
			// 从局部变量表中获取this指针 也就是那个Class实例对象的指针
			// this指针中Extra()是持有该实例的那个类 这一步是在classLoad时完成的
			// 调用这个类的GetFields可以获取所有属性[]Field
			// Field就是一些Name Descriptor Slots...
			// 然后加载java/lang/Reflect/Field类
			// 用上面的[]Field遍历构造Reflect.Field的实例并装载到数组中
			// 注意在构造时Reflect.Field.extra被设置为了Field
			// 最终返回的就是Reflect.Field的一个数组
			final Field[] fields = parent.getDeclaredFields();
			// 循环遍历属性进行拷贝
			for (Field field : fields) {
				try {
					try {
						// field 是 java.lang.reflect.Field
						// 这里我猜测是field.Extra()获取了Field
						// 然后传入dstBean
						// 调用dstBean.SetRefVar(Field.name, Field.desc, value)
						// 这个函数将找到dstBean的对应实例字段 然后修改对应的内存
						field.set(dstBean, field.get(srcBean));
					} catch (IllegalAccessException e) {
						if (Reflector.canControlMemberAccessible()) {
							field.setAccessible(true);
							field.set(dstBean, field.get(srcBean));
						} else {
							throw e;
						}
					}
				} catch (Exception e) {
					// Nothing useful to do, will only fail on final fields, which will be ignored.
				}
			}
			parent = parent.getSuperclass();
		}
	}
}

```

copyBeanProperties方法的工作原理非常简单：通过反射获取类的所有属性，然后依次将这些属性值从源对象复制出来并赋给目标对象。但是要注意一点，该属性复制器无法完成继承得来的属性的复制，因为 getDeclaredFields方法返回的属性中不包含继承属性

PropertyNamer提供属性名称相关的操作功能，例如，通过 get、set方法的方法名找出对应的属性等。要想让 PropertyNamer 正常地发挥作用，需保证对象属性、方法的命名遵循 Java Bean的命名规范

```java
public final class PropertyNamer {
	private PropertyNamer() {}

	// 将方法名转化为属性名
	public static String methodToProperty(String name) {
		if (name.startsWith("is")) {
			name = name.substring(2);
		} else if (name.startsWith("get") || name.startsWith("set")) {
			name = name.substring(3);
		} else {
			throw new ReflectionException("Error parsing property name '" + name + "'. Didn't start with 'is', 'get' or 'set'.");
		}

		// 将方法名中属性的大小写修改正确
		if (name.length() == 1 || (name.length() > 1 && !Character.isUpperCase(name.charAt(1)))) {
			name = name.substring(0, 1).toLowerCase(Locale.ENGLISH) + name.substring(1);
		}
		return name;
	}

	public static boolean isProperty(String name) {
		return isGetter(name) || isSetter(name);
	}

	public static boolean isGetter(String name) {
		return (name.startsWith("get") && name.length() > 3) ||
						(name.startsWith("is") && name.length() > 2);
	}

	public static boolean isSetter(String name) {
		return name.startsWith("set") && name.length() > 3;
	}
}

```

PropertyTokenizer 是一个属性标记器。传入一个形如“student[sId].name”的字符串后，该标记器会将其拆分开，放入各个属性中

```java
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
```
## 对象包装器子包
reflection包下的 wrapper子包是对象包装器子包，该子包中的类使用装饰器模式对各种类型的对象（包括基本 Bean对象、集合对象、Map对象）进行进一步的封装，为其增加一些功能，使它们更易于使用。

```java
public interface ObjectWrapperFactory {
	boolean hasWrapperFor(Object object);

	ObjectWrapper getWrapperFor(MetaObject metaObject, Object object);
} 
```
- 默认实现 **不过该默认实现中并没有实现任何功能。MyBatis 也允许用户通过配置文件中的objectWrapperFactory节点来注入新的 ObjectWrapperFactory**

```java
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

```

- ObjectWrapper接口是所有对象包装器的总接口

```java
public interface ObjectWrapper {
	// get一个属性的值
	Object get(PropertyTokenizer prop);
	// set一个属性的值
	void set(PropertyTokenizer prop, Object value);
	// 找到指定属性值
	String findProperty(String name, boolean useCameCaseMapping);
	// 获得getter列表
	String[] getGetterNames();
	// 获得setter列表
	String[] getSetterNames();
	// 获得getter的类型
	String[] getGetterType(String name);
	// 获得getter的类型
	String[] getSetterType(String name);
	// 查看指定属性是否有setter
	boolean hasSetter(String name);
	// 查看指定属性是否有getter
	boolean hasGetter(String name);
	// 生成一个属性的实例
	MetaObject instantiateProPertyValue(String name, PropertyTokenizer prop,
																			ObjectFactory objectFactory);
	// 判断是否是集合
	boolean isCollection();
	// 添加元素
	void add(Object element);
	// 添加全部元素
	<E> void addAll(List<E> element);
}

```

**以 BeanWrapper为例，我们介绍一下包装器的实现。在介绍之前我们先了解 reflection包中的两个类：MetaObject类和 MetaClass类。meta 在中文中常译为“元”，在英文单词中作为词头有“涵盖”“超越”“变换”等多种含义。在这里，这三种含义都是存在的。例如，MetaObject类中涵盖了对应 Object类中的全部信息，并经过变化和拆解得到了一些更为细节的信息。因此，可以将 MetaObject类理解为一个涵盖对象（Object）中更多细节信息和功能的类，称为“元对象”。同理，MetaClass就是一个涵盖了类型（Class）中更多细节信息和功能的类，称为“元类”。**


## 反射包装类

## 异常拆包工具

## 参数名解析器

