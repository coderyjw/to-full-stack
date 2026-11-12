# Day 17｜前端转全栈第 17 天：Java 里没有 axios，只有 JDBC

数据库的语法练了两天，但 SQL 一直是手敲在客户端里的。今天的终点是让 **Java 代码**去执行 SQL——这门技术叫 JDBC。开头先补两块数据库的内功：常用函数和 DCL 权限语句。

> 💬 前端的网络请求真的太舒服了：`await axios.get('/api/users')` 一行拿到数据，剩下的交给 then。第一次照着教程写 JDBC，注册驱动、拿连接、建语句、执行、遍历结果集、挨个 close——心里只有一句：**这七步里任何一步忘了，程序就报错或者泄漏连接**。后来用上连接池和 ORM 才明白，这七步是地基，框架只是把它包起来了。

## 1. MySQL 的常用函数

SQL 也提供函数，用来在**查询阶段**加工数据。好处是不用把原始数据捞到 Java 里再处理——和 Day 12 说的「Stream 能干的别写 for」是同一个道理：能在数据源算完的，就别搬上来算。

### 1.1 字符串函数

| 函数                     | 作用                   | 示例                       | 结果  |
| ------------------------ | ---------------------- | -------------------------- | ----- |
| `CONCAT(a, b, ...)`      | 拼接                   | `CONCAT('第', 1, '名')`    | 第1名 |
| `LENGTH(s)`              | **字节数**             | `LENGTH('张三')`（UTF-8）  | 6     |
| `CHAR_LENGTH(s)`         | **字符数**             | `CHAR_LENGTH('张三')`      | 2     |
| `SUBSTR(s, pos, len)`    | 截取，位置从 1 开始    | `SUBSTR('hello', 2, 3)`    | ell   |
| `UPPER / LOWER`          | 大小写                 | `UPPER('abc')`             | ABC   |
| `TRIM(s)`                | 去首尾空格             | `TRIM(' a ')`              | a     |
| `REPLACE(s, a, b)`       | 替换                   | `REPLACE('a-b', '-', '/')` | a/b   |
| `INSTR(s, sub)`          | 子串位置，找不到返回 0 | `INSTR('abc', 'c')`        | 3     |
| `LPAD / RPAD(s, n, pad)` | 左/右补齐到 n 位       | `LPAD('7', 3, '0')`        | 007   |

两个前端会栽的坑：**`LENGTH` 数的是字节不是字符**（中文在 UTF-8 里 3 字节），要数汉字用 `CHAR_LENGTH`；**`SUBSTR` 下标从 1 开始**，和 JS 的 `slice(0)` 差一位。

### 1.2 数值函数

```sql
SELECT ROUND(3.567, 1);    -- 3.6   四舍五入
SELECT CEIL(3.1), FLOOR(3.9); -- 4, 3  向上/向下取整
SELECT ABS(-5);            -- 5
SELECT MOD(10, 3);         -- 1     取余
SELECT TRUNCATE(3.789, 2); -- 3.78  直接截断（不四舍五入）
```

JS 里这些靠 `Math.round / Math.ceil / Math.floor`，SQL 里是同一个思路。注意金额计算一律交给 `DECIMAL` 字段和 SQL，别用 JS 的浮点数算完再存（Day 15 提过 0.1 + 0.2 的坑）。

### 1.3 日期函数

| 函数                              | 作用            | 示例结果                                  |
| --------------------------------- | --------------- | ----------------------------------------- |
| `NOW()`                           | 当前日期时间    | 2026-11-17 10:30:00                       |
| `CURDATE()` / `CURTIME()`         | 当前日期 / 时间 | 2026-11-17 / 10:30:00                     |
| `YEAR(d)` / `MONTH(d)` / `DAY(d)` | 取年 / 月 / 日  | 2026 / 11 / 17                            |
| `DATEDIFF(d1, d2)`                | 相差天数        | `DATEDIFF('2026-11-20','2026-11-17')` → 3 |
| `DATE_ADD(d, INTERVAL n DAY)`     | 日期加减        | `DATE_ADD(NOW(), INTERVAL 7 DAY)`         |
| `DATE_FORMAT(d, fmt)`             | 格式化成字符串  | `DATE_FORMAT(NOW(), '%Y-%m-%d')`          |
| `STR_TO_DATE(s, fmt)`             | 字符串转日期    | `STR_TO_DATE('2026/11/17', '%Y/%m/%d')`   |

`DATE_FORMAT` 是报表和接口返回里最常用的：数据库存 `DATETIME`，展示时转成 `2026-11-17` 这种字符串，前端就不用再格式化一遍。

**但有个性能陷阱**：`WHERE DATE_FORMAT(created_at, '%Y-%m-%d') = '2026-11-17'` 这种写法会让 `created_at` 上的索引失效（对字段用了函数，索引顺序对不上了）。正确写法是范围查询：

```sql
WHERE created_at >= '2026-11-17 00:00:00' AND created_at < '2026-11-18 00:00:00'
```

### 1.4 流程函数

```sql
-- IF(条件, 真值, 假值)：相当于三元表达式
SELECT name, IF(salary > 10000, '高薪', '普通') FROM emp;

-- IFNULL(值, 兜底)：null 时取兜底值（对应 JS 的 ?? ）
SELECT name, IFNULL(salary, 0) FROM emp;

-- CASE WHEN：多分支，相当于 switch / if-else 链
SELECT name, salary,
    CASE
        WHEN salary >= 20000 THEN 'A'
        WHEN salary >= 10000 THEN 'B'
        ELSE 'C'
    END AS level
FROM emp;

-- CASE 等值写法
SELECT name,
    CASE dept_id WHEN 1 THEN '研发' WHEN 2 THEN '销售' ELSE '其他' END AS dept_name
FROM emp;
```

对应关系很清楚：`IF` ↔ 三元表达式、`IFNULL` ↔ `??`、`CASE WHEN` ↔ `switch` / 多分支 if。区别是这些函数**在数据库里执行**，几百万行的分类统计不用先捞出来。

## 2. DCL 语句

DCL（Data Control Language）管的是**权限**：谁可以连这个库、能对哪张表做什么。前两天学的 DDL/DML/DQL 都是「怎么操作数据」，DCL 是「谁能来操作」。

### 2.1 用户管理

```sql
-- 创建用户（MySQL 8 的写法，必须指定 host）
CREATE USER 'app_user'@'localhost' IDENTIFIED BY 'App@123456';

-- 改密码
ALTER USER 'app_user'@'localhost' IDENTIFIED BY 'NewPwd@2026';

-- 删除用户
DROP USER 'app_user'@'localhost';

-- 查看所有用户
SELECT user, host FROM mysql.user;
```

`'app_user'@'localhost'` 这个组合说明：**MySQL 的用户 = 用户名 + 来源主机**。`@'%'` 表示任意主机（远程访问），`@'192.168.1.%'` 表示指定网段。同名用户配不同 host 是不同的账号。

### 2.2 授权与回收

```sql
-- 授予 blog 库所有表的查询和插入权限
GRANT SELECT, INSERT ON blog.* TO 'app_user'@'localhost';

-- 回收权限
REVOKE INSERT ON blog.* FROM 'app_user'@'localhost';

-- 查看某用户的权限
SHOW GRANTS FOR 'app_user'@'localhost';

-- 权限变更后刷新（通过 GRANT/REVOKE 操作通常不需要，直接改权限表才要）
FLUSH PRIVILEGES;
```

常用权限：`SELECT` / `INSERT` / `UPDATE` / `DELETE`（表级 DML）、`CREATE` / `DROP` / `ALTER`（结构）、`ALL PRIVILEGES`（全部）。

### 2.3 一个必须记住的原则

**最小权限原则**：应用连接数据库的账号，只给业务需要的权限。只做查询的报表服务，就不要给 `DELETE`；只操作 `blog` 库的账号，就别给 `*.*`。

这和 Day 15 讲的「数据库约束是绕不过去的最后防线」一脉相承：前端有按钮级权限、后端有接口鉴权、数据库还有一层账号权限——**每一层都假设上一层可能被绕过**。生产环境用 root 连应用是最典型的危险操作，一旦代码被注入，攻击者拿到的就是整个实例的最高权限。

## 3. JDBC

### 3.1 它到底是什么

JDBC（Java Database Connectivity）是 Java 官方定义的一套**操作数据库的标准接口**（`java.sql.*` 包里的 `Connection`、`Statement`、`ResultSet` 等），具体实现由各数据库厂商提供——MySQL 提供的是 MySQL Connector/J 驱动。

这种设计正是 Day 06 讲的「面向接口编程」：Java 代码只依赖 `Connection` 接口，换数据库只需要换驱动 jar 和连接 URL，代码基本不动。

### 3.2 准备驱动

Maven 项目在 `pom.xml` 里加依赖（版本按实际最新 8.x 调整）：

```xml
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <version>8.4.0</version>
</dependency>
```

普通 Java 项目则手动下载 jar 并加入 classpath（IDEA：Project Structure → Libraries → 加 jar）。

### 3.3 七步走通

```java
import java.sql.*;

public class JdbcDemo {
    public static void main(String[] args) throws Exception {
        // 1. 注册驱动（MySQL 8 的驱动会自动注册，这行可省，但显式写上更清楚）
        Class.forName("com.mysql.cj.jdbc.Driver");

        // 2. 获取连接：url、用户名、密码
        String url = "jdbc:mysql://localhost:3306/blog"
                   + "?serverTimezone=Asia/Shanghai&useSSL=false&characterEncoding=utf8";
        Connection conn = DriverManager.getConnection(url, "root", "123456");

        // 3. 创建语句对象
        String sql = "SELECT id, name, salary FROM emp WHERE salary > ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setDouble(1, 8000);           // 4. 填参数（下标从 1 开始）

        // 5. 执行，拿到结果集
        ResultSet rs = ps.executeQuery();

        // 6. 遍历结果集（next() 把指针移到下一行，没数据返回 false）
        while (rs.next()) {
            int id = rs.getInt("id");
            String name = rs.getString("name");
            double salary = rs.getDouble("salary");
            System.out.println(id + " / " + name + " / " + salary);
        }

        // 7. 释放资源：后开的先关
        rs.close();
        ps.close();
        conn.close();
    }
}
```

URL 的格式记一下：`jdbc:mysql://主机:端口/库名?参数`。几个常用参数：

| 参数                           | 作用                              |
| ------------------------------ | --------------------------------- |
| `serverTimezone=Asia/Shanghai` | 时区，不配上可能出现时间差 8 小时 |
| `useSSL=false`                 | 本地开发关掉 SSL 提示             |
| `characterEncoding=utf8`       | 编码，防中文乱码                  |
| `allowPublicKeyRetrieval=true` | MySQL 8 免 SSL 连接时可能需要     |

### 3.4 增删改：executeUpdate

查询用 `executeQuery()` 返回 `ResultSet`；增删改用 `executeUpdate()`，返回**影响的行数**：

```java
String sql = "INSERT INTO emp(name, salary, age, dept_id) VALUES (?, ?, ?, ?)";
try (Connection conn = DriverManager.getConnection(url, "root", "123456");
     PreparedStatement ps = conn.prepareStatement(sql)) {

    ps.setString(1, "赵六");
    ps.setDouble(2, 9000);
    ps.setInt(3, 28);
    ps.setInt(4, 1);

    int rows = ps.executeUpdate();
    System.out.println("影响行数：" + rows);   // 1
}
```

上面用 `try ( ... )` 是 **try-with-resources**（Day 13 讲 IO 时提过）：括号里声明的连接和语句会在块结束时自动关闭，顺序正好是后开先关。JDBC 资源写在 try 括号里是硬性要求级别的习惯——手动 close 一定会漏，漏了就连接泄漏，池子被占满后整个服务卡死。

### 3.5 为什么必须用 PreparedStatement

新手教程里常见 `Statement` 拼字符串：

```java
// 危险写法！前端搜索框输入 ' OR '1'='1 就能拖走整张表
String sql = "SELECT * FROM emp WHERE name = '" + userInput + "'";
Statement st = conn.createStatement();
ResultSet rs = st.executeQuery(sql);
```

如果 `userInput` 传进来的是 `' OR '1'='1`，拼出来的 SQL 变成：

```sql
SELECT * FROM emp WHERE name = '' OR '1'='1'
```

条件恒真，全表数据泄露；要是拼接的是 `DELETE` 语句，后果更直接。这就是 **SQL 注入**，常年位居安全漏洞榜首。

`PreparedStatement` 用 `?` 占位、参数单独传，参数值会被当成**数据**而不是 SQL 片段处理，注入无从谈起；顺带还能复用预编译的执行计划，批量插入时快不少：

```java
String sql = "INSERT INTO emp(name, salary) VALUES (?, ?)";
try (PreparedStatement ps = conn.prepareStatement(sql)) {
    for (int i = 0; i < 1000; i++) {
        ps.setString(1, "员工" + i);
        ps.setDouble(2, 5000 + i);
        ps.addBatch();               // 攒着
    }
    ps.executeBatch();               // 一次发过去
}
```

> 💬 前端做表单校验时，一直以为「把单引号转义一下」就安全了。真正看到 `' OR '1'='1` 把整张表拉出来那一刻才明白：防注入不是过滤特殊字符，而是**别把用户输入拼进 SQL**。PreparedStatement 不是「更好的写法」，是唯一正确的写法。

### 3.6 连接池

每次 `DriverManager.getConnection()` 都是一次完整的 TCP 握手 + 身份验证，耗时几十毫秒；高并发下频繁建连既慢又会把数据库连接数打满。生产环境一律用**连接池**：启动时先建好一批连接，用的时候借、用完还。

主流选择：**HikariCP**（Spring Boot 默认，性能最好）、**Druid**（阿里，带监控面板）。用法都是先配数据源，再从池子里 `getConnection()`：

```java
HikariConfig config = new HikariConfig();
config.setJdbcUrl("jdbc:mysql://localhost:3306/blog");
config.setUsername("app_user");
config.setPassword("App@123456");
config.setMaximumPoolSize(10);          // 池子上限，别超过数据库承受能力

DataSource ds = new HikariDataSource(config);
try (Connection conn = ds.getConnection(); ...) { ... }
```

代码几乎没变——因为池子也实现了 `DataSource`，拿到的 `Connection` 用完后 `close()` 实际是「归还」而不是真断开。

### 3.7 前端 ↔ JDBC 对照

| 环节     | 前端请求接口           | Java 连数据库                        |
| -------- | ---------------------- | ------------------------------------ |
| 一行调用 | `await axios.get(url)` | 七步，缺一不可                       |
| 配置     | axios 实例 baseURL     | JDBC URL + 账号密码                  |
| 参数传递 | `params` / `body`      | `PreparedStatement` 的 `?`           |
| 结果处理 | `res.data` 是现成对象  | `ResultSet` 一行行取                 |
| 连接复用 | 浏览器自动复用 TCP     | 必须自己搞连接池                     |
| 出错处理 | `catch(err)`           | `SQLException` 检查异常，到处 throws |
| 上生产   | 框架/浏览器兜底        | 上 ORM（MyBatis / JPA）              |

最后一行是真实体会：JDBC 是地基，但业务代码里几乎不会直接写它——`ResultSet` 到对象的映射太啰嗦了。MyBatis 做的事就是把这七步和映射全包掉，让写 SQL 从「Java 里拼字符串」回到「写 XML / 注解里的 SQL」。

## 4. 今日学习总结

1. SQL 内置函数分四类：字符串、数值、日期、流程；能在 SQL 里算的别捞到 Java 里算；
2. 两个易错：`LENGTH` 数字节、`CHAR_LENGTH` 数字符；`SUBSTR` 下标从 1 开始；
3. **别在 WHERE 里对字段用函数**（`DATE_FORMAT(created_at,...)`），索引会失效，改写成时间范围查询；
4. 流程函数对照：`IF` ↔ 三元、`IFNULL` ↔ `??`、`CASE WHEN` ↔ switch；
5. DCL 管权限：`CREATE USER` / `GRANT` / `REVOKE` / `SHOW GRANTS`；MySQL 的账号是「用户名 + 来源主机」；
6. **最小权限原则**：应用账号只给必要权限，生产绝不用 root 连库；
7. JDBC 是标准接口，实现靠厂商驱动，换库只换驱动和 URL——面向接口编程的教科书案例；
8. 七步流程：注册驱动 → 拿连接 → 建语句 → 填参 → 执行 → 遍历结果 → 关资源，资源用 try-with-resources 兜住；
9. **永远用 PreparedStatement**，`?` 占位而不是拼字符串——防 SQL 注入没有折中方案；
10. 生产必上连接池（HikariCP / Druid），再往上是 ORM。**MyBatis**，把今天这七步交给框架，专写 SQL 不写样板代码。
