# Day 18｜前端转全栈第 18 天：一条路由写一个类，Java 真不嫌麻烦

前 17 天基本都在跟 Java 语言本身较劲：语法、集合、Stream、IO、JDBC，代码全写在 `main` 里，跟浏览器一点关系没有。今天开始碰 HTTP，这块叫 JavaWeb，四个东西：Servlet、JSP、Session/Cookie、Filter。

看之前我以为这块会很重，翻完发现概念真不多，主要是换了个说法。心里也有数——这些活后面都会交给框架，现在没人手写 Servlet 拼 JSON 了。所以今天我只打算把概念过一遍，知道底下怎么转的就收手，不往深里挖。

## 1. 先对了个表

看资料的时候一直在脑子里翻译成 Express，干脆列出来，省得来回想：

| 前端（Express）             | JavaWeb                          | 差别                           |
| --------------------------- | -------------------------------- | ------------------------------ |
| `app.get('/user', handler)` | `@WebServlet("/user")` + `doGet` | **一个函数 → 一个类**          |
| `app.use(mw)`               | `Filter`                         | 中间件改名叫过滤器             |
| `req.query`                 | `request.getParameter()`         | 一样，但读不了 JSON            |
| `res.json(obj)`             | 手拼字符串 + `setContentType`    | 没有 `JSON.stringify()` 那一步 |
| `res.redirect()`            | `response.sendRedirect()`        | 一样                           |
| `res.render()`              | JSP                              | 服务端模板                     |
| `req.session`               | `HttpSession`                    | 名字都没改                     |
| `app.listen(3000)`          | 启动 Tomcat，把项目丢进去跑      | 端口不归代码管                 |

最不适应的是第一行。一条路由，Express 写个函数就完事，Java 得正儿八经建一个类。写 Node 那么多年，从来没为「一个接口」单独开过文件。

## 2. Servlet：一个类处理一个请求

我的理解是：Servlet 就是跑在容器里的一个 Java 类，容器最有名的是 Tomcat。Tomcat 说白了是个 Java 写的服务器程序，干的活跟 Node 里 `http.createServer` 加一张路由表是一个意思——起来、占个端口、等请求；请求来了，它把请求头和请求体拆开，找到对应的类调一下，再把结果写回去。

Express 里这套东西是自己在代码里写出来的，Tomcat 把它做成了一个独立程序，路由也不靠代码注册，靠注解声明。

照着写了个最小的：

```java
@WebServlet("/hello")
public class HelloServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        resp.setContentType("text/html;charset=UTF-8");  // 不写，中文就是乱码
        String name = req.getParameter("name");           // 相当于 req.query.name
        resp.getWriter().write("Hello, " + name);
    }
}
```

跟 `app.get('/hello', (req, res) => res.send('Hello, ' + req.query.name))` 是一回事。四件事：继承 `HttpServlet`、注解里写路径、重写 `doGet`、往 `resp` 里写。

边写边记了几个点：

- 参数一律 `getParameter` 拿，GET 的 query 和 POST 的表单都归它管；但 **JSON 请求体它读不了**，得自己把请求里的原始数据读出来，再解析成对象；
- 响应得手动 `setContentType("text/html;charset=UTF-8")`，不设就乱码；请求那边设编码 `setCharacterEncoding` 要赶在取参数之前；
- 返回 JSON 得自己把对象拼成字符串（没有 `JSON.stringify()`），`res.json()` 在这儿是不存在的；
- 只写了 `doGet` 却收到 POST，容器直接回 405（方法不允许）。第一次试的时候拿 Postman 发了个 POST，看到 405 愣了几秒才对上原因；
- 版本坑：Tomcat 10 是 `jakarta.servlet`，9 及以下是 `javax.servlet`。这个我一开始没注意，`import` 一片红。

## 3. 生命周期

| 阶段        | 时机                                         | 几次 |
| ----------- | -------------------------------------------- | ---- |
| 创建对象    | Tomcat 启动，或第一次请求                    | 1    |
| `init()`    | 对象创建完立刻调一次                         | 1    |
| `service()` | 每次请求，按方法分发给 `doGet` / `doPost` 等 | N    |
| `destroy()` | 项目停掉或 Tomcat 关闭                       | 1    |

中间那行是我今天真正记住的：**Tomcat 只创建这一个对象，之后所有请求都往这一个对象里塞。**

这点跟前端习惯正好反着。Express 里模块也只加载一次，但处理函数每次请求都会重新调一遍，函数里 `let` 的变量天然是新的；Servlet 那个类只造一次，写在类上的字段就始终只有一份，两个用户同时访问，写进去的数据会互相盖掉。

## 4. 转发和重定向

两个都能跳，机制不一样：

- **转发 `forward`**：服务器内部跳，一次请求。地址栏不变，`request` 里的数据能带过去，只能跳本站；
- **重定向 `redirect`**：服务器回一个跳转状态码（302），浏览器自己去请求新地址。地址栏变，数据全丢，能跳外站。

我的记法：forward 像 vue-router 切组件，地址不动、状态还在；redirect 像 `location.href = '/xxx'`，重新来一遍。

什么时候用哪个，我记的是：表单提交成功用重定向，不然按 F5 就把表单再交一遍；查完数据要渲染页面就用转发，因为结果得靠 `request` 带过去。

## 5. Cookie 和 Session

HTTP 是无状态的，这点早就知道，只是前端习惯把 token 塞 `localStorage`，这套用的是 Cookie + Session。

| 比什么   | Cookie               | Session                     |
| -------- | -------------------- | --------------------------- |
| 存在哪   | 浏览器               | 服务器内存                  |
| 能存什么 | 只有字符串，4KB 上限 | 任意 Java 对象              |
| 怎么认人 | 请求自动带上         | 靠 Cookie 里的 `JSESSIONID` |
| 生命周期 | `MaxAge` 控制        | 默认 30 分钟不活动          |

自己概括一句：**Session 靠 Cookie 实现，但数据在服务端**。浏览器里那个 `JSESSIONID` 只是钥匙，柜子在服务器上。

```java
req.getSession().setAttribute("user", loginUser);   // 存
Object u = req.getSession().getAttribute("user");   // 取
req.getSession().invalidate();                      // 退出登录
```

登录流程我按顺序捋了一遍：表单 POST → `getParameter` 拿账号密码查库 → 成功就把用户塞进 Session 再重定向到首页 → 失败就转发回登录页，带个错误提示 → 之后每个请求由 Filter 检查 Session 里有没有人。

顺手记了个坑：Session 是存在某一台服务器的内存里的，以后上线是多台机器，用户在 A 机登录、下个请求被分到 B 机，就掉登录了。常见做法是挪到 Redis 那种内存数据库里，不过后来大家更愿意改用 token，思路知道就行。

## 6. Filter

```java
@WebFilter("/*")
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;

        if (request.getSession().getAttribute("user") == null) {
            ((HttpServletResponse) resp).sendRedirect("/login.jsp");
            return;                     // 不放行，请求到此为止
        }
        chain.doFilter(req, resp);      // 放行，等价于 next()
    }
}
```

`chain.doFilter` 就是 Express 里那个 `next()`，不调用请求就卡在这儿。当成中间件理解之后一下就通了——统一编码、登录校验、权限拦截、记日志、加跨域头，全放这一层，多个 Filter 串成链，先进后出，跟 Koa 的洋葱模型一样。

有个细节挺隐蔽：登录校验的 Filter 得放过登录页和静态资源，不然会重定向到自己，死循环。我是看别人的例子才注意到这行。

## 7. JSP

JSP 本质还是个 Servlet，只是写法换成了 HTML 里嵌 Java——容器第一次访问时把它拼成一个 Servlet 类，编译好再跑。`<% %>` 写逻辑、`<%= %>` 输出、`${}` 取数据。

看着就眼熟——这不就是 EJS / Pug / PHP 吗，服务端把数据塞进模板、拼成完整 HTML 发出去。React / Vue 之前页面都这么写，前后端分离之后服务端只吐 JSON，这东西基本退场，只在老项目里还能见到。

这块我打算只做到「扔过来能看懂」，不花时间学了。

## 8. 今天的笔记就到这

1. 变化最大的是控制权：以前代码在 `main` 里自己跑，今天开始由 Tomcat 来调。
2. 一条路由一个类，是从 Express 过来最不适应的地方。
3. 一个能跑的 Servlet 就四件事：继承 `HttpServlet`、注解写路径、重写 `doGet`、往 `resp` 里写。
4. `init` 一次、`service` 每次、`destroy` 一次；Tomcat 只造这一个对象，所有请求都往里塞，类上的字段别拿来存状态。
5. 转发一次请求、地址不变、数据还在；重定向两次请求、地址变、数据全丢。表单提交成功就用重定向。
6. Cookie 在浏览器，Session 在服务端，靠 `JSESSIONID` 认人。
7. Filter 就是中间件，`chain.doFilter` 等于 `next()`。
8. JSP 当服务端模板看，能看懂老项目就行。

这四样现在看着零碎，但接请求、拦请求、记登录状态这几件事本身不会消失，只是后面写法会变短。明天开始学框架，第一个就是 Spring——看看这些活是怎么被它接管的，写法到底能短成什么样。
