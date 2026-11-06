# Day 05｜前端转全栈第 5 天：写了 6 年假 class，今天见真面向对象

## 1. 面向对象基础

**面向对象思想是java的核心编程思想。**

> 💬 说实话，写了 6 年 JS，我一直觉得自己天天在写 class，面向对象这块肯定稳了。结果一翻资料才发现：ES6 的 class 只是套了个外壳，底层还是原型链——相当于我用了 6 年"假的"面向对象。从这一篇开始，来真的。

1. 什么是面向对象？

   面向对象就是一种编程范式，它将现实事物抽象为对象，对象封装属性与行为；依靠封装、继承、多态三大特性组织代码，便于大型程序维护和复用。

2. 为什么要使用面向对象编程？

   为了少写代码：很多功能别人已经写好了，我们创建这个对象，直接拿来用就行。

3. 怎么使用？

   就像之前讲的 Random 随机数类，我们只需要 new 出它的对象，再调用`nextInt`方法，就可以获取到一个随机数

```Java
Random rd = new Random();
int i = rd.nextInt(100);
System.out.println("i = " + i);
```

### 1.1 类和对象

#### 1.1.1 类(实体类)\_class

类分两种：

1. 实体类 -> 世间万物的分类
2. 测试类 -> 带main方法的类 ->为了测试是否能跑通

类的概述：

一类事物的抽象表示

实体类包含两部分：

1. 属性 -> 成员变量
2. 行为 -> 成员方法

例如：

```java
public class Person {
    //属性 -> 成员变量
    String name;
    int age;

    //行为 -> 成员方法
    public void eat(){
        System.out.println("人要吃饭");
    }

    public void drink(){
        System.out.println("人要喝水");
    }

    public void la(){
        System.out.println("人要出恭");
    }

    public void sa(){
        System.out.println("人要嘘嘘");
    }
}
```

描述动物类

```java
public class Animal {
    String brand;
    int age;
    String color;

    public void sleep(){
        System.out.println("动物要睡觉");
    }

    public void jiao(){
        System.out.println("动物会嚎叫");
    }
}
```

描述手机类

```java
public class Phone {
    String brand;
    double price;
    String color;

    public void call(String name){
        System.out.println("给"+name+"打电话");
    }

    public String sendMessage(){
        return "给朋友发短信";
    }
}
```

#### 1.1.2 对象

1. 概述：`一类事物的具体体现`
2. 使用：
   1. 导包 import (如果两个类不在同一包下)
   2. 创建对象：`类名 对象名 = new 类名()`
   3. 调用成员：成员变量、成员方法

   测试：

   ```java
   public class DemoObject {
       public static void main(String[] args) {
           Person person = new Person();
           System.out.println(person.name);
           System.out.println(person.age);

           person.name = "coder";
           person.age = 18;
           System.out.println(person.name);
           System.out.println(person.age);

           person.eat();
           person.drink();
           person.la();
           person.sa();
       }
   }
   ```

### 1.2 匿名对象

匿名对象就是没有名字的对象

`new对象的时候,没有等号左边,只有等号右边的new对象部分`

例如：

```Java
new Person();
new Person().eat();
```

- 优缺点：
  - 优点：好使用
  - 缺点：对象不能循环使用，浪费内存

### 1.3 成员变量和局部变量

| 对比维度   | 成员变量                             | 局部变量                                    |
| ---------- | ------------------------------------ | ------------------------------------------- |
| 定义位置   | 类中、方法体外                       | 方法内部，或方法的参数形参位置              |
| 初始化规则 | 自带 JVM 默认值，不赋值也能直接使用  | 无默认值，必须显式初始化后才能使用          |
| 作用范围   | 覆盖整个类，类内所有方法均可访问     | 仅在所在方法 / 代码块内部有效，出范围即失效 |
| 内存位置   | 跟随对象存在，存储在堆内存中         | 跟随方法调用存在，存储在栈内存中            |
| 生命周期   | 对象创建时诞生，对象被 GC 回收时销毁 | 方法执行入栈时诞生，方法执行结束弹栈时销毁  |

## 2. 面向对象三大特征（一） - 封装

`封装从java角度来说：将细节隐藏起来(不让外界直接使用),对外提供一套公共的接口(供外界通过这个公共的接口间接使用隐藏起来的细节)`

怎么用？

```java
1.用private修饰成员之后(相当于隐藏细节)
2.提供getxxx/setxxx方法(相当于对外提供的公共接口)
  a.setxxx:为属性赋值
  b.getxxx:获取属性值
```

例如：

```Java
public class Person {
    private String name;
    private int age;

    /**
     * 针对私有化的name提供getxxx/setxxx方法
     */
    public void setName(String xingMing) {
        name = xingMing;
    }

    public String getName() {
        return name;
    }

    public void setAge(int nianLing) {
        if (nianLing <= 0) {
            System.out.println("年龄不合法");
        } else {
            age = nianLing;
        }
    }

    public int getAge() {
        return age;
    }
}
```

上面的 Person类中，对name、age加上修饰符 private。所以不能直接在对象上获取属性，需要通过 get/set Age/Name() 来操作

## 3. 面向对象三大特征（二） - 继承

继承就是子类继承父类

1. 父类是怎么来的？

   定义多个类的时候发现我们在反复定义相同的成员，此时我们就可以定义一个父类，将重复的成员都放到父类中,其他类直接继承这个父类，就可以直接使用父类中抽取出来的非私有成员。

2. 怎么继承？

   ```java
   1.定义一个父类,编写共性成员
   2.定义一个子类,继承父类  -> 子类 extends 父类
   3.创建子类对象,调用继承的成员

   public class Employee {
       String name;
       int age;
       public void work(){
           System.out.println("员工都要工作");
       }

       private void eat(){
           System.out.println("员工都要吃饭");
       }
   }

   public class Manager extends Employee{
   }

   public class Teacher extends Employee{

   }

   public class Test01 {
       public static void main(String[] args) {
           Teacher teacher = new Teacher();
           teacher.name = "teacher";
           teacher.age = 18;
           System.out.println(teacher.name+"..."+teacher.age);
           teacher.work();
           //teacher.eat();eat方法是私有的,继承之后也用不了

           System.out.println("===================");
           Manager manager = new Manager();
           manager.name = "manager";
           manager.age = 56;
           System.out.println(manager.name+"..."+manager.age);
           manager.work();
       }
   }
   ```

> 继承前提下成员变量访问特点:看等号左边是谁,先调用谁中的成员变量,子类没有,找父类

### 3.1 父子类成员变量和方法的访问特点

#### 3.1.1 成员变量

**情况一：子类和父类中的成员变量不重名**

```java
public class Fu {
    int numFu = 100;
}
```

```java
public class Zi extends Fu{
    int numZi = 10;
}
```

```java
public class Test01 {
    public static void main(String[] args) {
        Zi zi = new Zi();
        System.out.println(zi.numZi);//自己的
        System.out.println(zi.numFu);//继承的

        System.out.println("======================");
        Fu fu = new Fu();
        System.out.println(fu.numFu);//父类自己的
        //System.out.println(fu.numZi);//不能使用子类的成员
    }
}
```

**情况二：子类和父类中的成员变量重名**

```java
public class Fu {
    int num = 100;
}
```

```java
public class Zi extends Fu {
    int num = 10;
}
```

```java
public class Test01 {
    public static void main(String[] args) {
        Zi zi = new Zi();
        System.out.println(zi.num);//子类的

        System.out.println("=================");
        Fu fu = new Fu();
        System.out.println(fu.num);//父类的

        System.out.println("=================");

        //多态
        Fu fu2 = new Zi();
        System.out.println(fu2.num);//父类的
    }
}
```

> 继承前提下成员变量访问特点:看等号左边是谁,先调用谁中的成员变量,子类没有,找父类

#### 3.1.2 成员方法

**情况一：子类和父类中的成员方法不重名**

```java
public class Fu {
   public void methodFu(){
       System.out.println("methodFu方法");
   }
}
```

```java
public class Zi extends Fu {
    public void methodZi(){
        System.out.println("methodZi方法");
    }
}
```

```java
public class Test01 {
    public static void main(String[] args) {
        Zi zi = new Zi();
        zi.methodZi();
        zi.methodFu();

        System.out.println("=================");
        Fu fu = new Fu();
        fu.methodFu();
        //fu.methodZi();不能调用子类特有的成员
    }
}
```

**情况二：子类和父类中的成员方法重名**

```java
public class Fu {
   public void method(){
       System.out.println("父类中的method方法");
   }
}
```

```java
public class Zi extends Fu {
    public void method(){
        System.out.println("子类中的method方法");
    }
}
```

```java
public class Test01 {
    public static void main(String[] args) {
        Zi zi = new Zi();
        zi.method();//子类的method

        System.out.println("================");
        Fu fu = new Fu();
        fu.method();//父类的method

        System.out.println("================");
        Fu fu2 = new Zi();
        fu2.method(); // 子类的method
    }
}
```

> 继承中成员方法访问特点:看new的是谁,先调用谁中的方法,子类没有,找父类
>
> 一句话记忆：**成员变量看等号左边，成员方法看 new 的右边**

### 3.2 方法的重写

- 概述：`子类中有一个和父类从方法名以及参数列表上一样的方法,叫做方法的重写`
- 前提：`必须有子父类继承关系`
- 如何判断方法是否为重写的方法：`在方法上加上一个注解 -> @Override`

```java
public class Fu {
    public void method(){
        System.out.println("我是父类中的method方法");
    }
}

public class Zi extends Fu{
    @Override
    public void method(){
        System.out.println("我是子类中的method方法");
    }
}

public class Test01 {
    public static void main(String[] args) {
        Zi zi = new Zi();
        zi.method();
    }
}
```

1.方法的重载:方法名相同,参数列表不同的方法-> 侧重同一个类

2.方法的重写:发生在继承中,子类中有一个和父类方法名以及参数列表一模一样的方法

> 注意事项
>
> 1. 子类方法重写父类方法，必须要保证权限大于等于父类权限。(权限修饰符)
>    public -> protected -> 默认 -> private
> 2. 子类方法重写父类方法,方法名和参数列表都要一模一样。
> 3. 私有方法不能被重写,构造方法不能被重写,静态方法也不能重写
> 4. 子类重写父类方法之后,返回值类型应该是父类方法返回值类型的子类类型,一般情况下都一样
> 5. 私有方法、静态方法可以继承但是不能被重写;构造方法不能被继承,也不能被重写

## 4. super 和 this

### 4.1 继承中构造方法的特点

1. 特点：`创建子类对象时,先初始化父类(不是说new父类对象,而是先调用父类无参构造方法)`
2. 原因：`构造方法第一行默认会有一个super(),不写也有`，`super()代表的就是父类无参构造方法`

```java
public class Fu {
    public Fu(){
        System.out.println("我是父类无参构造");
    }
}

public class Zi extends Fu{
    public Zi(){
        super();
        System.out.println("我是子类无参构造");
    }

    public Zi(int i){
        super();
        System.out.println("我是子类有参构造");
    }
}

public class Test01 {
    public static void main(String[] args) {
        Zi zi = new Zi();
        System.out.println("========");
        Zi zi1 = new Zi(10);
    }
}
```

### 4.2 super和this的具体使用

#### 4.2.1 super

1. 概述：super代表的是父类引用
2. 作用：主要调用父类成员
3. 使用：
   1. 调用父类构造方法：在子类的构造中使用；super() 调用无参构造，super(实参) 调用有参构造。
   2. 调用父类成员变量：在子类中使用；super.成员变量
   3. 调用父类成员方法：在子类中使用；super.成员方法名()

```java
public class Fu {
    int num = 10;
    public Fu(){
        System.out.println("父类无参构造");
    }

    public Fu(int num){
        System.out.println("父类有参构造");
    }

    public void method(){
        System.out.println("父类method方法");
    }
}


public class Zi extends Fu{
    int num = 100;
    public Zi(){
        super();//调用父类无参构造
        System.out.println("子类无参构造");
    }

    public Zi(int num){
        //调用父类有参构造
        super(10);
        System.out.println("子类有参构造");
    }

    public void method(){
        System.out.println(num);//子类的num
        System.out.println(super.num);//父类的num
        super.method();//调用父类的method
        System.out.println("子类method方法");
    }
}

public class Test {
    public static void main(String[] args) {
        Zi zi = new Zi();
        System.out.println("============");
        Zi zi1 = new Zi(10);
        System.out.println("============");
        zi.method();
    }
}
```

> super()或者super(实参) -> 必须要在构造第一行写

#### 4.2.2 this

1. 概述：代表的是当前对象
2. 作用：区分重名的成员变量和局部变量；调用当前对象的成员
3. 使用：
   1. 调用当前对象的构造：在本类构造中 this()、this(实参)
   2. 调用当前对象的成员变量：在本类中 this.成员变量名
   3. 调用当前对象的方法：在本类中 this.方法名()

```Java
public class Person {
    String name = "张三丰";

    public Person(){
        this("张无忌");//调用当前对象的有参构造
        //super();
        System.out.println("Person的无参构造");
    }

    public Person(String name){
        //this();//调用当前对象的无参构造
        System.out.println("Person的有参构造");
    }

    public void eat(String name){
        System.out.println(name);//张翠山
        System.out.println(this.name);
        System.out.println("人要吃饭");
        //this.drink();
    }
    public void drink(){
        System.out.println("人要喝水");
    }
}


public class Test01 {
    public static void main(String[] args) {
        Person person = new Person();
        person.eat("张翠山");
    }
}
```

> this()或者this(实参) 也必须要在构造第一行
>
> 所以,this和super就不能同时在构造方法中出现

### 4.2.3 继承的特点

1. 继承只支持单继承，不能多继承
2. 继承支持多层继承
3. 一个父类可以有多个子类

## 5. 今日学习总结

1. Java 是真·基于类的 OOP：先定义类（属性 + 行为），再 `new` 对象使用；ES6 的 class 底层还是原型，只是套了个外壳
2. 封装 = `private` 藏细节 + `getter/setter` 开口子，set 里还能顺手做合法性校验（比如年龄不能为负）
3. 继承用 `extends`，子类直接复用父类的非私有成员；只支持单继承，但可以多层继承，一个父类可以有多个子类
4. 重载 ≠ 重写：重载在同一个类里，方法名相同参数列表不同；重写在父子类之间，方法签名一模一样，用 `@Override` 验明正身
5. 访问口诀：**成员变量看等号左边，成员方法看 new 的右边**；`super` 找父类，`this` 找当前对象，且都必须写在构造方法第一行
6. 三大特征还差一个「多态」，下一篇见
