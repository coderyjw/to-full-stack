# Day 23｜前端转全栈第 23 天：package.json 变成了 pom.xml

前面二十多天，我一直在干一件事：往 `pom.xml` 里贴一段 XML，然后等 IDEA 右上角转圈，转完就能 import 新的类。

贴了七八次，从来没想过这玩意儿到底在干嘛——jar 从哪来？下到哪去了？为什么有时候贴进去报红、等十分钟又好了？今天专门补这一块，因为再往下走（后面要引 Redis、消息队列）全靠它。

## 1. Maven 干了 npm 和 webpack 两份活

前端这边分工很清楚：`npm` 管装包，`webpack/vite` 管构建。Maven 一个人把两件事都干了：

| 前端            | Maven                             |
| --------------- | --------------------------------- |
| `package.json`  | `pom.xml`                         |
| `dependencies`  | `<dependencies>`                  |
| `npm install`   | `mvn install`（或者 IDEA 点刷新） |
| `node_modules/` | 本地仓库 `~/.m2/repository`       |
| `npm run build` | `mvn package`                     |

所以我一开始的困惑——「为什么加个依赖还要写 XML」——本质上是把两件事混成一件了。npm 那边声明和构建是分开的，Maven 用一个文件同时管这两件事，看着就重。

## 2. pom.xml：先认三个坐标

npm 里一个包的名字就是身份，`axios` 就是 `axios`。Maven 这边一个包要靠三段确定：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>      <!-- 组织，像 npm 的 scope -->
    <artifactId>spring-boot-starter-web</artifactId> <!-- 项目名，才是「包名」 -->
    <version>3.2.0</version>                         <!-- 版本 -->
</dependency>
```

这三段叫 **GAV 坐标**，缺一个就找不到包。类比过来：

- `groupId` ≈ npm 的 `@scope`（`@vue/cli` 里的 `@vue`），只是 Java 这边反着写，用域名倒序（`org.springframework`）；
- `artifactId` ≈ 包名；
- `version` ≈ 版本号。

我自己项目的坐标写在文件最上面，也是三段：

```xml
<groupId>com.example</groupId>
<artifactId>demo</artifactId>
<version>1.0.0</version>
<packaging>jar</packaging>     <!-- 打出来是 jar 还是 war -->
```

`packaging` 这个前端没有对应物。Day 18 打的是 war（丢进 Tomcat），现在内嵌 Tomcat 了，打 jar 就行——一个能直接 `java -jar` 跑起来的包。

另外那个 `<parent>` 是 Spring Boot 塞进来的，作用是统一管版本：引 `starter-web` 时不用写版本号，父 POM 里配好了。这个思路像 npm 的 lockfile + 固定版本策略，只不过它是显式写在一个父文件里。

## 3. 依赖管理：Java 只有一份，这点跟 npm 不一样

这是今天最大的认知冲击。

npm 装依赖，是每个包带自己的 `node_modules`，所以 A 依赖 lodash@3、B 依赖 lodash@4，两个版本可以**同时存在**，互不干扰。

Maven 不是。**一个项目只有一份 classpath，同一个 artifact 只能有一个版本。** 撞上了就必须选一个。

怎么选？两条规则：

1. **路径最短优先**：我直接声明的版本比「依赖的依赖」带进来的版本优先；
2. **路径一样长，先声明的优先**。

查冲突靠这条命令：

```bash
mvn dependency:tree
```

它把整棵树打出来，哪个包从哪条路径进来的、最后选了哪个版本，一目了然。前端的对应物是 `npm ls`，但 npm 那边是查「装了哪些版本」，这边是查「最后只留下了哪一个」。

还有 scope，控制这个依赖在哪些阶段可用，跟 devDependencies 是一个思路但更细：

| scope             | 什么时候有                             | 前端类比                            |
| ----------------- | -------------------------------------- | ----------------------------------- |
| `compile`（默认） | 编译、测试、运行全程                   | `dependencies`                      |
| `provided`        | 编译和测试有，打包时不要               | 已由容器提供，像 CDN 引入的全局变量 |
| `runtime`         | 编译时不用，运行时要（比如 JDBC 驱动） | 只在运行时加载                      |
| `test`            | 只有测试代码能用                       | `devDependencies`                   |

`test` 那个最容易搞错：JUnit 这种打成 `test` 是对的，但要是写了 `compile`，最后打出来的 jar 里会带上整个测试框架，白胖十几兆。

## 4. 生命周期：一条流水线，不是一堆脚本

npm 那边 `npm run build` 跑的是 `package.json` 里我自己写的脚本，跑了什么完全看我写了什么。

Maven 反过来：**阶段是内置的、固定顺序的**，我叫哪个阶段，它就把前面的全跑一遍。

```
validate → compile → test → package → verify → install → deploy
```

所以：

- `mvn compile`：只编译；
- `mvn test`：编译 + 跑测试；
- `mvn package`：编译 + 跑测试 + 打成 jar（在 `target/` 下）；
- `mvn install`：前面全干 + 把 jar 装进本地仓库，别的项目就能引用了；
- `mvn deploy`：再往前一步 + 推送到私服，给团队其他人用。

刚接触时我老想着「我要单独跑一下打包」，其实 `mvn install` 一条就把编译、测试、打包、入库全串了。反过来，急着看效果又不想等测试：

```bash
mvn clean package -DskipTests
```

`clean` 是另一条生命周期，负责删掉 `target/`，等于前端删 `dist/`。常用就这几个：

| 命令                  | 干嘛                  |
| --------------------- | --------------------- |
| `mvn clean`           | 删 target             |
| `mvn compile`         | 编译到 target/classes |
| `mvn test`            | 跑单元测试            |
| `mvn package`         | 打 jar/war 到 target/ |
| `mvn install`         | 装进本地仓库          |
| `mvn dependency:tree` | 看依赖树              |

Day 20 那个「点 main 就能跑」，其实背后也是这些阶段跑出来的；IDEA 只是把按钮包了一层。

## 5. 仓库：三层，从近到远

依赖不会凭空出现，Maven 按这个顺序找：

1. **本地仓库** `~/.m2/repository`：硬盘上那个文件夹，跟 `node_modules` 一个角色，只是它**不属于某个项目，全机器共用**。所以第二个项目引同样的包，秒下；
2. **私服**（公司内部，通常是 Nexus）：团队自己封装的包、或者给中央仓库做缓存。个人项目基本用不上；
3. **中央仓库**：全世界的公共仓库，几十万个包都在这儿。

国内直接连中央仓库基本是龟速，所以第一步都得配镜像。在 `~/.m2/settings.xml` 里加一段阿里云：

```xml
<mirrors>
    <mirror>
        <id>aliyun</id>
        <mirrorOf>central</mirrorOf>
        <url>https://maven.aliyun.com/repository/public</url>
    </mirror>
</mirrors>
```

这个文件是**全局的**，跟项目无关，换电脑第一件事就是配它。

> 💬 我遇到过一个特别欠揍的情况：某个依赖死活下不下来，IDEA 一直报 `Could not resolve`，重试也没用。后来在本地仓库里翻，发现那个包目录下躺着一堆 `.lastUpdated` 后缀的文件——下载失败一次就留一个，之后 Maven 看到这个文件就认为「我试过了，别下了」，直接跳过。把整个目录删掉重新拉，一次就成。现在养成习惯：报红先在本地仓库里找那个目录，删了重来，比等它自己恢复快。

## 6. 多模块：一句带过

大一点的项目会把 controller、service 拆成独立模块，靠一个父 `pom.xml` 聚合起来，`<modules>` 里列子模块。

这个结构和前端的 monorepo（pnpm workspace）是一个思路：一个大仓库，里面几个包互相引用。不过我的项目还小，一个 pom 够用，先不碰。

## 7. 今日学习总结

1. Maven 一个人干 npm + webpack 两份活：管依赖，也管构建。
2. `pom.xml` = `package.json`，`npm install` = `mvn install`，`node_modules` ≈ 本地仓库 `~/.m2/repository`。
3. 一个包靠 GAV 三段定位：groupId ≈ scope、artifactId ≈ 包名、version 版本，缺一个就找不到。
4. 最大差异：npm 可以同时装两个版本的 lodash，Maven **一个项目只有一份 classpath**，撞版本必须选一个。
5. 选版本的规则是最短路径优先、等长时先声明优先，冲突用 `mvn dependency:tree` 查。
6. scope 管依赖在哪个阶段可用，`test` 别写成默认的 `compile`，否则打出来的包里带着测试框架。
7. 生命周期是固定顺序的流水线，叫后面的阶段会自动跑前面的：`mvn install` 其实包含了编译、测试、打包。
8. 仓库三层：本地 `~/.m2/repository` → 私服 → 中央仓库，国内先配阿里云镜像。
9. 下载失败会在本地仓库留 `.lastUpdated`，之后直接跳过不重试，删掉目录重来最快。

依赖这件事算是搞清楚了。不过现在接口还是谁来都能调——得把 Day 18 的 Session 捡回来，配上拦截器，看看「没登录就别想看」在后端是怎么拦下来的。
