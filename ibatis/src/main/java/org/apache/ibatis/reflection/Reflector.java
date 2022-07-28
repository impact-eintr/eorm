package org.apache.ibatis.reflection;

import org.apache.ibatis.reflection.invoker.Invoker;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;

public class Reflector {
  // 要被反射解析的类
  private final Class<?> type;

  private final String[] readablePropertyNames;

  private final String[] writablePropertyNames;

  private final Map<String, Invoker> setMethods = new HashMap<>();

  private final Map<String, Invoker> getMethods = new HashMap<>();

  private final Map<String, Class<?>> setTypes = new HashMap<>();

  private final Map<String, Class<?>> getTypes = new HashMap<>();

  private Constructor<?> defaultConstructor;

  private Map<String, String> caseInsensitivePropertyMap = new HashMap<>();

  public Reflector(Class<?> clazz) {
    // 要被反射解析的类
    type = clazz;
    // 设置默认构造器属性
    addDefaultConstructor(clazz);
    // 解析所有getter
    addGetMethods(clazz);
    // 解析所有setter
    addSetMethods(clazz);
    // 解析所有属性
    addFields(clazz);
    // 设定可读属性
    readablePropertyNames = getMethods.keySet().toArray(new String[0]);
    // 设定可写属性
    writablePropertyNames = setMethods.keySet().toArray(new String[0]);
    // 将可读或者可写属性放入大小无关的属性映射表
    for (String propName : readablePropertyNames) {
      caseInsensitivePropertyMap.put(propName.toUpperCase(Locale.ENGLISH), propName);
    }
    for (String propName : writablePropertyNames) {
      caseInsensitivePropertyMap.put(propName.toUpperCase(Locale.ENGLISH), propName);
    }
  }

  public void addDefaultConstructor(Class<?> clazz) {
    Constructor<?>[] constructors = clazz.getDeclaredConstructors();
    Arrays.stream(constructors).filter(constructor -> constructor.getParameterTypes().length == 0).findAny().ifPresent(constructor -> this.defaultConstructor = constructor);
  }

  // 找出类中的get方法
  private void addGetMethods(Class<?> clazz) {
    Map<String, List<Method>> conflictingGetters = new HashMap<>();

    Method[] methods = getClassMethods(clazz);
  }

  private Method[] getClassMethods(Class<?> clazz) {
    Map<String, Method> uniqueMethods = new HashMap<>();
    Class<?> currentClass = clazz;
    while (currentClass != null && currentClass != Object.class) {
      addUniqueMethods(uniqueMethods, currentClass.getDeclaredMethods());

     Class<?>[] interfaces = currentClass.getInterfaces();
     for (Class<?> anInterface : interfaces) {
       addUniqueMethods(uniqueMethods, anInterface.getMethods());
     }
      currentClass = currentClass.getSuperclass();
    }

    Collection<Method>  methods = uniqueMethods.values();

    return methods.toArray(new Method[0]);
  }

  private void addUniqueMethods(Map<String, Method> uniqueMethods, Method[] methods) {
    for (Method currentMethod : methods) {
     if (!currentMethod.isBridge()) {
       String signature = getSignature(currentMethod);
       if (!uniqueMethods.containsKey(signature)) {
         uniqueMethods.put(signature, currentMethod);
       }
     }
    }
  }

  private String getSignature(Method method) {

  }
















}
