## 1.流程控制

Java中的流程控制语句 switch、case、break、default、if/else 和 JS中的几乎都是一样的。我们快速过一下

| 对比项                        | Java                                                                    | JavaScript                                                        |
| ----------------------------- | ----------------------------------------------------------------------- | ----------------------------------------------------------------- |
| **if-else 条件**              | 括号内 **必须是 boolean** ，不能写数字、字符串，`if(1)`直接编译报错     | 可以是任意值，自动转真假（真值 / 假值），`if(1)`、`if("abc")`合法 |
| **if-else 语法**              | `if() {} else if() {} else {}`，结构一致                                | 写法长得一样                                                      |
| **switch 匹配方式**           | 等值匹配，类型必须一致（类似`===`）                                     | 使用`===`严格相等匹配                                             |
| **switch 支持类型**           | 旧版：byte/short/int/char、枚举JDK7 + 支持 String；JDK17 + 支持模式匹配 | 几乎所有类型：number、string、boolean、对象、null                 |
| **case 后面的值**             | **必须是常量** ，不能是变量、表达式                                     | 可以是常量、变量、表达式                                          |
| **case 穿透**                 | ✅ 不加 break 会穿透                                                    | ✅ 不加 break 会穿透                                              |
| **break**                     | 作用：跳出 switch；也可以跳出循环                                       | 作用和 Java 一样，跳出 switch / 循环                              |
| **default**                   | 可选兜底分支；位置任意；default 里没 break 照样穿透                     | 行为一致，位置随便放，无 break 会穿透                             |
| **switch 箭头写法 ->、yield** | Java14 + 支持，自带阻断、无穿透，支持 yield 返回值                      | ❌ 没有这个语法                                                   |
| **switch 传入 null**          | 传入 null，String 类型 switch 会空指针 NPE                              | 可以正常匹配`case null`                                           |
| **if 多个分支**               | 只能布尔表达式，适合范围、组合条件 `> < &&                              | 同样支持复杂条件，还能利用真值简化                                |

- case的穿透性

```java
Scanner sc = new Scanner(System.in);
int month = sc.nextInt();
switch (month) {
    case 12:
    case 1:
    case 2:
        System.out.println("冬季");
        break;
    case 3:
    case 4:
    case 5:
        System.out.println("春季");
        break;
    case 6:
    case 7:
    case 8:
        System.out.println("夏季");
        break;
    case 9:
    case 10:
    case 11:
        System.out.println("秋季");
        break;
    default:
        System.out.println("输入有误");
        break;
}
```

- 箭头写法：自带阻断，**不用写 break，不会穿透**

```Java
int num = 1;
switch (num) {
    case 1 -> System.out.println("一");
    case 2 -> System.out.println("二");
    default -> System.out.println("未知");
}
```

- yeild 例子：用来返回结果给变量

```Java
String res = switch (num) {
    case 1 -> {
        System.out.println("匹配到1");
        yield "一"; // yield 代表返回这个值
    }
    case 2 -> "二";
    default -> "未知";
}
```

```java
1.概述:java语言提前定义好的类
2.作用:通过键盘录入的形式将数据放到代码中参与运行
3.使用:
  a.导包:为了声明我们使用的是哪个包下的对象
    import java.util.Scanner;
  b.创建对象:
    Scanner 对象名 = new Scanner(System.in)
  c.调用对象中的功能:
    对象名.nextInt() 键盘录入整数
    对象名.next() 键盘录入字符串
```

## 2.循环
