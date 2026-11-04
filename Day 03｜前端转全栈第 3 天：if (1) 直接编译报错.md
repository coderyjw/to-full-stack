# Day 03｜前端转全栈第 3 天：if (1) 直接编译报错

## 1. 流程控制

### 1.1 先介绍一个类 Scanner

```Java
1.Scanner 是一个java语言提前定义好的类
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

### 1.2 switch、case、break、default、if/else

Java中的流程控制语句 switch、case、break、default、if/else 和 JS中的几乎都是一样的。快速过一下就行

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

## 2.循环

java中的循环和js的也差不多，都有 **for循环**、while循环、do...while循环。不同的是java中循环的条件必须是boolean，没有for...of / for...in。遍历数组可以用 **增强 for(遍历集合，后面说)**

### 2.1 for循环

和js一样，但是条件**只能是 boolean**

```Java
for (int i = 0; i < 3; i++) {
    System.out.println("我爱java");
}
```

### 2.2 while 循环

一样条件**只能是 boolean**

```Java
        //1.定义一个count,接收对折的次数
        int count = 0;
        //2.定义一个变量,表示山峰的高度 -> mountain
        int mountain = 8844430;
        //3.定义一个变量,表示纸的厚度
        double paper = 0.1;
        //4.判断如果纸的厚度小于山峰的高度,就对折,然后统计对折次数
        while(paper<mountain){
            paper*=2;//paper = paper*2;
            count++;
        }
        //5.输出count
        System.out.println("count = " + count);
```

### 2.3 do...while

```java
int i = 1;
do {
    System.out.println("我爱钱");
    i++;
}while (i<=10);
```

### 2.4 这个三个循环的区别

1. for循环中初始化的变量,出了for循环使用不了了
2. while循环中的初始化变量,出了while还能使用
3. do...while至少执行一次

### 2.5 continue、break

这两个也是一样的

1. break:
   1. 在switch中代表结束switch语句
   2. 在循环中代表结束循环
2. continue:
   1. 结束当前本次循环,自动进入下一次循环

## 3. 再介绍一个随机数类 Random

在 js中要获取一个0-1 的随机数是用 Math.random() 方法

而在 Java 中就是Random类。它位于 `java.util.Random`

```java
使用:
  a.导包:import java.util.Random
  b.创建对象:
    Random 对象名 = new Random()
  c.调用方法:
    对象名.nextInt() -> 在int的取值范围随机一个数
    对象名.nextInt(int bound) -> 在0-(bound-1)之间随机一个数
```

### 3.1 对比表

| 项目             | JavaScript                      | Java                                |
| ---------------- | ------------------------------- | ----------------------------------- |
| 有没有 Random 类 | ❌ 无                           | ✅ `java.util.Random`               |
| 调用方式         | `Math.random()` 静态方法        | `new Random()` 创建对象再调用方法   |
| 自定义种子       | ❌ 不支持                       | ✅ 支持，构造方法传 seed            |
| 能力             | 只返回 0~1 浮点数，其余自己计算 | 直接生成 int、boolean、long、double |
