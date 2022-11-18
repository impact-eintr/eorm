/**
 *    Copyright 2009-2016 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.type;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * References a generic type.
 *
 * @param <T> the referenced type
 * @since 3.1.0
 * @author Simone Tripodi
 */

/**
 * 用来处理泛型
 * TypeReference<T> T 是原类型 是所有TypeHAndler的父类 所有TypeHandler在构造时都会解析传入的T
 * @param <T>
 */
public abstract class TypeReference<T> {

  // 泛型类中的实际类型
  private final Type rawType;

  protected TypeReference() {
    rawType = getSuperclassTypeParameter(getClass()); // this.getClass()
  }

  /**
   * 解析出当前TypeHandler实现类能够处理的目标类型
   * @param clazz TypeHandler实现类
   * @return 该TypeHandler实现类能够处理的目标类型
   */
  Type getSuperclassTypeParameter(Class<?> clazz) {
    // 获取clazz类的带有泛型的直接父类
    Type genericSuperClass = clazz.getGenericSuperclass();
    if (genericSuperClass instanceof Class) {
      if (TypeReference.class != genericSuperClass) { // <<T>> 泛型的嵌套 解嵌套
        return getSuperclassTypeParameter(clazz.getSuperclass());
      }
      throw new TypeException("'"+getClass()+"' extends TypeReference bu miss the type parameter. Remove the extension or add a type paramter to it");
    }
    // 说明genericSuperclass是泛型类 获取泛型的第一个参数 T
    Type rawType = ((ParameterizedType)genericSuperClass).getActualTypeArguments()[0];
    if (rawType instanceof ParameterizedType) {
      // 获取参数化类型的实际参数
      rawType = ((ParameterizedType)rawType).getRawType();
    }
    System.out.println("T: "+rawType.getTypeName());
    return rawType;
  }

  public final Type getRawType() {
    return rawType;
  }

  @Override
  public String toString() {
    return rawType.toString();
  }

}
