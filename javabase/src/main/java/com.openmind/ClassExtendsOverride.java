package com.openmind;

/**
 * jishuzhan
 *
 * ① 覆盖（Override）是指子类对父类方法的一种重写，只能比父类抛出更少的异常，访问权限不能比父类的小，
 * 被覆盖的方法不能是 private，否则只是在子类中重新定义了一个方法；
 * ② 重载（Overload）表示同一个类中可以有多个名称相同的方法，但这些方法的参数列表各不相同。
 *
 * 注意：在Java中，变量不能被重写。
 *
 * 构造方法必须与类名相同；
 * 构造方法没有返回类型（void 也不能有）；
 * 构造方法不能被继承、覆盖、直接调用；
 * 类定义时提供了默认的无参构造方法；
 * 构造方法可以私有，外部无法使用私有构造方法创建对象。
 *
 * @author zhoujunwen
 * @date 2019-12-20
 * @time 09:40
 * @desc
 */
class ClassExtendsOverride {
    public int x = 0;
    public static int y = 0;

    public void m() {
        System.out.println("A");
    }
}

class ClassExtOverrideB extends ClassExtendsOverride {
    public int x = 1;
    public static int y =2;

    @Override
    public void m() {
        System.out.println("B");
    }

    public static void main(String[] args) {
        ClassExtendsOverride a = new ClassExtOverrideB();
        System.out.println(a.x);  // 0
        System.out.println(a.y);  // 0
        a.m();    // B
    }
}


class A {
    public void m(A a) {
        System.out.println("AA");
    }
    public void m(D d) {
        System.out.println("AD");
    }
}
class B extends A {
    @Override
    public void m(A a) {
        System.out.println("BA");
    }
    public void m(B b) {
        System.out.println("BD");
    }

    /**
     * 第一个 BA：因为 A 的 m() 方法，被子类 B 重写了，所以输出是：BA；
     * 第二个 BA：因为 B 是 A 的子类，当调用父类 m() 方法时，发现 m() 方法被 B 类重写了，所以会调用 B 中的 m() 方法，输出就是：BA；
     * 第三个 BA：因为 C 是 B 的子类，会直接调用 B 的 m() 方法，所以输出就是：BA；
     * 第四个 AD：因为 D 是 A 的子类，所以会调用 A 的 m() 方法，所以输出就是：AD。
     *
     * @param args
     */
    public static void main(String[] args) {
        A a = new B();
        B b = new B();
        C c = new C();
        D d = new D();
        a.m(a);   // BA
        a.m(b);   // BA
        a.m(c);   // BA
        a.m(d);   // AD
    }
}
class C extends B{}
class D extends B{}
