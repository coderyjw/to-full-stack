# Day 06｜前端转全栈第 6 天：interface 不是 TS 那个 interface

昨天把封装和继承啃完了，三大特征还剩最后一个多态。但多态前面还卡着两块骨头：抽象类和接口，得先清掉。

先说结论：写过 TS 的我，一直以为自己懂 `interface`。今天才发现，我只是会写「类型声明」。Java 的 `interface` 是运行时真实存在的东西，里面还能带方法实现——这个真给我整不会了。

## 1. 抽象 abstract

### 1.1 抽象到底在抽什么

把一堆事物共有的性质提取出来，隐藏内部实现，只留对外能用的那部分。

说白了就是：**我知道你一定会这门手艺，但我不关心你怎么使出来。**

腾讯面试最爱问的「抽象和封装区别」，其实一句话就能记住：

- 封装：把细节藏起来，你不许直接看
- 抽象：把细节去掉，我压根不写

### 1.2 抽象类和抽象方法是怎么来的

把共有的方法抽到父类之后，会发现一个问题：父类里根本没法定实现。

比如「动物叫」，狗是汪汪、猫是喵喵，那 `Animal` 自己该怎么叫？没法叫。这时候就可以把方法定义成抽象的，只留一个方法签名，具体实现丢给子类。

记住一句话：**有抽象方法的类，一定是抽象类。**

### 1.3 定义格式

```java
// 抽象类
public abstract class 类名 {}

// 抽象方法：没有方法体，签名后面直接一个分号
修饰符 abstract 返回值类型 方法名(形参);
```

前端对照一下 TS 的写法：

```ts
abstract class Animal {
  abstract shout(): void; // TS：编译期检查，编译完这段就没了
}
```

长得几乎一样，但差别在骨头里：TS 的 `abstract` 是编译期的一次性检查，产物里啥也不剩；Java 的 `abstract` 是语言规则，编译器会真的拦着你不让你 `new Animal()`。

### 1.4 怎么用（四步）

1. 定义一个子类，继承抽象父类
2. 重写父类里**所有**抽象方法，给出具体实现
3. 创建**子类**对象（抽象父类不能 new）
4. 调用子类重写过的方法

```java
public abstract class Animal {
    public abstract void eat();
}

public class Dog extends Animal {
    @Override
    public void eat() {
        System.out.println("狗吃狗粮");
    }
}

public class Cat extends Animal {
    @Override
    public void eat() {
        System.out.println("猫吃猫粮");
    }
}

public class Test01 {
    public static void main(String[] args) {
        Dog dog = new Dog();
        dog.eat();

        Cat cat = new Cat();
        cat.eat();
    }
}
```

### 1.5 我卡了十分钟的问题：那继承还有啥意义？

当时脑子里冒出来的原话是：

> 继承本来是为了少写代码。现在把父类方法整成抽象的，子类还必须老老实实重写一遍，那一行 `extends` 到底图啥？

后来想通了，抽象根本不是「省代码」的工具，它是**设计**。抽象类可以看成一类事物的标准：只要你归到这个分类里，就必须拥有这个分类的能力。怎么证明你有？重写一遍。

父类负责定标准，子类负责实现。省下来的不是代码量，是「以后别人接手时知道该实现什么」。

前端里也有类似的东西：组件库规定你传进来的组件必须有 `onClick`，至于点了干什么，各个组件自己决定。约定在前，实现在后。

### 1.6 抽象的注意事项

1. 抽象类不能直接 new 对象，只能创建非抽象子类的对象
2. 抽象类里可以有构造方法，是给子类创建对象时初始化父类属性用的
3. 抽象类里可以有成员变量、构造方法、成员方法，跟普通类一样
4. 抽象类里不一定非得有抽象方法，但**有抽象方法的类一定是抽象类**
5. 抽象类的子类必须重写父类所有抽象方法，否则编译不过——除非这个子类也是抽象类

第 5 条最容易翻车：少重写一个，编译器直接把红波浪线糊你脸上。

## 2. 接口 interface

### 2.1 接口是什么

接口是一种规则，一种标准。既然是标准，里面的成员就非常单一。

想想 USB：鼠标、键盘、U 盘都能插同一个口，不是因为它们长得像，而是因为它们都遵守同一套规则——有 `open`，有 `close`。

### 2.2 定义与实现

```java
public interface 接口名 {}

// 实现类
public class 实现类 implements 接口名 {}
```

具体四步：

1. 创建接口
2. 创建实现类，实现这个接口
3. 在实现类中重写接口里所有抽象方法
4. 创建实现类对象（接口不能 new），调用重写的方法

```java
public interface USB {
    public abstract void open();
    public abstract void close();
}

public class Mouse implements USB {
    @Override
    public void open() {
        System.out.println("鼠标打开");
    }

    @Override
    public void close() {
        System.out.println("鼠标关闭");
    }
}

public class Test01 {
    public static void main(String[] args) {
        Mouse mouse = new Mouse();
        mouse.open();
        mouse.close();
    }
}
```

### 2.3 接口里能放什么（这段版本演进最有意思）

| JDK 版本   | 新增成员                          |
| ---------- | --------------------------------- |
| JDK 8 之前 | 抽象方法、常量                    |
| JDK 8      | 默认方法 default、静态方法 static |
| JDK 9      | 私有方法 private                  |

每加一样，背后都藏着一个「当年被骂出来的需求」。

#### 2.3.1 抽象方法

```java
public interface USB {
    public abstract void open();
    void close();   // 不写 abstract，默认也是 abstract
}
```

不写 `abstract` 也有，前端同学可以理解成「接口里的方法默认全是抽象方法」。

#### 2.3.2 常量 public static final

```java
public interface USB {
    public static final int NUM = 10;
    int NUM1 = 100;   // 不写修饰符，默认就是 public static final
}
```

- `final` 代表最终的，被修饰的变量不能二次赋值，也就是常量——**约等于 JS 的 `const`**
- 调用方式：`接口名.常量名`，不用 new
- `static final` 修饰的变量必须手动赋值，习惯上变量名全大写

#### 2.3.3 默认方法 default（这才是今天最大的收获）

JDK 8 之前有个死局：如果给一个已经在到处使用的接口新增一个抽象方法，**所有实现这个接口的类全部编译报错**，得挨个改代码。这在类库里根本不敢动。

于是 `default` 出现了：

- 给接口新增方法时带上默认实现，旧的实现类一行不改也能正常跑
- 实现类需要的话，可以重写这个方法
- 代价：打破了「接口只定义契约、不能有实现」的旧认知；还引出多实现冲突——一个类同时实现两个接口，两个接口有同名 `default`，子类必须手动重写消歧义

```java
public interface USB {
    public default void methodDef() {
        System.out.println("我是接口中的默认方法");
    }
}

public class Mouse implements USB {
    @Override
    public void methodDef() {
        System.out.println("我是重写的接口中的默认方法");
    }
}

public class Test01 {
    public static void main(String[] args) {
        Mouse mouse = new Mouse();
        mouse.methodDef();
    }
}
```

#### 2.3.4 静态方法 static

JDK 8 之前，跟某个接口配套的工具方法只能单独扔到一个工具类里，缺点很明显：

- 工具类和接口语义上紧紧绑着，却放在另一个类，找起来割裂
- 写着写着就攒出一堆零散的工具类

接口静态方法属于接口本身：

- 只能通过 `接口名.方法()` 调用
- **不会被实现类继承**，也不会被子类继承
- 天生适合放这个接口专属的工厂方法、判断、转换工具

```java
public interface USB {
    public default void methodDef() {
        System.out.println("我是接口中的默认方法");
    }

    public static void methodStatic() {
        System.out.println("我是接口中的静态方法");
    }
}

public class Test01 {
    public static void main(String[] args) {
        USB usb = new Mouse();
        usb.methodDef();

        // 静态方法只能用接口名调
        USB.methodStatic();
    }
}
```

#### 2.3.5 私有方法 private

有了 `default` 和 `static` 之后，新的问题来了：多个默认方法之间有重复代码，想抽公共逻辑，又不想暴露给实现类。

JDK 9 补上接口 `private` 方法：只给接口内部复用，对外隐藏。

```java
public interface USB {
    private void methodPrivate() {
        System.out.println("接口中的私有方法");
    }

    public default void methodDef() {
        methodPrivate();
    }
}
```

是不是很熟？这就是前端天天干的「抽个内部函数」，只不过抽的位置从模块顶层挪到了接口里。

### 2.4 接口的特点

1. 接口可以多实现：一个类可以实现一个或多个接口

   ```java
   public class InterfaceImpl implements InterfaceA, InterfaceB {}
   ```

2. 接口可以多继承：一个接口可以继承一个或多个接口

   ```java
   public interface InterfaceC extends InterfaceA, InterfaceB {}
   ```

3. 一个类可以继承一个父类的同时实现多个接口

   ```java
   public class Zi extends Fu implements InterfaceA, InterfaceB {}
   ```

4. 多个接口里抽象方法重名且参数一样，只重写一次就行

   ```java
   public interface InterfaceA { void method(); }
   public interface InterfaceB { void method(); }

   public class InterfaceImpl implements InterfaceA, InterfaceB {
       @Override
       public void method() {
           System.out.println("我是重写的 method 方法");
       }
   }
   ```

5. 多个接口里**默认方法**重名，参数一样，必须重写一次，否则编译不过

   ```java
   public class InterfaceImpl implements InterfaceA, InterfaceB {
       @Override
       public void methodDef() {
           System.out.println("重写的 methodDef 方法");
       }
   }
   ```

第 4 条和第 5 条的区别值得记一下：抽象方法重名是「一个签名」，重写一次就同时满足了；默认方法重名是「两份现成的实现打架」，编译器分不清你要哪个，只能你自己裁决。

### 2.5 抽象类和接口的区别

| 对比项         | 抽象类 abstract class                      | 接口 interface                       |
| -------------- | ------------------------------------------ | ------------------------------------ |
| 继承 / 实现    | `extends`，单继承                          | `implements`，多实现                 |
| 构造器         | 有构造器，供子类调用                       | 没有构造器，不能 new                 |
| 成员变量       | 普通变量、常量都可以                       | 默认 `public static final`，只能常量 |
| 方法（JDK 8+） | 抽象方法、普通方法、static、private 都可以 | 抽象、default、static、private       |
| 访问修饰       | public / protected / 默认 / private        | 默认 public；private 需显式写        |
| 语义           | is-a，是一种                               | can-do，具备某种能力                 |
| 多继承         | ❌ 不支持                                  | ✅ 多实现                            |
| 字段           | 可以有状态                                 | 一般不能放可变状态                   |

选哪个？一句话：**描述「它是什么」用抽象类，描述「它能干什么」用接口。**

狗是一种动物（抽象类），狗会看门（接口）。这个例子土，但真的一辈子忘不掉。

## 3. 多态

一句话：**同一个行为，不同对象有不同实现；父类引用指向子类对象，运行时才确定真正执行的方法。**

三个前提，缺一不可：

1. 有继承 / 实现关系
2. 子类重写（`@Override`）父类方法
3. 使用父类引用接收子类对象

前端其实天天在用，只是没起名字：

```js
// JS：谁传进来都行，看鸭子类型
function shout(animal) {
  animal.shout();
}
```

```java
// Java：类型必须先声明，但 Dog 和 Cat 都能塞进来
Animal a1 = new Dog();
Animal a2 = new Cat();
```

同一件事。区别是 JS 靠运行时猜你像不像鸭子，Java 靠编译期确认你是不是这个类。

### 3.1 基本使用

1. 定义一个父类或者接口
2. 定义一个子类或者实现类去继承 / 实现它
3. 重写抽象方法
4. 用父类引用接子类对象，调用方法

```java
abstract class Animal {
    public abstract void shout();
}

class Dog extends Animal {
    @Override
    public void shout() {
        System.out.println("汪汪");
    }
}

class Cat extends Animal {
    @Override
    public void shout() {
        System.out.println("喵喵");
    }
}

public class Test {
    public static void main(String[] args) {
        // 父类引用，指向子类实例 —— 这就是多态
        Animal a1 = new Dog();
        Animal a2 = new Cat();
        a1.shout();   // 汪汪
        a2.shout();   // 喵喵
    }
}
```

### 3.2 多态下成员的访问特点

这两条是昨天那句口诀的延续：**成员变量看等号左边，成员方法看 new 的右边。**

**成员变量：看等号左边是谁，就先调谁里面的成员变量，子类没有才找父类。**

```java
public class Fu { int num = 100; }
public class Zi extends Fu { int num = 10; }

public class Test01 {
    public static void main(String[] args) {
        Fu fu = new Zi();
        System.out.println(fu.num);   // 100，看左边是 Fu
    }
}
```

**成员方法：看 new 的是谁，就先调谁里面的方法，子类没有才找父类。**

```java
public class Fu {
    public void method() {
        System.out.println("我是父类中的 method 方法");
    }
}
public class Zi extends Fu {
    @Override
    public void method() {
        System.out.println("我是子类中的 method 方法");
    }
}

public class Test01 {
    public static void main(String[] args) {
        Fu fu = new Zi();
        System.out.println(fu.num);   // 100（变量看左）
        fu.method();                  // 子类中的 method（方法看右边 new 的）
    }
}
```

为什么变量和方法规则不一样？因为变量是编译期就绑定好的（静态绑定），方法要等运行时看真实对象是谁（动态绑定）。这也是「多态」三个字的技术底子。

### 3.3 多态的好处（为什么非得学它）

先摆问题：按原始方式 new 对象，既能调继承来的，也能调重写的，还能调子类特有的。用多态 new 出来，子类特有功能就调不了了，那多态图啥？

| 写法                        | 好处                         | 坏处                   |
| --------------------------- | ---------------------------- | ---------------------- |
| 原始方式 `Zi zi = new Zi()` | 继承的、重写的、特有的都能调 | **扩展性差**           |
| 多态方式 `Fu fu = new Zi()` | **扩展性强**                 | 不能直接调子类特有功能 |

多态的杀伤力在「加一个子类」的时候才显出来：

```java
// 没有多态：来一个动物加一个方法
public void feed(Dog dog) { dog.eat(); }
public void feed(Cat cat) { cat.eat(); }
public void feed(Pig pig) { pig.eat(); }
```

```java
// 有多态：一个方法搞定，明天加猪、后天加鹅，这行代码一个字都不用改
public void feed(Animal animal) { animal.eat(); }
```

这就叫「面向接口编程」。前端同学应该很有体感——这不就是给组件传 props 时，把类型声明成父类型 / 联合类型，而不是给每个具体组件写一个组件吗。

### 3.4 多态中的转型

1. **向上转型**（默认）：父类引用指向子类对象，`Animal a = new Dog();`
2. **向下转型**：把父类类型转成子类类型，得强转。作用是能调子类的特有方法

```java
Animal a = new Dog();
Dog d = (Dog) a;    // 强转
d.watchHouse();     // 现在能调子类特有方法了
```

前端对照 TS 的 `as`：

```ts
const d = a as Dog; // TS：纯编译期，哄编译器用的，运行时啥也不发生
```

```java
Dog d = (Dog) a;      // Java：运行时真的检查，不是 Dog 就当场崩
```

这个差别很关键：TS 的 `as` 骗得过去，Java 的强转骗不过去。

### 3.5 转型会出的问题

1. 类型转换异常：`ClassCastException`
2. 原因：强转的时候，等号左右两边类型不一致

```java
Animal a = new Cat();
Dog d = (Dog) a;   // 炸了：Cat cannot be cast to Dog
```

3. 用 `instanceof` 先判断类型

- 格式一：`对象名 instanceof 类型` → 判断前面的对象是否属于后面的类型

  ```java
  if (a instanceof Dog) {
      Dog d = (Dog) a;
      d.watchHouse();
  }
  ```

- 格式二：`对象名 instanceof 类型 对象名` → 判断通过会自动强转（模式匹配，JDK 16 起）

  ```java
  if (a instanceof Dog d) {
      d.watchHouse();   // 不用再写强转
  }
  ```

`instanceof` JS 里也有，但用法差得远：JS 是顺着原型链找构造函数，Java 是真的在比对类型。

## 4. 今日学习总结

1. **抽象 = 只定标准不写实现**。抽象方法所在类必须是抽象类，抽象类不能 new，子类必须重写全部抽象方法，否则编译不过
2. **接口是规则/标准，成员很少**。JDK 8 之前只有抽象方法 + 常量；JDK 8 加了 `default` 和 `static`；JDK 9 加了 `private`
3. **`default` 是接口演化的救命稻草**。给已广泛使用的接口加方法，老实现类不改也能跑；代价是多实现时同名 `default` 必须手动重写
4. **抽象类 vs 接口**：is-a 用抽象类（单继承、可有状态），can-do 用接口（多实现、只能常量）
5. **多态三前提**：有继承/实现 + 子类重写 + 父类引用接子类对象；好处是扩展性，代价是调不到子类特有方法
6. **多态访问口诀**：成员变量看等号左边，成员方法看 new 的右边。变量是编译期绑定，方法是运行时绑定
7. **向下转型要配 `instanceof`**，否则 `ClassCastException` 随时伺候；JDK 16+ 可以用 `instanceof 类型 变量名` 一步到位

从 JS 角度看今天这三块，最反直觉的一件事是：**JS 里「类型」是运行时才存在的东西，Java 里类型是编译期就要谈妥的合同。** 抽象类、接口、多态，本质都是把这份合同写得越来越正式。

## 5. 今天就讲到这里了

今天信息量确实有点大，抽象、接口、多态三个概念一天塞完，硬扛下来脑子是懵的。建议回头把 `Animal` / `USB` 那两个例子自己敲一遍，比看十遍都管用。

留一道小题，答案在最下面，先自己想想：

**单选题**：下面这段代码，输出什么？

```java
class Fu {
    int num = 100;
    public void method() { System.out.println("Fu"); }
}
class Zi extends Fu {
    int num = 10;
    @Override
    public void method() { System.out.println("Zi"); }
}

Fu fu = new Zi();
System.out.println(fu.num);
fu.method();
```

A. `100` 和 `Fu`
B. `10` 和 `Zi`
C. `100` 和 `Zi`
D. `10` 和 `Fu`

<details>
<summary>答案</summary>

**C**。成员变量看等号左边（`Fu`，所以是 100），成员方法看 new 的右边（`Zi`，所以打印 Zi）。

</details>

明天继续，把多态的边角料（final、权限修饰符、代码块、内部类）扫干净，这块就算收尾了。
