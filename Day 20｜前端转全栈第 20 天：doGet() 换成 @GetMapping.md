# Day 20｜前端转全栈第 20 天：doGet() 换成 @GetMapping

Day 18 那个 Servlet 项目，一个接口要一个类，还得去 web.xml 里登记，三个接口就是三个类加一段配置。今天换成 Spring MVC，一个类里塞五个方法，写完点一下 main，服务就起来了——Tomcat 都不用自己装。

前两天学的容器和注解，今天终于有地方落地了。

## 1. 同一个接口，两天的写法

Day 18 拿 Servlet 写「返回用户列表」，是这样的：

```java
@WebServlet("/user/list")
public class UserListServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        resp.getWriter().write("[{\"id\":1,\"name\":\"张三\"}]");
    }
}
```

今天同样的接口：

```java
@RestController
@RequestMapping("/users")
public class UserController {

    @GetMapping
    public List<User> list() {
        return List.of(new User(1L, "张三"), new User(2L, "李四"));   // 直接返回对象
    }
}
```

三个变化最明显：不用继承 HttpServlet、不用碰 request/response 这两个参数、不用自己拼 JSON 字符串。

手拼 JSON 那行看着就烦，`\"` 转义一层套一层，字段一多必出错，还没人帮我检查。现在直接返回 List，HTML 里那套「对象 → JSON」的活框架干了。

## 2. 服务器不用自己装了

Day 18 的部署流程我还记得：写代码 → 打成 war → 丢进 Tomcat 的 webapps 目录 → 启动 Tomcat。中间任何一步错了，就是 404 白页，而且报错信息藏在 catalina.out 里。

现在只要一个类：

```java
@SpringBootApplication
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);   // 跑完 8080 就能访问
    }
}
```

这就很像 `node index.js` 之后 `app.listen(8080)`。区别是 Node 里服务器是我自己写的，这里的 Tomcat 是**内嵌**在依赖包里的——一个 jar 里装着代码和服务器，双击就能跑。

这种「带服务器的 jar」叫 **Spring Boot**。Day 19 记过一句「Spring Boot 是脚手架」，今天算是见到实际样子了：帮我建工程、带内嵌 Tomcat、顺便把 Jackson（负责 JSON 那个库）也装好了。真正处理路由的还是 Spring MVC。

## 3. 路由：app.get() 的 Java 版

Express 里写路由是这样：

```js
app.get("/users", (req, res) => res.json(users));
```

Spring MVC 里，`@GetMapping("/users")` 对应 `app.get`，一个 Controller 类就是一组路由。类上那个 `@RequestMapping("/users")` 是路径前缀，相当于 Express 的 `router.use('/users', ...)`，方法里再只写后半截。

对比 Day 18 的 `@WebServlet("/user/list")`：Servlet 的路径是绑在**类**上的，一个类只能有一个路径；Controller 的路径绑在**方法**上，一个类能挂几十个。

路径参数也顺着这个思路：

```java
@GetMapping("/{id}")                              // 对应 /users/1
public User detail(@PathVariable Long id) { ... }
```

`@PathVariable` 就是 Express 的 `req.params.id`，`{id}` 就是 `:id`。

## 4. 参数怎么接

这部分是今天最实用的，整理成一张表：

| 前端写法（Express）                 | Spring MVC 写法                               | 备注                                     |
| ----------------------------------- | --------------------------------------------- | ---------------------------------------- |
| `req.query.id`                      | `@RequestParam Long id`                       | 默认必传，不传直接 400                   |
| `req.query.age` 可选                | `@RequestParam(required = false) Integer age` | 不加 `required=false` 就是必传           |
| `req.params.id`                     | `@PathVariable Long id`                       | 路径里的 `{id}`                          |
| `req.body`（`express.json()` 之后） | `@RequestBody User user`                      | 整段 JSON 变成一个对象，字段名对上就填好 |
| `req.headers['token']`              | `@RequestHeader("token") String token`        | 拿请求头                                 |

最爽的是 `@RequestBody`。Express 里拿到的是个普通对象，字段对不对、类型对不对全靠自己判断，`user.age` 是字符串还是数字得自己 `Number()`。这边声明成 `User`，Jackson 直接把 JSON 往对象里填，`age` 写个 `"abc"` 就直接 400 打回来。

参数类型也不用自己转，`@RequestParam Long id` 声明成 Long，传 `?id=1` 进来框架就转好了。传 `?id=abc` 就是 400，不用手写 `parseInt` 之后再判断 NaN。

## 5. 返回值：不用 JSON.stringify 了

`res.json(user)` 在 Java 这边的对应，就是 `return user`。

框架拿到返回值之后干三件事：用 Jackson 把对象序列化成 JSON、把 `Content-Type` 设成 `application/json`、写进响应体。所以那个 `setContentType("application/json;charset=UTF-8")` 也不用写了。

让这一切成立的注解是 `@RestController`，它等于 `@Controller` 加 `@ResponseBody`：

- `@Controller`：这个类的方法返回值是**视图名**，框架去找对应的页面模板（JSP/Thymeleaf 那套）；
- `@ResponseBody`：这个方法的返回值直接写进响应体，不当视图名用；
- `@RestController`：类上写这一个，等于给每个方法都加了 `@ResponseBody`。

> 💬 我第一版写的是 `@Controller`，方法 `return "ok";`，想着至少能看到个 ok。结果浏览器直接 404，IDEA 控制台干干净净，一条报错都没有。折腾了二十分钟才反应过来：Spring MVC 拿着 "ok" 去找叫 ok 的页面了，压根没打算把它当字符串返回。改成 `@RestController` 才对。

这个坑挺典型的——**404 不一定是路径写错了，也可能是我把返回值放错了地方。**

## 6. 一个请求进来之后

Day 18 里每个接口都是一个 Servlet，有几个接口就有几个 Servlet。Spring MVC 反过来：全项目只有**一个** Servlet 接所有请求，叫 `DispatcherServlet`。

简化成五步：

1. 请求先进内嵌的 Tomcat；
2. Tomcat 不问路径，全部丢给 `DispatcherServlet`；
3. `DispatcherServlet` 拿路径和方法（GET/POST）去查路由表，找到对应的方法——这张表是启动时扫 `@GetMapping` 这些注解建好的；
4. 参数解析：路径里的、query 里的、body 里的，拆开塞进方法参数；
5. 调方法，返回值交给消息转换器（Jackson）写成 JSON。

对照 Express 的中间件链：请求先过一串 `app.use`，最后被某个路由处理函数接住。`DispatcherServlet` 就是那个总调度，只不过它自己不做业务，只负责「找谁做」。

这段我没深挖，先把骨架记个大概。哪天接口莫名其妙 404 了，知道是第 3 步没匹配上；参数莫名其妙 400 了，知道是第 4 步没解析对。

## 7. 分层：Day 19 的注解在这儿用上了

Controller 只管接参数、返结果，业务逻辑和 SQL 不该写在这里。所以一个接口后面通常是三层：

```java
@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;    // 不用 new，容器塞进来

    @GetMapping("/{id}")
    public User detail(@PathVariable Long id) {
        return userService.getById(id);  // 业务交给 Service
    }
}
```

Controller 调 Service、Service 调 Mapper（管 SQL），全靠 `@Autowired` 串起来。Day 19 学的 DI，当时只有一个 `UserService` 和 `UserDao` 两个空类，看不出好处；今天接口一多就明白了——**每加一个接口，不用在任何一个地方 new 对象，也不用管谁先创建谁后创建。**

## 8. 今天踩的坑

- **`@Controller` 返回字符串**：字符串被当视图名，404。要 JSON 就用 `@RestController`。
- **`@RequestParam` 拿不到名字**：编译之后参数名默认会丢，Spring 只能靠字节码里留的调试信息猜。Spring Boot 的父 POM 默认开了 `-parameters`，所以没事；纯 Spring 项目不加这个编译参数，就得写成 `@RequestParam("id")` 显式指定。名字对不上时典型症状是 400 或者拿到 null。
- **405 和 400 到底谁是谁**：路径对不对是 404，路径对但方法不对（`@GetMapping` 的接口发了 POST）是 405，参数没解析上是 400。Day 18 只见过 404，今天三种凑齐了。
- **对象里没 getter**：Jackson 序列化靠 getter，字段是 private 又忘了生成 getter，返回的 JSON 里那个字段直接消失，也不报错。写实体类时顺手生成 getter/setter，别省。
- **8080 被占**：Day 18 是 Tomcat 占着，今天是自己这个 jar 占着，忘了关就 `Port 8080 was already in use`。

## 9. 今日学习总结

1. Day 18 一个接口一个 Servlet 类，今天一个类挂一堆接口，差别就在路径绑在类上还是方法上。
2. `@GetMapping` ≈ `app.get`，`@RequestMapping` 类上那句 ≈ `router.use` 的前缀。
3. `@RequestParam` / `@PathVariable` / `@RequestBody` 分别对应 query、params、body，还能顺手把类型转好。
4. `return 对象` ≈ `res.json()`，序列化和 Content-Type 框架包了。
5. `@RestController` = `@Controller` + `@ResponseBody`，少了后面那个，字符串会被当视图名，直接 404。
6. 全项目只有一个 `DispatcherServlet` 接请求，它查路由表找人干活，自己不做业务。
7. `@SpringBootApplication` 加一个 main 方法就跑起来了，Tomcat 内嵌在 jar 里，不用打 war 丢 webapps。
8. 404 是路径没匹配，405 是方法不对，400 是参数没解析上——分清楚能省一半排查时间。
9. Controller / Service / Mapper 三层靠 `@Autowired` 串，Day 19 的 DI 到这儿才算真的用上。

接口是通了，但数据还是 `List.of` 里手写的假数据。下一篇该把 Day 17 那套 JDBC 搬进来了——MyBatis，让它替我把 SQL 和对象之间的搬运干了。
