package org.apache.ibatis.reflection;

import org.apache.ibatis.reflection.invoker.Invoker;
import org.apache.ibatis.reflection.invoker.MethodInvoker;
import org.apache.ibatis.reflection.property.PropertyNamer;

import java.lang.reflect.*;
import java.util.*;
import java.util.Map.Entry;


public class Reflector {
  // 要被反射解析的类
  private final Class<?> type;

  // 可读的属性列表，有get()方法的属性列表
  private final String[] readablePropertyNames;

  // 可写的属性列表，有set()方法的属性列表
  private final String[] writablePropertyNames;

  // set方法映射 key:attrName value:set()
  private final Map<String, Invoker> setMethods = new HashMap<>();
  // get方法映射 key:attrName value:get()
  private final Map<String, Invoker> getMethods = new HashMap<>();

  private final Map<String, Class<?>> setTypes = new HashMap<>();

  private final Map<String, Class<?>> getTypes = new HashMap<>();

  // 默认构造函数
  private Constructor<?> defaultConstructor;
  // 大小写无关的属性映射表
  private Map<String, String> caseInsensitivePropertyMap = new HashMap<>();

  // Reflector的构造方法
  public Reflector(Class<?> clazz) {
    // 要被反射解析的类
    type = clazz;
    addDefaultConstructor(clazz); // 解析所有构造函数
    addGetMethods(clazz); // 解析所有get()
    addSetMethods(clazz); // 解析所有set()
    addFields(clazz); // 解析所有属性
    // 设置可读属性
    readablePropertyNames = getMethods.keySet().toArray(new String[0]);
    // 设置可写属性
    writablePropertyNames = setMethods.keySet().toArray(new String[0]);
    // 将可读写的属性放入大小写无关的属性映射表
    for (String propName : readablePropertyNames) {
      caseInsensitivePropertyMap.put(propName.toUpperCase(Locale.ENGLISH), propName);
    }
    for (String propName : writablePropertyNames) {
      caseInsensitivePropertyMap.put(propName.toUpperCase(Locale.ENGLISH), propName);
    }
  }

  private void addDefaultConstructor(Class<?> clazz) {
    Constructor<?>[] constructors = clazz.getDeclaredConstructors();
    // 无参构造函数即为默认构造函数
    Arrays.stream(constructors).filter(constructor -> constructor.getParameterTypes().length == 0)
      .findAny().ifPresent(constructor -> this.defaultConstructor = constructor);
  }

  private void addGetMethods(Class<?> clazz) {
    Map<String, List<Method>> conflictingGetters = new HashMap<>();
    // 找出该类中的所有方法
    Method[] methods = getClassMethods(clazz);
    // 过滤出get方法
    Arrays.stream(methods).filter(m -> m.getParameterTypes().length == 0 && PropertyNamer.isGetter(m.getName())).forEach(m -> addMethodConflict(conflictingGetters, PropertyNamer.methodToProperty(m.getName()), m));
    // 如果一个属性有多个疑似get方法 reolveGetterConflicts用来找出合适的那个
    resolveGetterConflicts(conflictingGetters);
  }

  private void addSetMethods(Class<?> clazz) {
    Map<String, List<Method>> conflictingSetters = new HashMap<>();
    Method[] methods = getClassMethods(clazz);
    Arrays.stream(methods).filter(m -> m.getParameterTypes().length == 1 && PropertyNamer.isSetter(m.getName()))
            .forEach(m -> addMethodConflict(conflictingSetters, PropertyNamer.methodToProperty(m.getName()), m));
    resolveSetterConflicts(conflictingSetters);
  }

  private void addMethodConflict(Map<String, List<Method>> conflictingMethods, String name, Method method) {
    // 如果没有则创建列表，有则取出列表
    // computeIfAbsent(name, k -> new ArrayList<>()) 方法为Map中的方法：
    // 如果map中通过name索引到value，则返回value
    // 否则将name作为输入交给lambda函数，直接lambda(name),则将name:lambda(name)写入map,并返回lambda(name)
    List<Method> list = conflictingMethods.computeIfAbsent(name, k -> new ArrayList<>());
    list.add(method);
  }


  // 如果一个属性有多个getter方法 找到真正的
  private void resolveGetterConflicts(Map<String, List<Method>> conflictingGetters) {
    for (Entry<String, List<Method>> entry : conflictingGetters.entrySet()) {
      Method winner = null;
      String propName = entry.getKey();
      // 对key的多个getter进行循环
      for (Method candidate : entry.getValue()) {
        if (winner == null) {  // 初始化
          winner = candidate;
          continue;
        }
        // 比较返回类型 rTyp get()
        Class<?> winnerType = winner.getReturnType();
        Class<?> candidateType = candidate.getReturnType();
        if (candidateType.equals(winnerType)) {
          if (!boolean.class.equals(candidateType)) {
            // 不是布尔型，两个返回一样，则无法辨明到底哪个是getter
            throw new ReflectionException(
                    "Illegal overloaded getter method with ambiguous type for property "
                            + propName + " in class " + winner.getDeclaringClass()
                            + ". This breaks the JavaBeans specification and can cause unpredictable results.");
          } else if (candidate.getName().startsWith("is")) {
            winner = candidate;
          }
        } else if (candidateType.isAssignableFrom(winnerType)) {
          // winnnerType 是 candidateType的子类
        } else if (winnerType.isAssignableFrom(candidateType)) {
          //  candidateType是 winnnerType的子类
          winner = candidate;
        } else {
          throw new ReflectionException(
                  "Illegal overloaded getter method with ambiguous type for property "
                          + propName + " in class " + winner.getDeclaringClass()
                          + ". This breaks the JavaBeans specification and can cause unpredictable results.");
        }
      }
      addGetMethod(propName, winner);
    }
  }

  // 根据name和具体的method一个一个地添加getter
  private void addGetMethod(String name, Method method) {
    if (isValidPropertyName(name)) {
      getMethods.put(name, new MethodInvoker(method));
      Type returnType = TypeParameterResolver.resolveReturnType(method, type);
      // 这里调用了
      getTypes.put(name, typeToClass(returnType));
    }
  }

  private void addSetMethod(String name, Method method) {
    if (isValidPropertyName(name)) {
      setMethods.put(name, new MethodInvoker(method));
      Type[] paramTypes = TypeParameterResolver.resolveParamTypes(method, type);
      // 这里调用了
      setTypes.put(name, typeToClass(paramTypes[0]));
    }
  }

  private Class<?> typeToClass(Type src) {
    Class<?> result = null;
    if (src instanceof Class) {
      result = (Class<?>) src;
    } else if (src instanceof ParameterizedType) {
      result = (Class<?>) ((ParameterizedType)src).getRawType();
    } else if (src instanceof GenericArrayType) {
      Type componentType = ((GenericArrayType) src).getGenericComponentType();
      if (componentType instanceof Class) {
        result = Array.newInstance((Class<?>) componentType, 0).getClass();
      } else {
        Class<?> componentClass = typeToClass(componentType);
        result = Array.newInstance(componentClass, 0).getClass();
      }
    }

    if (result == null) {
      result = Object.class;
    }
    return result;
  }


  // 如果一个属性有多个setter方法 找到真正的
  private void resolveSetterConflicts(Map<String, List<Method>> conflictingSetters) {
    for (String propName : conflictingSetters.keySet()) {
      List<Method> setters = conflictingSetters.get(propName);
      // 上一步已经设置
      Class<?> getterType = getTypes.get(propName);
      Method match = null;
      ReflectionException exception = null;

      for (Method setter : setters) {
        if (setter.getParameterTypes()[0].equals(getterType)) {
          match = setter;
          break;
        }
        if (exception == null) {
          try {
            match = pickBetterSetter(match, setter, propName);
          } catch (ReflectionException e) {
            match = null;
            exception = e;
          }
        }
      }

      if (match == null) {
        throw exception;
      } else {
        addSetMethod(propName, match);
      }
    }
  }

  // 选择最佳适配 包含最多信息的子类
  private Method pickBetterSetter(Method setter1, Method setter2, String property) {
    if (setter1 == null) {
      return setter2;
    }
    Class<?> paramType1 = setter1.getParameterTypes()[0];
    Class<?> paramType2 = setter2.getParameterTypes()[0];
    if (paramType1.isAssignableFrom(paramType2)) {
      return setter2;
    } else if (paramType2.isAssignableFrom(paramType1)) {
      return setter1;
    }
    throw new ReflectionException("Ambiguous setters defined for property '" + property + "' in class '"
            + setter2.getDeclaringClass() + "' with types '" + paramType1.getName() + "' and '"
            + paramType2.getName() + "'.");
  }

  // TODO 下午继续写
  private void addFields(Class<?> clazz) {

  }

  private boolean isValidPropertyName(String name) {
    return !(name.startsWith("$") || "serialVersionUID".equals(name) || "class".equals(name));
  }

  private Method[] getClassMethods(Class<?> clazz) {
    Map<String, Method> uniqueMethods = new HashMap<>();
    Class<?> currentClass = clazz;
    while (currentClass != null && currentClass != Object.class) {
      addUniqueMethods(uniqueMethods, currentClass.getDeclaredMethods()); // 剔除桥接方法
      Class<?>[] interfaces = currentClass.getInterfaces();
      for (Class<?> anInterface : interfaces) {
        addUniqueMethods(uniqueMethods, anInterface.getMethods());
      }
      currentClass = currentClass.getSuperclass();
    }

    Collection<Method> methods = uniqueMethods.values();

    return methods.toArray(new Method[0]);
  }

  /*这里会剔除jvm添加的桥接方法
  * */
  private void addUniqueMethods(Map<String, Method> uniqueMethods, Method[] methods) {
    for (Method currentMethod : methods) {
      if (!currentMethod.isBridge()) { // jvm为泛型擦除后转换Object添加的方法
        String signature = getSignature(currentMethod);
        if (!uniqueMethods.containsKey(signature)) {
            uniqueMethods.put(signature, currentMethod);
        }
      }
    }
  }

  // 获取函数签名 rType#methodName:pType1,pType2
  private String getSignature(Method method) {
    StringBuilder sb = new StringBuilder();
    Class<?> returnType = method.getReturnType();
    if (returnType != null) {
      sb.append(returnType.getName()).append('#');
    }
    sb.append(method.getName());
    Class<?>[] parameters = method.getParameterTypes();
    for (int i = 0; i < parameters.length; i++) {
      sb.append(i == 0 ? ':' : ',').append(parameters[i].getName());
    }
    return sb.toString();
  }

}
