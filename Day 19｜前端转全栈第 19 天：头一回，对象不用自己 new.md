# Day 19｜前端转全栈第 19 天：头一回，对象不用自己 new

昨天还在手写 Servlet，今天开始碰框架了，第一个就是 Spring——Java 后端绕不开的名字。看了一天资料，最大的感受是：这框架干的活，前端其实天天在享受，只是前端框架把事情做得太隐蔽，我从来没意识到背后有个「容器」。

今天四个概念：Spring 是什么、IoC、DI、AOP，外加事务。全是概念课，代码很少，正好。

## 1. Spring 到底指什么

我一开始被三个名字绕晕了：Spring、Spring Framework、Spring Boot。看完资料大概理清了：

- **Spring Framework**：框架本体，核心就两样——IoC 容器和 AOP，外面挂着一堆可选模块（数据库、Web 之类）；
- **Spring MVC**：Framework 里的 Web 模块，专门管 HTTP 请求那一层；
- **Spring Boot**：不是新框架，是脚手架。把 Framework、MVC 加上一堆配置打包好，开箱即用。

拿前端对照就清楚了：Spring Framework ≈ Vue 本体，Spring Boot ≈ create-vue / Vite 脚手架——帮你把配置配好，直接开写。所以平时说「学 Spring」，指的就是 Framework 这套核心。

## 2. IoC：对象不自己 new 了

**IoC（Inversion of Control，控制反转）**，名字唬人，意思特别朴素：**对象不自己 new，交给一个「容器」统一创建和管理。**

以前写 Java 是这样：

```java
UserDao dao = new UserDao();                  // 我自己造
UserService service = new UserService(dao);   // 还是我自己造，还得手动把依赖塞进去
```

谁用谁 new，对象之间的关系全靠手搓，改个构造函数全项目跟着改。

用了 Spring 之后：

```java
@Component                    // 告诉 Spring：这个类归你管
public class UserService { }

@Component
public class UserDao { }
```

程序启动时，Spring 扫描这些注解，把类创建好，放进自己维护的一个大 Map 里（类名 → 对象），要用的时候找它拿。这个大 Map 就是传说中的 **IoC 容器**。

想通之后发现，前端早就在这么干了：axios 实例建一次，到处 `import`，从来没人在每个文件里 `new Axios()`；Vue 的 app 实例、React 的 store 也一样——建一次，全局共享，谁也不自己造。IoC 就是把这个「建一次、共享」变成框架的默认行为，而且管得更宽：创建、销毁、依赖关系，全归它。

## 3. DI：依赖让框架塞进来

IoC 是思路，**DI（Dependency Injection，依赖注入）** 是落地手段：一个类需要别的类，不用自己 new，声明一句，容器把现成的塞进来。

```java
@Component
public class UserService {

    @Autowired
    private UserDao userDao;    // 不用 new，容器启动时自动塞进来
}
```

跟 React 的 `useContext`、Vue 的 `inject` 是一个思路：组件只声明「我需要什么」，不关心它从哪来，框架负责给。

顺手记了一个点：容器里的对象默认**只有一个**（单例），整个应用共用

## 4. AOP：不改原代码，加功能

**AOP（面向切面）** 解决的问题：有段逻辑（记日志、算耗时、权限检查）想加到几十个方法上。笨办法是每个方法开头结尾复制一遍；AOP 的办法是把这段逻辑抽出来，声明「在所有这些方法的前后插入」，原代码一个字不动。

```java
@Aspect
@Component
public class LogAspect {

    @Before("execution(* com.example.service.*.*(..))")   // service 包下所有方法执行前
    public void log() {
        System.out.println("方法要执行了");
    }
}
```

前端天天在用这个思路：axios 拦截器（请求发出去前后插逻辑）、Express / Koa 中间件、装饰器 `@decorator`，全是「不改原逻辑，外面套一层」。AOP 就是 Java 这边的正式版，只是拦截目标从「请求」换成了「方法」。

## 5. 事务

### 5.1 事务是什么

场景是转账：A 账户扣 100，B 账户加 100。这两条 SQL 必须一起成功、一起失败——A 扣了钱 B 没收到，账就乱了。

**事务（Transaction）** 就是把这组操作捆成一个整体：全成功，或者全失败回滚（已经做的撤销）。

我第一反应是拿 `Promise.all` 类比——有一个 reject 整体就算失败。后来想了想，这个类比只能帮我记住「一起算」的感觉，事务其实更狠：`Promise.all` 失败时，前面已经 resolve 的不会撤销；数据库回滚时，已经执行的 SQL 是真的会被撤销。数据库在底层专门记了撤销日志。

### 5.2 ACID

事务的四个特性，我记的时候每个都翻了一遍前端话：

| 特性   | 意思                     | 我的前端翻译             |
| ------ | ------------------------ | ------------------------ |
| 原子性 | 一组操作不可拆，同生共死 | 就是转账那个例子         |
| 一致性 | 事务前后账要对得上       | 总余额不能凭空多或少 100 |
| 隔离性 | 多个事务同时跑，互不干扰 | 并发抢同一个变量的问题   |
| 持久性 | 提交成功就真的写进硬盘了 | 断电也不丢               |

### 5.3 @Transactional

Spring 把事务做成了一个注解，方法上加一句，方法里的多条 SQL 自动捆成一个事务：

```java
@Transactional
public void transfer(int from, int to, double amount) {
    accountMapper.minus(from, amount);   // A -100
    accountMapper.add(to, amount);       // B +100
    // 任何一步抛异常，两条 SQL 全部回滚
}
```

不用手写「开事务 → 执行 → 提交 / 回滚」那套 JDBC 代码（那套挺长，框架底层替我们做了）。原理就是刚学的 AOP——在方法外面包了一层，进方法开事务，抛异常回滚，正常返回提交。两个概念在这儿接上了，挺妙。

### 5.4 传播行为和隔离级别

这两个名词今天只记了一句话，不深挖：

- **传播行为**：一个带事务的方法调用另一个带事务的方法，事务怎么办——加入外层那个，还是自己另开一个？最常用的两个：`REQUIRED`（默认，有就加入，没有就新建）、`REQUIRES_NEW`（把外层挂起，自己开个新的，互不影响）。前端找不到太贴的类比，硬凑的话像「函数调用时作用域是合并还是新开」；
- **隔离级别**：多个事务并发读同一批数据时的松紧程度（会不会读到别人没提交的数据）。MySQL 默认「可重复读」，先记这一句，脏读幻读那些名词等面试再抠。

## 6. 今天的笔记就到这

1. Spring Framework 是本体（IoC + AOP），Spring Boot 是脚手架，别混成一个东西。
2. IoC = 对象不自己 new，容器统一创建和管理。前端早就在这么干——axios 实例建一次到处用。
3. DI = 声明需要什么，容器自动塞进来，`@Autowired` 一句的事；跟 `useContext` / `inject` 一个思路。
4. 容器里的对象默认单例，全应用共用——Day 18 的坑在这里原样复现，状态照样不能乱存。
5. AOP = 不改原代码加功能，axios 拦截器 / Express 中间件的 Java 正式版，拦截目标从「请求」换成「方法」。
6. 事务 = 一组 SQL 同生共死，转账是标准场景；比 `Promise.all` 更狠，回滚是真的能撤销已执行的 SQL。
7. `@Transactional` 一个注解管事务，原理就是 AOP 在方法外面包了一层。
8. 传播行为、隔离级别今天只记名字和默认值（`REQUIRED`、MySQL 可重复读），不深挖。

今天全是概念，手上没怎么动。明天开始写接口，看看这些容器和注解，在真正接 HTTP 请求的时候是怎么串起来的。
