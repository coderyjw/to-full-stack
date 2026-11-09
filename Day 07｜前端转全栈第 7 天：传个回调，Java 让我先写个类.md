# Day 07｜前端转全栈第 7 天：传个回调，Java 让我先写个类

本来以为是收尾的轻松局，结果被匿名内部类拦住了：在 JS 里传个回调，是一行箭头函数的事；在 Java 里，我得先写一个类。

昨天说要把多态剩下的边角料扫干净：权限修饰符、`final`。以为就是两个「背一下就行」的小知识点，结果越往后越不像边角料——代码块、内部类、匿名内部类全塞在今天了。

其中还有一条挺反直觉的：**`final` 修饰一个对象，锁住的是「这个变量指向哪儿」，不是「里面的东西不许动」。** 跟 JS 的 `const` 一模一样，别被「最终的」三个字骗了。

## 1. 权限修饰符

Java 提供了四种访问权限，用不同的权限修饰符修饰时，被修饰的内容能被别人看到的范围也不一样。

| 修饰符      | 说明                                                              |
| ----------- | ----------------------------------------------------------------- |
| `public`    | 公共的，最高权限。被 public 修饰的成员，在哪里都能访问            |
| `protected` | 受保护的                                                          |
| `default`   | 默认的。**不写权限修饰符就是默认权限，不能真的把 default 写出来** |
| `private`   | 私有的，只能在自己的类中直接访问                                  |

### 1.1 不同权限的访问能力

| 访问修饰符  | 同类 | 同包不同类 | 不同包子类 | 不同包非子类 |
| ----------- | ---- | ---------- | ---------- | ------------ |
| `public`    | ✅   | ✅         | ✅         | ✅           |
| `protected` | ✅   | ✅         | ✅         | ❌           |
| `default`   | ✅   | ✅         | ❌         | ❌           |
| `private`   | ✅   | ❌         | ❌         | ❌           |

**public 具有最大权限，private 有最小权限。**

这张表其实有规律，我是这么记的：

- `private`：只认自己这个类
- `default`：自己 + 同一个包的人
- `protected`：在 default 基础上，再多认一个「别人家的儿子」（不同包的子类）
- `public`：谁来都行

从「最封闭」往「最开放」逐层加人，`protected` 正好卡在中间那一格。

### 1.2 权限该怎么选

编写代码时，如果没有特殊的考虑，建议这样使用权限：

1. 属性用 `private`（封装思想）
2. 构造用 `public`（便于 new 对象）
3. 方法用 `public`（便于调用）

### 1.3 跟前端最大的差别

TS 里也有 `private` / `public` / `protected`，但它是**编译期的一次性检查**：类型擦除之后，运行时的 JS 对象上照样拿得到。

```ts
class Person {
  private name = "jack";
}
new Person()["name"]; // TS 会报错，但运行时真能拿到
```

Java 的 `private` 不是「贴个封条」，是语言 + JVM 层面的硬限制，编译期就过不去。想绕过只有反射，那属于明知道在干坏事。

从「提示」到「规定」，这是我今天感受最深的一次。

## 2. final 关键字

1. 概述：代表的是最终的
2. 使用范围：

- 修饰类
- 修饰方法
- 修饰对象
- 修饰局部变量
- 修饰成员变量

### 2.1 final 修饰类

格式：

```java
public final class 类名 {}
```

特点：**不能被继承。**

```java
public final class Animal {
}

public class Dog /* extends Animal */ {
    // 编译报错：cannot inherit from final Animal
}
```

### 2.2 final 修饰方法

格式：

```java
修饰符 final 返回值类型 方法名(形参) {
    方法体
    return 结果;
}
```

特点：

1. 不能被重写
2. **不能和 abstract 一起使用**

```java
public abstract class Animal {
    public final void eat() {
    }

    // public final abstract void drink();  // 报错，final 和 abstract 互斥
}
```

```java
public class Dog extends Animal {
//  @Override
//  public void eat() {
//      System.out.println("狗吃骨头");
//  }
    // 报错：'eat()' cannot override 'eat()' in 'Animal'; overridden method is final
}
```

`final` 和 `abstract` 为什么互斥？挺好理解：

- `abstract` 说「这个方法我不写，必须由子类实现」
- `final` 说「这个方法就长这样，谁都不许改」

一个要求你改，一个禁止你改，凑一起就是这个方法永远没有方法体。编译器当然不让。

### 2.3 final 修饰局部变量

格式：

```java
final 数据类型 变量名 = 值;
```

特点：不能被二次赋值。

```java
public class Test01 {
    public static void main(String[] args) {
        final int i = 10;
        // i = 20;   // 报错：cannot assign a value to final variable 'i'
    }
}
```

跟 `const` 没有任何差别，闭着眼写就行。

### 2.4 final 修饰对象

格式：

```java
final 类名 对象名 = new 类名();
```

特点：**地址值不能改变，但是属性值可以改变。**

```java
public class Person {
    private String name;
    private int age;

    public Person() {
    }

    public Person(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}
```

```java
public class Test02 {
    public static void main(String[] args) {
        final Person p1 = new Person("jack", 20);
        System.out.println(p1);              // 打印的是地址值

        // p1 = new Person("rose", 18);      // 报错：地址值不能改
        // System.out.println(p1);

        p1.setName("rose");                  // 属性值能改
        System.out.println(p1.getName());
    }
}
```

> 一句话记住：`final` 锁的是「这个变量指向哪」，不是「指向的东西里装了什么」。

跟 JS 的 `const` 修对象完全一个道理：

```js
const p = { name: "jack" };
p.name = "rose"; // 可以
p = { name: "rose" }; // 报错
```

顺便一个对照：前端想真把内容也锁死，有 `Object.freeze()`；Java 没有「冻结实例」这种机制，只能靠「字段全 private + 不提供 setter」。Java 在封装上明显更逼你自己动手。

### 2.5 final 修饰成员变量

格式：

```java
final 数据类型 变量名 = 值;
```

特点：

1. 被 final 修饰的成员变量不能被二次赋值
2. 被 final 修饰的成员变量必须手动赋值

```java
public class Student {
    final String name = "张三";

    public Student() {
    }

/*
    public Student(String name) {
        this.name = name;   // 报错：不能给 final 变量二次赋值
    }

    public void setName(String name) {
        this.name = name;   // 同样报错
    }
*/

    public String getName() {
        return name;
    }
}
```

这里有个坑值得单独说：`final` 成员变量**不是只能写在声明处**。它的赋值时机有三种，只要「赋上一次」就合法：

1. 声明的时候直接赋值
2. 在构造方法里赋值（每个构造各赋一次，保证创建对象时被赋上）
3. 在初始化代码块里赋值

上面那个报错，是因为 `name` 已经在声明处赋成 `"张三"` 了，第二次赋值才算越界。把声明处的初始值删掉，构造里赋值反而完全合法：

```java
public class Student {
    final String name;      // 只声明，不赋值

    public Student(String name) {
        this.name = name;   // 合法：这是它唯一一次赋值
    }
}
```

所以「必须手动赋值」的准确意思是：**不是「立刻赋值」，是「对象创建完成之前，必须被赋过一次」。** 这条正好跟今天后面讲的构造代码块、静态代码块连上了。

### 2.6 final + static = 常量

这两个一组合，就是我们平时说的常量：

```java
public static final int MAX_COUNT = 100;
```

- `static`：属于类，不用 new 就能用
- `final`：赋值之后不许改
- 命名习惯：全大写 + 下划线分隔

还记得 Day 06 接口里那个「常量」吗？接口的成员变量默认就是 `public static final`，所以接口里写 `int NUM = 10;` 就等于写了个常量。今天这条算是把昨天那个点补上了。

## 3. 代码块

### 3.1 构造代码块

格式：

```java
{
    代码
}
```

执行特点：**优先于构造方法执行，而且每 new 一次就会执行一次。**

```java
public class Person {
    public Person() {
        System.out.println("构造方法");
    }

    // 构造代码块
    {
        System.out.println("构造代码块");
    }
}
```

```java
public class Test01 {
    public static void main(String[] args) {
        Person person = new Person();
        Person person1 = new Person();
    }
}
```

输出是四行，交替着来：

```
构造代码块
构造方法
构造代码块
构造方法
```

所有构造方法共用的初始化逻辑，塞进构造代码块，就不必在每个构造里复制一遍。前端类比的话，比较像「每个构造函数开头那几行重复的 `this.xxx = xxx`」。

### 3.2 静态代码块

格式：

```java
static {
    代码
}
```

执行特点：**优先于构造代码块以及构造方法执行，且只执行一次。**

```java
public class Person {
    public Person() {
        System.out.println("构造方法");
    }

    // 构造代码块
    {
        System.out.println("构造代码块");
    }

    // 静态代码块
    static {
        System.out.println("静态代码块");
    }
}
```

```java
public class Test01 {
    public static void main(String[] args) {
        Person person = new Person();
        Person person1 = new Person();
    }
}
```

输出：

```
静态代码块
构造代码块
构造方法
构造代码块
构造方法
```

按执行优先级来看：**静态代码块 > 构造代码块 > 构造方法**。静态代码块只在类第一次被加载时跑一次，后面 new 多少个对象都不再执行。

> 如果有一些数据需要先初始化，而且只需要初始化一次、反复使用，那么这些数据就可以放到静态代码块里初始化。

这一点前端同学会觉得很亲切：模块顶层写的那段初始化代码，就是这个效果——模块被 import 一次，代码只跑一次。

## 4. 内部类

### 4.1 静态成员内部类

格式：直接在定义内部类的时候加上 `static` 关键字。

```java
public class A {
    static class B {
    }
}
```

注意：

1. 内部类中可以定义属性、方法、构造等
2. 静态内部类可以被 `final` 或者 `abstract` 修饰
   - 被 `final` 修饰：不能被继承
   - 被 `abstract` 修饰：不能 new
3. 静态内部类不能调用外部的非静态成员
4. 内部类还可以被四种权限修饰符修饰

调用静态内部类成员的方式：

```java
外部类.内部类 对象名 = new 外部类.内部类();
```

```java
public class Person {
    public void eat() {
        System.out.println("人要干饭");

        // jump();

        // new Heart().jump();
    }

    /**
     * 静态成员内部类
     */
    static class Heart {
        public void jump() {
            System.out.println("心脏咣咣咣地跳动");
            // new Person().eat();
        }
    }
}
```

```java
public class Test01 {
    public static void main(String[] args) {
        Person.Heart heart = new Person.Heart();
        heart.jump();
    }
}
```

注意看 `new Person.Heart()` —— 静态内部类不需要外部类对象，直接就能 new。这也是它最实用的一点：想给某个类配个「只属于它的小结构体」，又不希望外面随便用，就塞成静态内部类。

### 4.2 非静态成员内部类

格式：

```java
public class 类名 {
    class 类名 {
    }
}
```

注意：

1. 内部类中可以定义属性、方法、构造等
2. 非静态内部类可以被 `final` 或者 `abstract` 修饰
   - 被 `final` 修饰：不能被继承
   - 被 `abstract` 修饰：不能 new
3. 内部类还可以被四种权限修饰符修饰
4. 非静态内部类需要先有外部类对象，才能被创建

调用非静态成员内部类：

```java
外部类.内部类 对象名 = new 外部类().new 内部类();
```

```java
public class Person {
    public void eat() {
        System.out.println("人要干饭");

        // jump();

        // new Heart().jump();
    }

    /**
     * 非静态成员内部类
     */
    class Heart {
        public void jump() {
            System.out.println("心脏咣咣咣地跳动");
            // new Person().eat();
        }
    }
}
```

```java
public class Test01 {
    public static void main(String[] args) {
        Person.Heart heart = new Person().new Heart();
        heart.jump();
    }
}
```

`new Person().new Heart()` 这个写法第一次见会愣一下，但它其实很讲道理：非静态内部类天生挂着一个「外部类实例」的引用，所以你得先交出这个实例。

### 4.3 变量重名怎么区分

外部类成员变量、内部类成员变量、内部类局部变量重名时，靠 `this` 的层级区分：

```java
public class Student {
    String name = "张三";        // 外部类成员变量

    class Heart {
        String name = "李四";    // 内部类成员变量

        public void dispaly() {
            String name = "王五";                  // 内部类局部变量
            System.out.println(name);              // 王五
            System.out.println(this.name);         // 李四
            System.out.println(Student.this.name); // 张三
        }
    }
}
```

三层名字，三层写法：直接写是局部，`this.` 是内部类成员，`外部类名.this.` 是外部类成员。前面 `super` / `this` 那套「就近原则」在这儿又续上了。

### 4.4 局部内部类

可以定义在方法中、代码块中、构造方法中。

```java
public class Person {
    public void eat() {
        /**
         * 局部内部类
         */
        class Heart {
            public void jump() {
                System.out.println("跳跳跳跳跳跳");
            }
        }

        Heart heart = new Heart();
        heart.jump();
    }
}
```

```java
public class Test01 {
    public static void main(String[] args) {
        Person person = new Person();
        person.eat();
    }
}
```

局部内部类的活动范围只有所在方法内部，出了方法就用不了——跟函数里声明的局部变量一个待遇。

### 4.5 匿名内部类（重点）

匿名内部类是局部内部类的一种。

如果定义局部内部类的时候不显式给它一个名字，这个内部类就叫「匿名内部类」；同时它也会作为某个接口的实现类来用。

#### 4.5.1 基本使用

格式一：利用有名对象去创建匿名内部类的对象

```java
接口名 对象名 = new 接口() {
    重写方法() {}
};
对象名.重写方法名();
```

格式二：利用匿名对象去创建匿名内部类的对象

```java
new 接口() {
    重写方法() {}
}.重写的方法名();
```

使用场景：如果只是想简单地实现一下接口中的某个抽象方法，然后直接调用，正常要四步——

1. 创建实现类
2. 实现接口
3. 重写抽象方法
4. 创建实现类对象，调用重写方法

没必要这么麻烦，用匿名内部类创建对象，一个格式就把上面四步全包了。

```java
public interface USB {
    void open();
    // void close();
}
```

```java
public class Test01 {
    public static void main(String[] args) {
        /*
         * 格式1：利用有名对象创建匿名内部类对象
         *
         * USB usb = new Mouse();
         * usb.open();
         */
        USB usb = new USB() {
            @Override
            public void open() {
                System.out.println("USB打开了");
            }

        /*  @Override
            public void close() {
                System.out.println("USB关闭了");
            }*/
        };
        usb.open();
        // usb.close();

        System.out.println("==================");

        /*
         * 格式2：利用匿名对象创建匿名内部类对象
         *
         * new Mouse().open();
         */
        new USB() {
            @Override
            public void open() {
                System.out.println("USB打开了");
            }

        /*  @Override
            public void close() {
                System.out.println("USB关闭了");
            }*/
        }.open();
    }
}
```

写起来还是有点笨重，但这个「就地实现、只活一次」的感觉，前端其实天天在写：

```js
// JS：传一个对象进去，就地实现
run({
  open() {
    console.log("USB打开了");
  },
});
```

Java 那坨 `new USB() { ... }` 就是同一个意图，只是必须把类型写全，还要用 `@Override` 标一下。

#### 4.5.2 当参数传递

```java
public interface USB {
    void open();
}
```

```java
public class Test01 {
    public static void main(String[] args) {
        method01(new USB() {
            @Override
            public void open() {
                System.out.println("USB打开");
            }
        });
    }

    public static void method01(USB usb) {
        usb.open();
    }
}
```

这就是 JS 里 `onClick(() => {...})` 的 Java 版本——只不过传的不是函数，是「一个实现了接口的对象」。

#### 4.5.3 当返回值返回

```java
public interface USB {
    void open();
}
```

```java
public class Test02 {
    public static void main(String[] args) {
        USB usb = method();
        usb.open();
    }

    public static USB method() {
        return new USB() {
            @Override
            public void open() {
                System.out.println("USB打开了");
            }
        };
    }
}
```
