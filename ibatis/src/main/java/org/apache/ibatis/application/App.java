package org.apache.ibatis.application;

import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.Reflector;
import org.apache.ibatis.reflection.factory.DefaultObjectFactory;
import org.apache.ibatis.reflection.wrapper.BeanWrapper;
import org.apache.ibatis.reflection.wrapper.DefaultObjectWrapperFactory;

import java.lang.reflect.InvocationTargetException;

class internal {
  private String attr = "internal message";
}

class Test {
  private static final int attr1 = 1;
  public double attr2;

  private internal attr3;

  public Test() {}

  public int getAttr1() {return attr1;}
  //public void setAttr1(int n) { attr1 = n;}
  public double getAttr2() {return attr2;}
  public void setAttr2(double n) { attr2 = n;}

  public void func1() {
    System.out.println("this is func1");
  }
}


public class App {
	public static void main(String[] args) {
    Test t = new Test();
    MetaObject mObj = MetaObject.forObject(t, new DefaultObjectFactory(), new DefaultObjectWrapperFactory(), new DefaultReflectorFactory());

    BeanWrapper bw = new BeanWrapper(mObj, t);

    System.out.println(bw.findProperty("attr3.attr", true));

    for (String s : bw.getGetterNames()) {
      System.out.println(s);
    }
  }
}
