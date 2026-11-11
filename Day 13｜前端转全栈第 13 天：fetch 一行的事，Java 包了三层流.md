# Day 13｜前端转全栈第 13 天：fetch 一行的事，Java 包了三层流

Day 12 的 Stream 处理的是内存里的数据，今天让数据从外面流进来——文件、网络、控制台。这一块前端基本没碰过：浏览器里读文件是 `fetch` / `FileReader`，拿到就是现成的字符串或 Blob；Java 这里却分出了字节流、字符流、缓冲流、转换流、序列化流、打印流……第一眼会觉得"至于吗"。

> 💬 我的第一反应是"Java 在为难人"：读个文本文件，为什么要先 new FileInputStream、再包一层 InputStreamReader、再包一层 BufferedReader？后来想通了：JS 是把编码、缓冲这些决定**替你做掉**了，Java 是把这些决定**摆在你面前**——要性能选字节流，要省心选字符流，要跨编码再包一层转换流。选择权给你，复杂度也给你。

## 1. 先把"流"的分类搞清楚

I/O 就是内存和外部设备（文件、网络、控制台）之间的数据搬运。Java 的流按两个维度分类：

**按方向分**（以内存为参照物！）：

| 方向          | 含义               | 抽象基类                                 |
| ------------- | ------------------ | ---------------------------------------- |
| 输入流 Input  | 从外面**读进**内存 | `InputStream`（字节）/ `Reader`（字符）  |
| 输出流 Output | 从内存**写到**外面 | `OutputStream`（字节）/ `Writer`（字符） |

**按数据单位分**：

| 类型   | 单位                   | 适用                       | 抽象基类                       |
| ------ | ---------------------- | -------------------------- | ------------------------------ |
| 字节流 | 1 字节（8 位）         | 万能：图片、视频、任何文件 | `InputStream` / `OutputStream` |
| 字符流 | 1 字符（自动处理编码） | 只适合文本                 | `Reader` / `Writer`            |

**命名规则一句话：功能前缀 + 基类后缀**。看到名字就知道它是干嘛的：`FileInputStream` = 读文件的字节输入流，`BufferedReader` = 带缓冲的字符输入流。

> 为什么要有字符流？一个中文在 UTF-8 里占 3 个字节，用字节流读文本，读出来的是"半个半个的汉字"，拼不好就乱码。字符流 = 字节流 + 编码表，它替你把字节按编码拼回字符。

## 2. 字节流：万能但原始

### 2.1 FileOutputStream：往文件里写

```java
import java.io.*;

public class Demo01 {
    public static void main(String[] args) throws IOException {
        // 第二个参数 true 表示追加；不传或传 false 是覆盖重写
        FileOutputStream fos = new FileOutputStream("note.txt", true);

        // write 只认 byte[]，字符串要先 getBytes()
        fos.write("Java 第 13 天\n".getBytes());
        fos.write("学字节流\n".getBytes());

        fos.close();   // 必须关流
    }
}
```

三个要点：

- 构造时文件不存在会**自动创建**；存在且没开追加模式，**整个文件清空重写**；
- `write` 只收 `byte[]`，字符串要 `getBytes()`（不传编码就用默认字符集，Day 09 讲过 JDK 18+ 默认 UTF-8）；
- 换行要自己写 `\r\n`（Windows）或 `\n`，没有"自动换行"。

### 2.2 FileInputStream：从文件里读

`read()` 每次读 **1 个字节**，返回值是"读到的那个字节"（0~255），文件读完返回 **-1**——所以 -1 就是循环结束的信号：

```java
FileInputStream fis = new FileInputStream("note.txt");
int b;
while ((b = fis.read()) != -1) {
    System.out.print((char) b);   // 英文没事，中文会乱码
}
fis.close();
```

单字节读慢，而且**读中文必乱码**：一个汉字 3 个字节，你一次读 1 个，每次都只是"三分之一个汉字"。正经读法是用字节数组当缓冲：

```java
try (FileInputStream fis = new FileInputStream("note.txt")) {
    byte[] buf = new byte[1024];     // 一次最多搬 1KB
    int len;                          // 实际读到了几个字节
    while ((len = fis.read(buf)) != -1) {
        System.out.print(new String(buf, 0, len));
    }
}
```

### 2.3 字节流的正经用途：拷贝一切二进制文件

图片、视频、压缩包没有"字符"概念，**必须用字节流**。拷贝 = 一边读一边写：

```java
public class Demo02 {
    public static void main(String[] args) {
        long start = System.currentTimeMillis();

        try (FileInputStream fis = new FileInputStream("cat.jpg");
             FileOutputStream fos = new FileOutputStream("cat-copy.jpg")) {
            byte[] buf = new byte[1024];
            int len;
            while ((len = fis.read(buf)) != -1) {
                fos.write(buf, 0, len);   // 读多少写多少，别把整个 buf 都写进去
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("拷贝耗时：" + (System.currentTimeMillis() - start) + "ms");
    }
}
```

注意 `write(buf, 0, len)`：最后一次读取通常**不满**一个 buf，只写实际读到的 `len` 个字节，否则拷出来的文件末尾会多出一截脏数据。

## 3. 字符流：文本专用

### 3.1 FileWriter / FileReader

```java
try (FileWriter fw = new FileWriter("note.txt")) {
    fw.write("字符流可以直接写字符串\n");
    fw.write(65);              // 写的是字符 'A'，不是数字 65（和字节流不一样）
}

try (FileReader fr = new FileReader("note.txt")) {
    int c;
    while ((c = fr.read()) != -1) {
        System.out.print((char) c);   // 中文不再乱码
    }
}
```

### 3.2 缓冲字符流：BufferedReader 的 readLine 是真香

基本字符流没有缓冲，一行一行读要自己拼 `\n`。套一层 `BufferedReader` 之后有了杀手级方法：

```java
try (BufferedReader br = new BufferedReader(new FileReader("note.txt"))) {
    String line;
    while ((line = br.readLine()) != null) {   // 一次读一整行，不含换行符
        System.out.println(line);
    }
}
```

这就是"**装饰器模式**"：`new BufferedReader(new FileReader(...))` ——里层负责真正读写，外层负责加缓冲、加按行读的能力，一层套一层，像 `fetch` 外面套拦截器。默认缓冲区 8192 个字符，所以大数据量也扛得住。

写文件对应的有 `BufferedWriter`，带 `newLine()`（跨平台换行）。

### 3.3 转换流：指定编码

`FileReader` 只能用默认字符集。文件如果是 GBK 编码的，就得用转换流手动指定：

```java
try (BufferedReader br = new BufferedReader(
        new InputStreamReader(new FileInputStream("gbk.txt"), "GBK"))) {
    System.out.println(br.readLine());
}
```

这里能看清字符流的本质了：**Reader = InputStream + 编码表**。`InputStreamReader` 就是那层"字节 → 字符"的翻译器。

### 3.4 关流：try-with-resources

> 💬 前端真的不管这个：fetch 完谁去关连接？`localStorage` 谁去 close？Java 的流连着操作系统的文件句柄，不关就占着不放；而且输出流有缓冲，不关流可能**缓冲区没刷干净，文件少一截**。我第一次忘关流，文件写出来永远缺最后一行，查了半天。

传统写法在 `finally` 里 close，非常啰嗦。Java 7 之后用 **try-with-resources**，把流声明在 `try` 的括号里，代码块结束**自动关闭**（哪怕抛异常）：

```java
// 前面所有例子已经是这个写法了
try (FileWriter fw = new FileWriter("a.txt");
     BufferedReader br = new BufferedReader(new FileReader("b.txt"))) {
    // 用流
} catch (IOException e) {
    e.printStackTrace();
}   // ← 这里 fw、br 按声明的相反顺序自动 close
```

能这么写的条件是类实现了 `AutoCloseable` 接口——IO 流全部实现了。**以后写 IO 一律用它，别再手写 finally 关流**。

### 3.5 顺手知道：NIO 的一行版

日常开发读个小文本其实不用自己包流，`java.nio.file.Files` 工具类一行搞定：

```java
import java.nio.file.*;
import java.util.List;

String content = Files.readString(Path.of("note.txt"));            // Java 11+
List<String> lines = Files.readAllLines(Path.of("note.txt"));       // 顺便按行分好
Files.writeString(Path.of("out.txt"), "直接写");
```

但面试和排错都绕不开流的分层原理——工具类只是把这套东西替你写好了。

## 4. 序列化流：把对象直接变成字节

### 4.1 为什么要序列化

对象活在内存里，程序一关就没了。想把它**存进文件**或者**通过网络传给另一台机器**，必须先变成一串字节——这个过程就是序列化，反过来恢复对象叫反序列化。

```java
import java.io.*;

public class User implements Serializable {     // ① 先实现这个接口
    private static final long serialVersionUID = 1L;   // ② 版本号

    private String name;
    private transient String password;          // ③ 不想被序列化的字段
    private static int loginCount;              // static 属于类不属于对象，也不会被序列化

    public User(String name, String password) {
        this.name = name;
        this.password = password;
    }
    // getter/setter 省略
}
```

三个关键点：

1. **`Serializable` 是标记接口**——里面一个方法都没有（Day 06 说过 interface 的另一种用法），只是给 JVM 打个标签"我允许被序列化"；不实现就序列化会抛 `NotSerializableException`；
2. **`serialVersionUID` 是版本号**：序列化时写进去，反序列化时比对。不加的话类一改（加个字段、改个方法），旧的字节数据就反序列化失败，抛 `InvalidClassException`。**写实体类就顺手加上**；
3. **`transient` 修饰的字段不序列化**，读回来是默认值（引用类型是 `null`）——密码、token 这类敏感信息靠它挡住。

> 💬 前端把 token 存 localStorage 都不太有罪恶感，反正就是字符串。到 Java 这里，`writeObject` 那一瞬间我才意识到：整个对象连字段带值要变成一串字节躺在磁盘上——`transient` 不是语法糖，是安全意识。

### 4.2 读写对象

```java
// 序列化：对象 → 文件
try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("user.dat"))) {
    oos.writeObject(new User("张三", "123456"));
}

// 反序列化：文件 → 对象（返回 Object，要强转）
try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("user.dat"))) {
    User u = (User) ois.readObject();
    System.out.println(u.getName());          // 张三
    System.out.println(u.getPassword());      // null：transient 字段没回来
}
```

### 4.3 和 JSON 是一回事吗

不是一回事，但解决的是同一类问题：

| 维度     | JS 的 `JSON.stringify` / `parse` | Java 原生序列化                                                                                  |
| -------- | -------------------------------- | ------------------------------------------------------------------------------------------------ |
| 格式     | **文本**，人能看懂，跨语言通用   | **二进制私有格式**，只有 Java 自己认识                                                           |
| 安全     | 没有版本号问题                   | 有 `serialVersionUID` 版本控制                                                                   |
| 风险     | 函数、`undefined` 会被丢掉       | 反序列化不可信数据有历史级安全漏洞（还记得 readObject 反序列化漏洞吗），生产里要慎用             |
| 实际开发 | 前后端传输都用 JSON              | Java 项目里跨语言传输一般也用 JSON（Jackson / Fastjson），原生序列化多用于本地缓存、RPC 框架内部 |

一句话：**面试要会原生序列化，实际干活更多是 JSON**。

## 5. 打印流：你天天在用的那个 System.out

```java
PrintStream ps = System.out;   // ← 剧透：这就是你写了 13 天的 System.out.println
```

打印流的特点：

- **只有输出，没有输入**（`PrintStream` 字节版 / `PrintWriter` 字符版）；
- `print` / `println` 把所有类型都做了重载：int、double、boolean、Object、char[]……传什么都能打印，不用自己转字符串；
- **不抛 IOException**（内部记了个 trouble 标志位），所以写起来不用 try-catch，是所有流里最省心的。

它真正的杀手锏是**改输出去向**——这就是日志系统的雏形：

```java
PrintStream log = new PrintStream("app.log");
System.setOut(log);                     // 从此 System.out 不再打印到控制台

System.out.println("2026-11-13 用户登录");   // 这行进了 app.log
System.out.println("2026-11-13 订单创建");
```

改完之后，所有 `System.out.println` 都悄悄写进文件。理解了这个，就明白日志框架为什么能"一行配置把日志从控制台切到文件"。

## 6. 选型速查

| 场景                             | 用什么                                                      |
| -------------------------------- | ----------------------------------------------------------- |
| 拷贝图片 / 视频 / 压缩包等二进制 | 字节流（`FileInputStream` + `FileOutputStream`）            |
| 读文本，按行处理                 | `BufferedReader.readLine()`                                 |
| 写文本，要跨平台换行             | `BufferedWriter` + `newLine()`，或 `PrintWriter.println`    |
| 文件编码不是默认的（如 GBK）     | `InputStreamReader` / `OutputStreamWriter` 指定编码         |
| 对象存文件 / 传网络              | `ObjectOutputStream` / `ObjectInputStream` + `Serializable` |
| 简单日志 / 打印                  | `PrintStream` / `PrintWriter`                               |
| 就想一行代码读个小文本           | `Files.readString` / `Files.readAllLines`                   |

判断顺序：**是二进制还是文本 → 是不是要按行 → 编码对不对 → 要不要存对象**。

## 7. 今日学习总结

1. **流按方向和单位分四个抽象基类**：`InputStream` / `OutputStream` / `Reader` / `Writer`；输入输出以**内存**为参照——读文件是"输入"；
2. **字节流万能但不认字**：读中文乱码（UTF-8 一个汉字 3 字节），拷二进制文件只能用它，`write(buf, 0, len)` 别把整个缓冲写进去；
3. **字符流 = 字节流 + 编码表**：`Reader/Writer` 只管文本；编码不对就用转换流 `InputStreamReader` 手动指定；
4. **缓冲流是装饰器模式**：`new BufferedReader(new FileReader(...))` 一层层包，`readLine()` 按行读是日常主力；
5. **流必须关**：占文件句柄 + 缓冲不刷会丢数据；**一律用 try-with-resources**，声明在 `try()` 里自动关；
6. **序列化三件套**：实现 `Serializable`（标记接口）、写 `serialVersionUID`（防改类失效）、敏感字段加 `transient`（static 也不会被序列化）；
7. **原生序列化是二进制私有格式**，跨语言传输实际用 JSON（Jackson 等），两者别混为一谈；
8. **`System.out` 本身就是 PrintStream**：打印流全类型重载、不抛 IO 异常，`setOut` 能把输出切进文件——日志框架的雏形；
9. **读小文本的捷径**是 `Files.readString` / `readAllLines`，但分层原理面试必考；
10. 下一篇：反射、注解
