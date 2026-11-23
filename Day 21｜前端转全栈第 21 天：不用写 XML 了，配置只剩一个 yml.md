# Day 21｜前端转全栈第 21 天：不用写 XML 了，配置只剩一个 yml

本来今天想直接上数据库。结果昨天那个工程越用越觉得邪门：我一行配置都没写，JSON 能自动转、路由能自动找到、Tomcat 能自己起来——凭啥？

不搞清楚这个，后面写再多接口心里也是虚的。所以今天先补 Spring Boot 本身，顺手把异常处理也一并收拾了。

## 1. Spring Boot 是「开箱即用的 Spring」

Day 19 记过一句：Spring Boot 不是新框架，是脚手架。今天算是真正体会到这句话。

拿前端说就特别顺：想写个 React 项目，不用自己配 webpack、babel、eslint、热更新，`create-react-app` 一条命令全给好；Vue 那边是 `create-vue`。Spring Boot 在 Java 这边干的是同一件事——**Spring 本体什么都能干，但什么都得自己配；Spring Boot 把最常见的那套配置提前配好，直接开写。**

撑起「开箱即用」的主要是三件事：

| 它做的事                                           | 前端对应                                   |
| -------------------------------------------------- | ------------------------------------------ |
| 约定大于配置：目录、包名、文件名对了就行，不用声明 | CRA 规定 `src/` 下怎么写，不用配 entry     |
| 自动配置：需要什么自己判断、自己注册               | webpack 默认帮我把 jsx、css、图片都处理了  |
| 内嵌服务器：jar 里带着 Tomcat                      | `node index.js` 直接起服务，不用另装 nginx |

## 2. 约定大于配置：目录结构本身就是配置

Day 18 写 Servlet 的时候，`web.xml` 里得一条条声明：哪个 URL 对应哪个 Servlet 类、哪个过滤器拦哪些路径、欢迎页是哪个文件。少写一条，404。

现在这些声明全没了，因为**约定代替了声明**：类放在 `controller` 包下、类上写 `@RestController`，框架扫到就认。

一个典型结构大概长这样：

```
src/main/java/com/example/demo/
├── DemoApplication.java      ← 启动类，得放最外层
├── controller/               ← 接请求
├── service/                  ← 业务
├── mapper/                   ← 数据访问
├── entity/                   ← 和表对应的对象
└── config/                   ← 自己写的配置类
src/main/resources/
├── application.yml           ← 配置就这一个
└── mapper/                   ← SQL 写在 XML 里（还没学，先占个位）
```

这套结构的核心是**单一职责 + 可替换**：Controller 只接参返参，Service 只写业务，Mapper 只碰 SQL。好处不用背，写两天就体会到了——Service 可以脱离 HTTP 单独跑测试，因为它的方法根本不认识 `HttpServletRequest`；哪天 Mapper 从 JDBC 换成别的实现，Controller 一个字都不用动。

> 💬 那个「启动类得放最外层」我踩过。顺手把 `DemoApplication` 扔进了 `controller` 包，想着都在一起好找。结果启动之后所有接口 404，Service 注入还报 `NoSuchBeanDefinitionException`。原因很简单：启动类默认只扫**自己所在的包和子包**，它一进 controller，service、mapper 全在扫描范围之外，谁都没被创建，自然谁都不认识谁。

## 3. 自动配置：为什么一行 XML 都不用写

这节是我今天最想搞明白的。翻了一圈资料，大概拆成四步：

1. 启动类上那个 `@SpringBootApplication`，其实是三个注解合体：`@SpringBootConfiguration`（这是个配置类）、`@ComponentScan`（扫自己的包）、`@EnableAutoConfiguration`（重点）；
2. `@EnableAutoConfiguration` 会去 classpath 里所有的 jar 包中找一份清单文件（Spring Boot 3 里叫 `AutoConfiguration.imports`），把那上面列的类名全部读进来——几百个，名字都叫 `XxxAutoConfiguration`；
3. 这几百个类不会全部生效，每个类头顶都挂着**条件注解**，条件不满足就跳过。常见三兄弟：

| 条件注解                    | 意思                       | 例子                                     |
| --------------------------- | -------------------------- | ---------------------------------------- |
| `@ConditionalOnClass`       | classpath 里有某个类才生效 | 有 `DispatcherServlet` 才装配 Spring MVC |
| `@ConditionalOnMissingBean` | 容器里没有这个 Bean 才生效 | 已经自己配了就不插手                     |
| `@ConditionalOnProperty`    | 配置里某个开关打开才生效   | `spring.mvc.xxx=true`                    |

4. 条件满足的自动配置类开始干活，往容器里注册一堆 Bean：路由表、参数解析器、JSON 转换器、内嵌 Tomcat……于是就有了昨天那个「什么都没配，但什么都能用」的工程。

`@ConditionalOnMissingBean` 这条我觉得是精华：**框架先按约定给一套默认的，我一旦自己定义了，它就让位。** 说白了是「约定优先，我的配置最高」，不是「框架说了算」。

前端的类比是组件库的默认值——`el-button` 不给 type 就是 default 样式，一旦传了 `type="primary"`，默认值就不管了。CRA 那套 webpack 配置也是，默认挺好，要改就得 eject 或者 craco，只不过 Boot 这边温柔得多，加个自己的 Bean 就行，不用把整套配置扒出来。

## 4. starter：一个依赖顶一堆

自动配置能生效的前提是 **classpath 里有那个类**。所以「要不要某个功能」，在 Spring Boot 里就变成了「引不引某个 starter」。

Day 18 那种老项目，pom 里得自己写：

```xml
<dependency><groupId>org.springframework</groupId><artifactId>spring-webmvc</artifactId></dependency>
<dependency><groupId>com.fasterxml.jackson.core</groupId><artifactId>jackson-databind</artifactId></dependency>
<dependency><groupId>org.apache.tomcat.embed</groupId><artifactId>tomcat-embed-core</artifactId></dependency>
```

现在只要一行：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

`spring-boot-starter-web` 这个包本身几乎没有代码，就是个**依赖清单**——它把 spring-webmvc、jackson、内嵌 tomcat、参数校验全拉进来，版本还是配套的，不用自己对。

拿 npm 类比，有点像装 `express` 之后还得自己 `npm i body-parser cors morgan`，而 starter 相当于有人替你把这几个打包成一个，版本也锁好了。区别是 npm 那边锁版本靠 lockfile，这边靠父 POM（`spring-boot-starter-parent`）统一管。

## 5. application.yml：配置就写这一个文件

以前 XML 满天飞：`web.xml` 管 Servlet，`spring-bean.xml` 管对象，`jdbc.properties` 管数据库，`log4j.xml` 管日志。现在全塞进 `application.yml`，几百行变几十行。

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/demo
    username: root
    password: 123456
```

几个我记下来的点：

- **yml 靠缩进分层，缩进只能用空格**，用了 Tab 直接启动报错，报错信息还说得不清不楚（`found character '\t' that cannot start any token`）；
- **冒号后面得留一个空格**，`port:8080` 会被当成一个字符串 key，`port: 8080` 才是键值对；
- `application.properties` 也能用，是 `server.port=8080` 这种平铺写法，功能一样，yml 层级清楚点，我现在用 yml；
- 取值用 `@Value("${server.port}")`，一组同前缀的配置就用 `@ConfigurationProperties(prefix = "spring.datasource")` 绑到一个类上；
- **多环境**：`application-dev.yml`、`application-prod.yml` 各写一份，主文件里 `spring.profiles.active: dev` 选一个生效。这个就是前端的 `.env.development` / `.env.production`，连名字都像。

优先级今天只记了个顺序：命令行参数 > 环境变量 > profile 配置 > 默认配置。真出问题再去抠，先知道「越具体越优先」就够了。

## 6. 统一异常处理：一个类兜住所有报错

这块是今天唯一让我觉得「早就该有」的东西。

之前写 Servlet，每个方法里 try/catch，出错就 `resp.setStatus(500)`，返回格式还各写各的。Spring MVC 一开始我也这么干，结果三个接口三种错误格式，前端那边 `res.data.msg` 有时候有、有时候没有，调试调得想砸键盘。

更糟的是——数据库报错我直接把异常信息吐回去了，里面带着表名和字段名。等于把库结构贴在公网上。

正确做法是一个类管全部：

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)          // 我自己定义的业务异常
    public Result<?> handleBusiness(BusinessException e) {
        return Result.fail(e.getCode(), e.getMessage());   // 提示语是给人看的
    }

    @ExceptionHandler(Exception.class)                  // 兜底
    public Result<?> handleOther(Exception e) {
        log.error("未处理异常", e);                       // 堆栈写进日志
        return Result.fail(500, "服务器开小差了");          // 前端只看这一句
    }
}
```

配上一个统一的返回壳：

```java
public class Result<T> {
    private int code;      // 200 成功，其它是错误码
    private String msg;
    private T data;
    // getter/setter 省略
}
```

这样就三件事一起解决了：**格式统一**（前端只解一层 `Result`）、**细节屏蔽**（堆栈留在服务器，前端拿不到表名）、**不用每个方法 try/catch**（业务代码里直接 `throw new BusinessException("余额不足")`，抛出去自然有人接）。

实现原理还是 Day 19 的 AOP：`@RestControllerAdvice` 会在 Controller 外面织一层，任何一个 Controller 方法抛异常，都会先被它接住。前端的对应物是 Express 的 error 中间件：

```js
app.use((err, req, res, next) => {
  res.status(500).json({ code: 500, msg: "服务器开小差了" });
});
```

思路一模一样，只不过 Express 得靠 `next(err)` 把错误递下去，Java 这边是抛出去就行，不用手写传递。

> 💬 有个地方我绕了一下：`@RestControllerAdvice` 里那些方法的返回值，为什么能直接变成 JSON？盯着看了一会儿才反应过来——这个名字里就带着 `@RestController`，等于替每个方法加了 `@ResponseBody`。昨天在 `@Controller` 上栽的跟头，今天它换了个马甲出现，幸好认出来了。

另外补一个边界：这个类只兜 **Controller 层**的异常。过滤器里抛的、自己 new 的线程里抛的，都轮不到它。前者在 DispatcherServlet 之前，后者压根不在请求链路上。这种时候就只能看到容器的默认错误页。

## 7. 今日学习总结

1. Spring Boot 就是「开箱即用的 Spring」，跟前端的 create-react-app / create-vue 一个位置，不是新框架。
2. 撑起开箱即用的是三件事：约定大于配置、自动配置、内嵌服务器。
3. 约定大于配置的具体表现：没有 `web.xml`，包名和注解就是声明；启动类得放最外层包，放错就整片扫不到。
4. 自动配置的流程：`@EnableAutoConfiguration` 读 jar 里的清单 → 几百个 `XxxAutoConfiguration` → 靠条件注解决定谁生效。
5. `@ConditionalOnMissingBean` 是关键——框架先给默认，我自己定义了就让位，所以是「约定优先，我的配置最高」。
6. starter 本身没代码，是份依赖清单；一个 `starter-web` 把 MVC、Jackson、内嵌 Tomcat 全带进来，版本还配套。
7. 配置从一堆 XML 收进一个 `application.yml`，注意缩进只能空格、冒号后必须有空格；多环境就是 `application-dev.yml` 那种写法，等于前端的 `.env.development`。
8. 统一异常处理用 `@RestControllerAdvice` + `@ExceptionHandler`，格式统一、细节屏蔽、业务代码不用 try/catch，原理还是 AOP。
9. 它只兜 Controller 那一层，过滤器和自建线程里的异常它管不着。

工程这块算是搞明白了个大概。但接口里的数据还是我在 `List.of` 里手写的，明天该把 Day 17 那套 JDBC 搬进来了——MyBatis，让它替我做 SQL 和对象之间的搬运。
