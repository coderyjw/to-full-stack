# 项目记忆：to-full-stack（6年前端转 Java 全栈系列）

## 文章规范
- 系列标题格式：`Day NN｜前端转全栈第 N 天：<JS→Java 差异点>`，写入文章开头 H1，文件重命名为同名 .md。
- 封面大字套路：前端写法（红色删除线）→ Java 写法（红色），等宽字体。已用：let→int / if(1)→if(true) / push()→int[5] / prototype→extends / throw "err"→new Exception() / ===→equals() / event loop→new Thread() / new Set()→new HashSet() / array.map()→stream().map() / readFile→FileInputStream / "// 注释"→@注解 / localStorage→MySQL / filter()→WHERE / axios→JDBC / app.get()→@WebServlet / new User()→@Autowired / doGet()→@GetMapping / bean.xml→application.yml / axios→Mapper / package.json→pom.xml / console.log→tail -f / delete()→STATE=0。番外封面大字可用非代码梗（如 99+你=100），编号位放 EXTRA 替代 DAY 数字。大字超过 ~21 个等宽字符（112px 字号）会溢出右边，需缩字号（84~96px）或缩短文案；26 字符在 100px 刚好放下。
- **对照梗必须先验 JS 现状**（ES6 有 Set/Map/WeakMap），别写"JS 没有所以 Java 才有"式表述；两边一致的点要明说"一致"。
- **篇幅原则（2026-11-12 用户强调）**：会被后续框架取代的知识（Servlet/JSP 这类）**只写概念不过度展开**，约 5~6k 字即可；一个概念配一个最小例子，不写建工程、不铺 API 表、不上完整配置示例。用户原话：「后面要学框架，这些实际都不会用的」。
- 文章定稿风格（Day05 起确认）：markdown 列表/表格替代代码块笔记、💬 第一人称感受引用块 2~3 处、文末「今日学习总结」+ 下篇预告钩子。
- **语气规范（2026-11-12 更新，覆盖旧版）**：文章是**自己的学习笔记**，不是教程、更不是对话。主线用第一人称「我」：我看之前以为 / 我的理解是 / 我记了几个点 / 我打算只做到能看懂。禁止教学腔——不要「必须知道」「记住这几点」「判断标准就一条」「今天的要求只有一个」这类祈使/布道句，改成「我的记法是」「我自己记的是」。**仍然禁止第二人称「你」**，也禁止「你说的 XX 就是它」式对话引用。💬 引用块留给具体踩坑故事，1~2 处够。
- **去 AI 味（2026-11-12 用户强调）**：宁可句子长短不齐，不要工整排比与「首先/其次/综上/维度」这类词；文末总结压到 9~10 条以内，每条口语化，别写成机械清单；适当加自嘲、骂一句「就这？」之类的人味。
- **前端视角铁律（2026-11-12 用户强调）**：作者是**纯前端**，后端零基础。任何后端概念（容器、Tomcat、实例、线程、原子性、报文、部署、302、序列化、压测、Redis、负载均衡…）都**不能默认读者懂**，必须用前端已知物翻译一次：Tomcat ≈ `http.createServer` + 路由表、容器一次只造一个对象 ≈ 模块级变量被所有请求共用、`i++` 三步 ≈ 读-加-写、302 ≈ `location.href`。术语能换成白话就换（「实例化」→「创建对象」、「应用卸载」→「项目停掉」）。
- **进度边界（2026-11-16 更新，覆盖旧版）**：Day 01–18 为 Java 语法 + JavaWeb（Servlet/JSP/Session/Filter）；Day 19 = Spring 核心概念（IoC/DI/AOP/事务，未写代码），Day 20 = Spring MVC（@RestController / @GetMapping / 参数绑定 / 内置 Tomcat / DispatcherServlet），Day 21 = Spring Boot（约定大于配置 / 自动配置 / starter / application.yml / @RestControllerAdvice 统一异常处理），Day 22 = MyBatis + MyBatis-Plus（Mapper 接口+XML / #{} vs ${} / 动态 SQL / 分页 / BaseMapper / LambdaQueryWrapper），Day 23 = Maven（pom/GAV / 依赖与 scope / 生命周期 / 仓库），Day 24 = Linux 常用命令（文件目录 / tail+grep 查日志 / 权限 755 / ps+kill+lsof），Day 25 = 真项目 CRUD 实操（配环境 / Base+Trade 微服务 / Action-Service-Mapper 分层 / operType 枚举 / req2Pojo / 逻辑删除 STATE / 窗口函数 / resultMap）。**未学** 拦截器、Spring Security、JPA、Redis、Docker 等，写作只能泛提一句；下一篇预计 Session + 拦截器。
- **前端基础假设（仍然有效）**：作者纯前端零后端，所有后端概念仍需用前端物翻译一次。
- **框架篇篇幅（2026-11-16 用户默认沿用）**：框架是后续真会用的，可比 Servlet 篇展开一点，约 4~5k 字；但第一天只写「一个接口从 Servlet 写法换成 Controller 写法 + 参数/返回值 + 请求流程骨架」，不铺工程搭建、不写 pom、不列全部注解。

## 封面规范（2026-11-06 更新）
- **每篇只出一张图**：`cover-dayNN.png`（2350×1000，2.35:1 公众号消息列表）。1:1（转发卡片/公众号主页）用户自己裁剪，不要主动出方版。
- 模板：covers/cover-dayNN.html，Chrome headless 渲染（必须加 --no-sandbox --disable-gpu --disable-software-rasterizer）。
- 视觉体系：米白底 #F6F3EC + 红 #E4391F + 黑 #16150F，右侧红书脊，SF Mono 小字标签，PingFang SC 大字。
- 布局注意：主体内容（DAY 编号 + 大字）集中在画面中部偏左，别贴边，方便用户裁 1:1。

## 渲染命令模板
"/Applications/Google Chrome.app/Contents/MacOS/Google Chrome" --headless=new --no-sandbox --disable-gpu --disable-software-rasterizer --hide-scrollbars --force-device-scale-factor=1 --window-size=W,H --screenshot=out.png "file:///path/cover.html"
