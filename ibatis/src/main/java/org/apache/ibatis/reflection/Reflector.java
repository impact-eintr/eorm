package org.apache.ibatis.reflection;

import org.apache.ibatis.reflection.invoker.Invoker;

import java.util.Map;

public class Reflector {
  // 要被反射解析的类
  private final Class<?> type;

  private final String[] readablePropertyNames;

  private final String[] writablePropertyNames;

  private final Map<String, Invoker>

}
