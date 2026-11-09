# Day 09｜前端转全栈第 9 天：=== 换成 equals，才比得出内容

今天全部围绕「字符串和对象」：Object 的两个方法、String 的不可变、一堆常用方法、文本块、StringBuilder。内容多，但主线只有一条——**JS 里顺手的那套比较方式，到 Java 基本都要换个写法**。

## 1. Object 类的两个方法

### 1.1 toString：为什么打印对象是地址值

Object 中的默认实现：

```java
public String toString() {
    return getClass().getName() + "@" + Integer.toHexString(hashCode());
}
```

- 直接输出对象名，默认调用 `toString()`，没重写就输出**地址值**；
- 重写之后，应该返回**对象的内容**（属性值）。

```java
public class Person {
    private String name;
    private int age;

    public Person(String name, int age) {
        this.name = name;
        this.age = age;
    }

    @Override
    public String toString() {
        return "Person{name='" + name + "', age=" + age + "}";
    }
}
```

```java
public class Demo01Object {
    public static void main(String[] args) {
        Person p1 = new Person("张三", 10);
        System.out.println(p1);            // Person{name='张三', age=10}
        System.out.println(p1.toString()); // 同上

        // 集合能直接打印内容，是因为 ArrayList 重写了 toString
        ArrayList<String> list = new ArrayList<>();
        list.add("张三");
        System.out.println(list);          // [张三]
    }
}
```

> 小结：直接输出对象名不想看到地址值，就重写 `toString()`。

### 1.2 equals：为什么内容一样的对象不相等

Object 中的默认实现，就是拿 `==` 比：

```java
public boolean equals(Object obj) {
    return (this == obj);
}
```

`==` 的规则很简单：

| 类型     | `==` 比较的东西 |
| -------- | --------------- |
| 基本类型 | 值              |
| 引用类型 | 地址值          |

所以默认的 `equals` 比的是地址。要按内容比较，就得自己重写。

**和 JS 对照一下**：

| 场景         | JS                                                   | Java                                                        |
| ------------ | ---------------------------------------------------- | ----------------------------------------------------------- |
| 字符串比内容 | `'abc' === 'abc'` → `true`（字符串是原始类型，比值） | `s1 == s2` → 看情况（String 是对象，比地址），要用 `equals` |
| 对象比引用   | `{a:1} == {a:1}` → `false`                           | `p1 == p2` → `false`，两边一致                              |
| 对象比内容   | 没有原生写法，手写遍历或 lodash `isEqual`            | 重写 `equals`，之后 `p1.equals(p2)` 就按内容比              |

一句话：**JS 的 `===` 约等于 Java 的 `==`（都比引用），Java 的 `equals` 才是"比内容"，而这个能力要你写进类里。**

重写前先想清楚四个问题：

1. **传进来的是自己**：`this == obj`，直接 return true，不用强转；
2. **传进来是 null**：直接 return false；
3. **类型不对**：先判断类型再强转，否则强转就 `ClassCastException`；
4. **类型对了**：向下转型，逐个比较属性。

`obj` 是 Object 类型，访问不到 Person 特有的 `name`/`age`——这正是继承里说的"父类不能直接调子类特有成员"，所以必须先向下转型。

```java
@Override
public boolean equals(Object obj) {
    if (this == obj) {
        return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
        return false;
    }
    Person p = (Person) obj;
    return age == p.age && Objects.equals(name, p.name);
}
```

这段不用手写——IDEA 里 `alt + insert` → equals() and hashCode() 自动生成。

```java
public class Demo02Object {
    public static void main(String[] args) {
        Person p1 = new Person("coder", 18);
        Person p2 = new Person("coder", 18);
        System.out.println(p1.equals(p2)); // true（重写后）

        String s1 = new String("abc");
        String s2 = new String("abc");
        System.out.println(s1 == s2);      // false：两个不同的对象，地址不同
        System.out.println(s1.equals(s2)); // true：String 重写了 equals
    }
}
```

> 小结：
>
> 1. 输出对象想看到内容 → 重写 `toString()`；
> 2. 比较对象想比内容 → 重写 `equals()`（String、ArrayList 这些官方类都重写好了）。

## 2. String：不可变的字符串对象

### 2.1 三个特点

- 所有字符串字面值（`"abc"`）都是 String 类的实例，**带双引号的都是对象**：`String s = "abc"` 中，String 是类型、s 是对象名、`"abc"` 是对象；
- 字符串是**常量**，值创建之后不能更改；
- 因为不可变，所以**可以共享**（常量池里同一份内容只存一次）。

### 2.2 底层原理

String 底层就是一个被 `final` 修饰的数组：

| JDK 版本   | 底层结构                                                    |
| ---------- | ----------------------------------------------------------- |
| JDK 8      | `private final char[] value`                                |
| JDK 8 之后 | `private final byte[] value`（省内存，拉丁字符只占 1 字节） |

数组被 final 修饰 → 数组的**地址值**定死，不能指向新数组。这就是"字符串不可变"的真正原因：值改不了，任何"修改"其实都是 new 了一个新对象。

### 2.3 四种创建方式

| 构造                   | 说明                                   |
| ---------------------- | -------------------------------------- |
| `String()`             | 空串对象                               |
| `String(String s)`     | 用字符串创建                           |
| `String(char[] chars)` | 用 char 数组创建                       |
| `String(byte[] bytes)` | 用 byte 数组创建，按平台默认字符集解码 |

```java
public class Demo01String {
    public static void main(String[] args) {
        System.out.println(new String());                  // 空串
        System.out.println(new String("abc"));             // abc
        System.out.println(new String(new char[]{'a','b','c'})); // abc
        System.out.println(new String(new byte[]{97,98,99}));    // abc

        // 截取数组的一部分：从索引 2 开始，取 3 个
        char[] chars = {'a','b','c','d','e','f'};
        System.out.println(new String(chars, 2, 3));       // cde
    }
}
```

> 实际开发中 99% 是直接 `String s = "abc";`，构造方法是给"从字节流/数组造字符串"的场景准备的。

### 2.4 byte 数组和中文（了解）

```java
byte[] bytes = {-27, -101, -67};
System.out.println(new String(bytes)); // 国
```

- 正数字节直接转成对应字符；
- 负数字节说明碰到中文了：**GBK 一个汉字 2 字节，UTF-8 一个汉字 3 字节**，解错编码就变成乱码；
- 补充：JDK 18 起（JEP 400）默认字符集统一为 UTF-8，不用再纠结"系统默认是 GBK"这个老问题了。

### 2.5 经典面试题：`==` 与常量池

同一个 `==`，为什么前面（`new` 出来的）是 false，这里（字面量）却是 true？**关键不在内容，而在串是怎么来的**：

| 写法                | 对象在哪                         | 同内容的第二个变量                |
| ------------------- | -------------------------------- | --------------------------------- |
| `String s1 = "abc"` | 字符串**常量池**，同内容只存一份 | 直接复用同一个对象 → `==` 为 true |
| `new String("abc")` | **堆**里，每次 new 都新开一块    | 地址不同 → `==` 为 false          |

```java
String s1 = "abc";              // 字面量：常量池里放一个 "abc"，s1 指向它
String s2 = "abc";              // 常量池里已有，直接复用 → s1、s2 指向同一块内存
String s3 = new String("abc");  // 堆里新开一个对象，内容一样但地址不同

System.out.println(s1 == s2); // true ：同一个对象
System.out.println(s1 == s3); // false：两个对象，地址不同
```

<img src="img/1743220521124.png" alt="1743220521124" style="zoom:80%;" />

所以结论要记成两句话，别记混：

1. **`==` 问的是"是不是同一个对象"**，答案取决于对象从哪来（字面量走常量池，new 走堆），内容一样不代表 `==` 为真；
2. **比内容永远用 `equals`**——它对两种写法都返回 true，`==` 只适合判断 `== null` 这种"有没有对象"的场景。

两个常考追问：

- **`String s = new String("abc")` 涉及几个对象？** → 2 个（常量池里的 `"abc"` + 堆里 new 出来的）；
- **共创建了几个？** → 1 个或 2 个（常量池里已有 `"abc"` 时只 new 1 个）。

### 2.6 拼接的坑

```java
String s1 = "hello";
String s2 = "world";
String s3 = "helloworld";
System.out.println(s3 == "hello" + "world");  // true  → 编译期就拼好了
System.out.println(s3 == s1 + s2);            // false → 有变量参与，运行时 new
System.out.println(s3 == s1 + "world");       // false → 同上
```

> 一句话记住：**等号右边全是字面值 → 编译期合并，不会产生新对象；有变量参与 → 运行时 new 一个**。所以循环里拼字符串一定要用 StringBuilder（第 5 节）。

## 3. String 常用方法

### 3.1 判断

| 方法                                 | 说明                           |
| ------------------------------------ | ------------------------------ |
| `boolean equals(Object obj)`         | 比较内容                       |
| `boolean equalsIgnoreCase(String s)` | 忽略大小写比较，验证码场景常用 |

### 3.2 获取

| 方法                                   | 说明                              |
| -------------------------------------- | --------------------------------- |
| `String concat(String str)`            | 拼接，返回新串                    |
| `char charAt(int index)`               | 按索引取字符                      |
| `int indexOf(String str)`              | 某串第一次出现的索引，没有返回 -1 |
| `String substring(int begin)`          | 从 begin 截到最后                 |
| `String substring(int begin, int end)` | 截取区间，**含头不含尾**          |
| `int length()`                         | 长度（注意：是方法，要带括号）    |

```java
String s = "abcdefg";
System.out.println(s.concat("哈"));    // abcdefg哈
System.out.println(s.charAt(0));       // a
System.out.println(s.indexOf("c"));    // 2
System.out.println(s.substring(2));    // cdefg
System.out.println(s.substring(2, 4)); // cd（含头不含尾）
System.out.println(s.length());        // 7
```

> 💬 前端数组是 `arr.length` 不带括号，Java 字符串是 `s.length()` 必须带括号——就这一个括号，我写了三天才形成肌肉记忆。

### 3.3 转换

| 方法                                  | 说明                             |
| ------------------------------------- | -------------------------------- |
| `char[] toCharArray()`                | 字符串 → char 数组               |
| `byte[] getBytes()`                   | 字符串 → byte 数组（默认字符集） |
| `byte[] getBytes(String charsetName)` | 按指定编码转 byte 数组           |
| `String replace(target, replacement)` | 替换，返回新串                   |

```java
String s = "aabcdefg";
System.out.println(s.toCharArray());         // aabcdefg
System.out.println(Arrays.toString(s.getBytes())); // [97, 97, 98, 99, ...]
System.out.println(s.replace("a", "z"));     // zzbcdefg
System.out.println(Arrays.toString("你".getBytes("UTF-8"))); // [-28, -67, -96]
```

### 3.4 分割

| 方法                           | 说明                       |
| ------------------------------ | -------------------------- |
| `String[] split(String regex)` | 按规则切割，参数是**正则** |

```java
System.out.println(Arrays.toString("abc,java,test".split(",")));  // [abc, java, test]
System.out.println(Arrays.toString("abc.java".split("\\.")));     // [abc, java] 点要转义
```

### 3.5 其他高频

| 方法                                     | 说明                             |
| ---------------------------------------- | -------------------------------- |
| `boolean contains(s)`                    | 是否包含                         |
| `boolean startsWith(prefix)`             | 是否以某串开头                   |
| `boolean endsWith(suffix)`               | 是否以某串结尾                   |
| `String toLowerCase()` / `toUpperCase()` | 大小写转换                       |
| `String trim()`                          | 去掉两端空格，**中间的空格不去** |

```java
System.out.println("abcdefg".contains("abc"));  // true
System.out.println("abcdefg".startsWith("a"));  // true
System.out.println("   abc  defg   ".trim());   // "abc  defg"
System.out.println("   a  b   ".replace(" ", "")); // ab：去所有空格得用 replace
```

## 4. 文本块（Java 15 正式转正）

写 HTML、JSON、SQL 时，以前只能这么拼：

```java
String s = "<!DOCTYPE html>\n" +
        "<html lang=\"en\">\n" +
        "<head>\n" +
        "    <title>标题</title>\n" +
        "</head>\n" +
        "</html>";
```

Java 15 之后，用 `"""` 包起来就行，不用转义、不用加 `\n`：

```java
String s = """
        <!DOCTYPE html>
        <html lang="en">
        <head>
            <title>标题</title>
        </head>
        </html>
        """;
```

三个要点：

1. 开始分隔符 `"""` 后面**必须换行**，`""""""`（六个引号写一行）是错的；
2. 内容里可以自由用双引号，不用转义；
3. 缩进会被自动对齐（以最短缩进为准），所以不用手写空格。

> 💬 这个太像 JS 的模板字符串了，我第一眼还以为终于能 `${name}` 插值了——并不行，Java 的文本块只是"多行原文"，没有插值能力。

## 5. StringBuilder：拼字符串的正确姿势

### 5.1 为什么需要它

String 每次拼接都 new 一个新对象，拼得越多越浪费；StringBuilder 底层是可变的数组（缓冲区），拼接都在同一块内存里做。

| 类            | 可变性 | 是否产生新对象 | 线程安全               | 效率 |
| ------------- | ------ | -------------- | ---------------------- | ---- |
| String        | 不可变 | 每次都产生     | 安全（不可变天然安全） | 最低 |
| StringBuilder | 可变   | 不产生         | 不安全                 | 最高 |
| StringBuffer  | 可变   | 不产生         | 安全（加锁）           | 中   |

结论：**单线程用 StringBuilder，多线程用 StringBuffer，效率 StringBuilder > StringBuffer > String。**

### 5.2 扩容机制

- 底层缓冲区默认是**长度 16** 的数组；
- 数组没被 final 修饰，装不下就扩容：**新建数组 → 拷贝老元素 → 地址赋给老数组**；
- 扩容规则：默认 **2 倍 + 2**；如果一次要存的量超过这个值，就直接按实际需要的长度扩。

### 5.3 常用方法

| 方法                      | 说明                                   |
| ------------------------- | -------------------------------------- |
| `StringBuilder append(x)` | 追加任意类型，返回自己（可以链式调用） |
| `StringBuilder reverse()` | 反转内容                               |
| `int length()`            | 当前长度                               |
| `String toString()`       | 转成 String（要当字符串用必须调它）    |

```java
public class DemoStringBuilder {
    public static void main(String[] args) {
        StringBuilder sb = new StringBuilder();
        sb.append("abc").append(1).append(true);
        System.out.println(sb);        // abc1true
        System.out.println(sb.reverse()); // eurt1cba
        System.out.println(sb.length()); // 8

        // 循环拼接：标准用法
        StringBuilder sb2 = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            sb2.append(i).append(i == 4 ? "" : ",");
        }
        String result = sb2.toString();
        System.out.println(result);    // 0,1,2,3,4
    }
}
```

## 6. 今日学习总结

1. **`toString` 管输出，`equals` 管比较**：不重写就都是地址值，重写后才有"内容"语义；
2. **`==` 比地址，`equals` 比内容**：基本类型用 `==`，引用类型比内容一律 `equals`；注意别拿 JS 硬套——JS 的 `===` 对对象也是比引用，和 Java 的 `==` 是一回事，差别只在字符串（JS 是原始类型、比值；Java 是对象、比地址）；
3. **String 不可变**：底层是 final 数组，地址定死，任何"修改"都产生新对象——不可变换来的是线程安全和常量池共享；
4. **字面量进常量池，new 进堆**：`"abc" == "abc"` 为 true，`new String("abc") == "abc"` 为 false；拼接时有变量参与就会 new；
5. **方法要记牢几个易错点**：`length()` 带括号、`substring` 含头不含尾、`split` 参数是正则（点要写 `\\.`）、`trim` 只去两端；
6. **文本块只是多行原文**：`"""` 后必须换行，没有 `${}` 插值；
7. **拼字符串用 StringBuilder**：默认 16 扩容 2n+2，单线程首选，最后 `toString()` 取出结果。
8. 下一篇：**集合**——前端有数组和 Map 走天下，Java 的 List / Set / Map 各有各的脾气，`ArrayList` 也不是数组的简单包装。
