# Day 15｜前端转全栈第 15 天：数据从 localStorage 搬进 MySQL

从今天起系列进入后端的正戏：数据库。前面 14 天写的 Java 程序，数据都是活在内存里的——程序一停，全没了。今天开始，数据要住进「关机也不丢」的地方。

> 💬 写前端 6 年，我存数据的手段就三样：内存变量、localStorage、cookie——上限 5MB，关了隐私窗口就蒸发，用户清个缓存就跑路。第一次意识到后端要管「百万用户的数据、断电不能丢、还得扛并发」时，才明白为什么数据库是一门独立的学问。localStorage 是便签纸，数据库是仓库。

## 1. 数据库是什么

### 1.1 三个概念：DB、DBMS、表

| 概念           | 全称          | 是什么                                   | 前端类比                              |
| -------------- | ------------- | ---------------------------------------- | ------------------------------------- |
| 数据库         | Database (DB) | 按结构组织、存在磁盘上的数据仓库         | 一个大号 localStorage 分区            |
| 数据库管理系统 | DBMS          | **管理数据库的软件**，MySQL 就是其中一种 | 浏览器（localStorage 是它提供的功能） |
| 表             | Table         | 数据按行列组织的基本单位                 | 一张结构固定的 Excel 表               |

注意一个常见混用：日常口语里「数据库」经常指 MySQL 这类软件，严格说 MySQL 是 **DBMS**，真正的数据存在它管理的 database 里。

**关系型 vs 非关系型**一句话版：MySQL、PostgreSQL、Oracle 这类用「表」存数据、数据之间有关系的是关系型（SQL）；MongoDB、Redis 这类自由格式的是非关系型（NoSQL）。全栈第一站选 MySQL：免费、用得最多、教程好找。

### 1.2 表长什么样

```text
user 表
+----+----------+------+---------------------+
| id | name     | age  | created_at          |
+----+----------+------+---------------------+
| 1  | 张三     | 25   | 2026-11-11 09:00:00 |
| 2  | 李四     | 30   | 2026-11-11 09:05:00 |
+----+----------+------+---------------------+
```

- **列（字段/column）**：定义了数据有哪几项、什么类型——像一张表的表头，建表时就定死；
- **行（记录/row）**：一条完整的数据——一个用户的全部信息。

### 1.3 数据库和 Java 类的对应关系（本篇最重要的一张表）

| 数据库世界  | Java 世界              |
| ----------- | ---------------------- |
| database 库 | 项目（一个项目一个库） |
| table 表    | 类（JavaBean）         |
| 一行记录    | 一个对象               |
| 一列字段    | 一个成员变量           |

**JavaBean** 就是「数据类」的规范写法：

- 成员变量全部 `private`；
- 每个变量配 public 的 getter/setter；
- 有无参构造器（有些框架要求）。

```java
public class User {
    private Integer id;
    private String name;
    private Integer age;

    public User() {}
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
}
```

它和 user 表**一字段对一字段**：id 对 id、name 对 name。开发流程是——先设计表结构，再照着表写 JavaBean，中间用框架（MyBatis、JPA）自动做「一行记录 ↔ 一个对象」的转换。

前端对照说清楚：**TS 的 interface 只是编译期的类型标注，编译完就没了；JavaBean 是真类**，带行为（getter/setter）、能 new、能被框架反射读取（正好用上 Day 14 的知识——MyBatis 就是反射调 getter 取值的）。另外注意上面用了 `Integer` 而不是 `int`：表里的字段可能是 NULL，基本类型 int 装不下 null，得用包装类（Day 09 讲过Integer 缓存，这里填一个使用场景）。

## 2. MySQL8 安装

照着菜鸟教程走即可：**https://www.runoob.com/mysql/mysql-install.html**（Windows 用 MSI 安装包一路下一步，macOS 直接装 DMG 或 `brew install mysql`）。

装完验证，终端敲：

```sql
mysql -u root -p
# 输入安装时设置的密码，看到 mysql> 提示符就成功了
```

强烈建议再装个图形化客户端：**Navicat**（付费）、**DataGrip**（付费）、**DBeaver**（免费）任选。写 SQL 犹如前端离不开 DevTools——命令行能跑，但看表结构、调试效率天差地别。

## 3. SQL 语言

### 3.1 介绍与分类

SQL（Structured Query Language，结构化查询语言）是操作关系型数据库的**标准语言**——学一次，MySQL、PostgreSQL、Oracle 通吃。这是它和前端语言体验完全不同的地方：不存在「换数据库就换语言」。

SQL 按功能分四类，这四个缩写后面会反复出现：

| 分类    | 全称                       | 管什么                            | 代表关键字               |
| ------- | -------------------------- | --------------------------------- | ------------------------ |
| **DDL** | Data Definition Language   | 数据库/表的**结构**（建、改、删） | CREATE / ALTER / DROP    |
| **DML** | Data Manipulation Language | 表里的**数据**（增、删、改）      | INSERT / UPDATE / DELETE |
| **DQL** | Data Query Language        | **查**数据                        | SELECT                   |
| **DCL** | Data Control Language      | 权限控制                          | GRANT / REVOKE           |

一句话区分 DDL 和 DML：**DDL 动的是「房子」（表结构），DML 动的是「家具」（表里的数据）**。本篇讲 DDL + DML，SELECT 查询内容多，留给下一篇。

### 3.2 通用语法

- 语句以 `;` 结尾，可以随便换行——所以正式写 SQL 都是多行排版；
- 关键字不区分大小写，但**惯例关键字大写**、表名字段名小写；
- 注释：`-- 单行注释`（注意是两个横杠，和 JS 的 `//` 不同）和 `/* 多行 */`；
- 库名、表名别用中文和关键字，遇到特殊字符用反引号包住（键盘 Esc 下面那个键）。

### 3.3 SQL 中的数据类型

前端被 JS 的「number 一把梭」宠坏了，SQL 里类型必须精确定义，**建表时选错类型，后面全是坑**。常用类型如下：

| 类型           | 说明                   | 前端注意点                                                                 |
| -------------- | ---------------------- | -------------------------------------------------------------------------- |
| `int`          | 整数                   | 对应 Java 的 int/Integer                                                   |
| `bigint`       | 大整数                 | **雪花 ID、分布式 ID 都用它**（int 存不满 11 位手机号都够呛）              |
| `double`       | 浮点数                 | 有精度问题                                                                 |
| `decimal(m,d)` | 定点小数               | **金额必须用它**！0.1+0.2 不等于 0.3 的老朋友（Day 02 埋的坑在这又出现了） |
| `varchar(n)`   | 可变长字符串，最长 n   | 最常用；n 是字符数不是字节数                                               |
| `char(n)`      | 定长字符串，不够补空格 | 手机号、身份证这种长度固定的用它                                           |
| `text`         | 长文本                 | 文章正文、JSON 串                                                          |
| `date`         | 只有日期               | 2026-11-15                                                                 |
| `datetime`     | 日期+时间              | 2026-11-15 10:30:00，**业务标配**                                          |
| `timestamp`    | 时间戳，会随时区变     | 2038 年问题，新项目用 datetime 稳妥                                        |

对照记忆：JS 的 `number` 在这里拆成了 int/bigint/double/decimal 四家；JS 的 string 拆成了 varchar/char/text，而且 **varchar 必须声明最大长度**——JS 字符串随便长，数据库的字符串是「先交钱再入住」。

## 4. MySQL 语句：DDL + DML

### 4.1 库操作

```sql
-- 创建数据库（if not exists 防止重名报错；utf8mb4 支持 emoji，别用 utf8！）
CREATE DATABASE IF NOT EXISTS blog DEFAULT CHARSET utf8mb4;

SHOW DATABASES;          -- 看所有库
USE blog;                -- 切换到这个库（之后的操作都在它里面）
SELECT DATABASE();       -- 看当前在哪个库
DROP DATABASE blog;      -- 删库（跑路专用，慎用）
```

一个前端不知道的坑：MySQL 的 `utf8` 编码**最多 3 字节，存不了 emoji**（emoji 要 4 字节）。用户昵称带个 😀 直接报错。所以无脑用 `utf8mb4`，mb4 = most bytes 4。

### 4.2 表操作

```sql
CREATE TABLE IF NOT EXISTS user (
    id       INT PRIMARY KEY AUTO_INCREMENT,  -- 主键 + 自增，下一节细讲
    name     VARCHAR(20) NOT NULL,
    age      INT,
    email    VARCHAR(50) UNIQUE,
    money    DECIMAL(10,2) DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

SHOW TABLES;         -- 看当前库所有表
DESC user;           -- 看表结构（describe）
DROP TABLE user;     -- 删表

-- 改表结构
ALTER TABLE user ADD COLUMN phone CHAR(11);       -- 加列
ALTER TABLE user MODIFY COLUMN name VARCHAR(50);  -- 改类型
ALTER TABLE user RENAME COLUMN phone TO mobile;   -- 改列名
```

建表语句是 DDL 的核心，读法：**字段名 + 类型 + 约束**，逗号隔开。这份示例用到了下一节的全部约束，后面逐个解释。

### 4.3 数据操作（DML）

```sql
-- 增：指定列插入（推荐，没写的列走默认值/自增）
INSERT INTO user (name, age, email) VALUES ('张三', 25, 'zs@test.com');

-- 一次插多条
INSERT INTO user (name, age) VALUES ('李四', 30), ('王五', 28);

-- 改：UPDATE 一定带 WHERE！
UPDATE user SET age = 26 WHERE id = 1;

-- 删：DELETE 一定带 WHERE！
DELETE FROM user WHERE id = 3;
```

> ⚠️ **UPDATE / DELETE 忘写 WHERE 会作用于全表**——一行 SQL 把几百万条数据全改了/全删了，且没有 Ctrl+Z。生产环境的标准防呆姿势：先写 `WHERE id = 1` 的 SELECT 确认目标，再把 SELECT 改成 UPDATE/DELETE 执行；或者开客户端的 safe update 模式。

> 💬 前端删错了顶多 localStorage.clear()， reload 一下就回来了；我第一次听说「没带 where 的 delete 把生产表清空」这种事故时，才理解为什么 DBA 改数据前都要先备份。数据库里每条 UPDATE/DELETE 都值得多看三秒。

顺带区分三个「删」：`DELETE`（按条件删数据，可回滚）、`TRUNCATE TABLE`（清空全表，快，不可回滚）、`DROP TABLE`（连表带数据一起删，结构都没了）。

## 5. 约束

约束是**数据库层把关的规则**：往表里塞不合规矩的数据，直接报错拒绝。这补上了前端认知里的一个空缺——JS 里校验全靠前端代码，用户关掉 JS 或直接发请求就能绕过；数据库约束是**绕不过去的最后防线**。

### 5.1 主键约束 PRIMARY KEY

- 唯一 + 非空，一张表只能有一个主键（联合主键此处不展开）；
- 作用是给每行数据一个**身份证号**，定位、更新、关联全靠它；
- 惯例：自增整数 id。

前端对照：JS 对象的 key 想重复就重复，数组的 index 会随删增移动——从没有过「永不重复、永不变化」的行标识，而这恰恰是数据库设计的基石。

### 5.2 自增长约束 AUTO_INCREMENT

```sql
id INT PRIMARY KEY AUTO_INCREMENT
```

插入时不写 id，数据库自动发号：1、2、3……只增不减，删掉的号不回收。

前端对照：前端的 id 是自己造的——`Date.now()`、`uuid`、`nanoid()`；数据库的自增相当于**有一个官方发号器**，只管插数据，号码由它来发。注意分布式多库场景自增会撞号，那时才轮到雪花 ID（bigint 存的就是它）。

### 5.3 非空约束 NOT NULL

该列必须给值，插 NULL 直接报错。前端对照：相当于把 HTML 的 `required` 校验下沉到了数据库层，而且**绕不过去**。

经验法则：业务上必有值的字段（用户名、创建时间）都加 NOT NULL，让错误在写入时就暴露，而不是查询时查出一堆 null 再在代码里防。

### 5.4 唯一约束 UNIQUE

该列的值全表不能重复（NULL 除外，多个 NULL 允许）。手机号、邮箱、身份证号都是标配。

前端对照：注册时「该手机号已被注册」的检查，前端的防重复提示只是体验，真正保证不重复的是数据库的 UNIQUE 约束——两个请求同时提交，代码层可能都查到「不存在」，数据库约束会拦住第二个。

### 5.5 综合示例

```sql
CREATE TABLE account (
    id         INT PRIMARY KEY AUTO_INCREMENT,  -- 主键 + 自增
    username   VARCHAR(20) NOT NULL,            -- 必填
    phone      CHAR(11) NOT NULL UNIQUE,        -- 必填且不重复
    balance    DECIMAL(10,2) NOT NULL DEFAULT 0,-- 金额用 decimal
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);
```

| 前端的做法                  | 数据库的做法              |
| --------------------------- | ------------------------- |
| `uuid()` / `nanoid()` 造 id | `AUTO_INCREMENT` 自动发号 |
| 表单 `required` 校验        | `NOT NULL`                |
| 提交前查重接口              | `UNIQUE`                  |
| `Number` 存金额             | `DECIMAL(10,2)`           |

记住分工：**前端校验管体验（即时提示），数据库约束管底线（谁也绕不过）**，两层都要有。

## 6. 今日学习总结

1. **DBMS 是软件（MySQL），DB 是数据，表是基本单位**——行列结构，行=记录、列=字段；
2. **表↔JavaBean、行↔对象、列↔成员变量**——先建表再写类，MyBatis/JPA 负责转换；
3. JavaBean 规范：private 字段 + getter/setter + 无参构造；表字段可能为 NULL 时用包装类 Integer 而不是 int；
4. SQL 四分类：**DDL 动结构（CREATE/ALTER/DROP），DML 动数据（INSERT/UPDATE/DELETE），DQL 查（SELECT），DCL 管权限**；
5. 语法：分号结尾、关键字惯例大写、注释是 `--` 不是 `//`；
6. 类型别选错：**金额必须 DECIMAL**，ID 用 BIGINT，字符串 varchar 要定长，时间用 DATETIME，编码无脑 utf8mb4（MySQL 的 utf8 存不了 emoji）；
7. **UPDATE/DELETE 必带 WHERE**——忘写的代价是全表，先 SELECT 确认再改；
8. 三个删的区别：DELETE 按条件可回滚、TRUNCATE 清空全表、DROP 连表都删；
9. 四大约束：PRIMARY KEY（唯一+非空，行身份证）、AUTO_INCREMENT（官方发号器）、NOT NULL（必填底线）、UNIQUE（全表防重）；
10. 前端校验是体验、数据库约束是底线，两层缺一不可。表建好了、数据能插了，下一篇就是 SQL 的重头戏——**SELECT 查询**：条件、排序、分页、聚合，一条语句把数据玩出花。
