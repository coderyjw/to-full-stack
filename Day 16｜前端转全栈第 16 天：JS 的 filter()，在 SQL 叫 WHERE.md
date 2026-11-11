# Day 16｜前端转全栈第 16 天：JS 的 filter()，在 SQL 叫 WHERE

上一篇把库和表建好了，数据也插进去了，今天干正事：把数据查出来。单表查询、多表连接、子查询一趟讲完，外加建表之后绕不开的三件事——备份还原、三范式、表关系。

> 💬 写前端时，数据是后端接口给的现成 JSON：拿到数组 filter、sort、reduce 一顿操作再渲染。转全栈才发现，这些活儿本来就该在数据库里一条 SQL 干完——数据库过滤、排序的效率比内存里的 JS 高得多，前端只负责展示。以前是在终点领包裹，现在要从仓库发货。

## 1. 单表查询

先准备一张示例表（后面多表查询还会用到）：

```sql
CREATE TABLE emp (
    id      INT PRIMARY KEY AUTO_INCREMENT,
    name    VARCHAR(20) NOT NULL,
    salary  DECIMAL(10,2),
    age     INT,
    dept_id INT
);
```

### 1.1 简单查询

```sql
SELECT * FROM emp;                       -- 全表（生产环境少用，列多时又慢又费内存）
SELECT name, salary FROM emp;            -- 指定列
SELECT name AS 姓名, salary * 12 AS 年薪 FROM emp;  -- 别名 + 表达式
SELECT DISTINCT dept_id FROM emp;        -- 去重
```

注意列可以直接参与运算（`salary * 12`），这在 JS 里等于 `arr.map(e => e.salary * 12)`。

### 1.2 条件查询

```sql
SELECT * FROM emp WHERE salary > 8000;
SELECT * FROM emp WHERE salary BETWEEN 5000 AND 10000;   -- 含两端
SELECT * FROM emp WHERE dept_id IN (1, 3);               -- 枚举
SELECT * FROM emp WHERE name LIKE '张%';                  -- 模糊：% 任意多个字符，_ 单个字符
SELECT * FROM emp WHERE dept_id IS NULL;                 -- 判空只能用 IS NULL，= NULL 查不到任何行
```

| JS                                         | SQL                     |
| ------------------------------------------ | ----------------------- |
| `arr.filter(e => e.salary > 8000)`         | `WHERE salary > 8000`   |
| `arr.filter(e => e.name.startsWith('张'))` | `WHERE name LIKE '张%'` |
| `[1,3].includes(e.dept_id)`                | `dept_id IN (1, 3)`     |
| `e.deptId === null / undefined`            | `IS NULL`               |

两个易错点：**`<>` 是不等于**（也支持 `!=`）；**`= NULL` 永远为假**——NULL 和任何值比较结果都是 UNKNOWN，只能 `IS NULL` / `IS NOT NULL`。

### 1.3 排序查询

```sql
SELECT * FROM emp ORDER BY salary DESC;             -- 降序（ASC 升序为默认）
SELECT * FROM emp ORDER BY dept_id ASC, salary DESC; -- 先按部门，部门内按薪资
```

对照 JS：`sort()` 是原地修改、返回同一引用；SQL 的 `ORDER BY` 不动原数据。另外 JS 的 `sort()` 默认按字符串比较（`[10, 9].sort()` 得 `[10, 9]`，经典坑），SQL 按列类型比较，数字列就是数值序。

### 1.4 聚合查询

```sql
SELECT COUNT(*) FROM emp;           -- 行数
SELECT MAX(salary), MIN(salary) FROM emp;
SELECT AVG(salary), SUM(salary) FROM emp;
SELECT COUNT(DISTINCT dept_id) FROM emp;  -- 去重计数
```

注意 **`COUNT(*)` 和 `COUNT(列)` 的区别**：前者数行数，后者该列为 NULL 的行不计数。JS 里这些都靠 reduce 手搓，SQL 里是内置函数且走优化器，快几个量级。

### 1.5 分组查询

```sql
-- 每个部门的平均薪资
SELECT dept_id, AVG(salary) FROM emp GROUP BY dept_id;

-- 平均薪资超过 8000 的部门（过滤聚合结果用 HAVING）
SELECT dept_id, AVG(salary) FROM emp GROUP BY dept_id HAVING AVG(salary) > 8000;
```

**WHERE 和 HAVING 的区别**是必考题：

|                | WHERE         | HAVING        |
| -------------- | ------------- | ------------- |
| 过滤对象       | 原始行        | 分组后的结果  |
| 执行时机       | GROUP BY 之前 | GROUP BY 之后 |
| 能否用聚合函数 | 不能          | 能            |

另外一个容易撞上的报错：MySQL 默认开启 `only_full_group_by`，SELECT 里的非聚合列必须出现在 GROUP BY 里——`SELECT name, dept_id ... GROUP BY dept_id` 会直接报错，因为每组里 name 有多个，数据库不知道取哪个。

### 1.6 分页查询

```sql
SELECT * FROM emp ORDER BY id LIMIT 0, 10;    -- 第 1 页：跳过 0 条，取 10 条
SELECT * FROM emp ORDER BY id LIMIT 10, 10;   -- 第 2 页
```

前端分页传的 `page / pageSize`，到 SQL 里换算成偏移量：`LIMIT (page - 1) * pageSize, pageSize`。数据量大了之后 `LIMIT 1000000, 10` 会慢（先扫过一百万行再丢弃），那是后话，先记住写法。

### 1.7 一条语句的执行顺序（重要）

写复杂 SQL 脑子里要有这条流水线，很多「为什么这里用不了那个字段」的问题都出在顺序上：

```text
FROM → WHERE → GROUP BY → HAVING → SELECT → ORDER BY → LIMIT
```

比如 ORDER BY 能用 SELECT 里的别名（执行在 SELECT 之后），WHERE 不能（执行在 SELECT 之前）。

## 2. 数据库的备份与还原

```bash
# 备份：导出 blog 库到 blog.sql（在终端执行，不是 mysql 提示符里）
mysqldump -u root -p blog > blog.sql

# 还原方式一：命令行导入
mysql -u root -p blog < blog.sql

# 还原方式二：先登录，再 source
mysql -u root -p
mysql> USE blog;
mysql> SOURCE /path/to/blog.sql;
```

图形化客户端（Navicat / DBeaver）里就是右键库 → 转储 SQL 文件 / 运行 SQL 文件，效果一样。

上一篇说过忘写 WHERE 清空全表的事故——**备份就是那颗后悔药**。生产环境的惯例是定时自动备份 + 异地存放，个人项目至少做到动结构（DROP/ALTER）之前手动备一份。

## 3. 数据库三范式

范式是设计表结构的规范，越往后越严格。常用的前三条：

**第一范式（1NF）：列不可再分**——每个字段只存一个原子值。

- 反例：`hobby` 列存「篮球,游泳」→ 查「喜欢篮球的人」只能 LIKE 模糊扫全表；
- 前端熟悉的反面教材：往一个字段里塞 JSON 字符串，就是违反 1NF 的典型操作。

**第二范式（2NF）：非主键列必须完全依赖主键**（针对联合主键）。

- 反例：订单明细表用（订单 id + 商品 id）联合主键，却又存了「商品名」「商品价格」——商品名只依赖商品 id，不依赖整个主键，同一商品名在表里重复一万遍，改名要改一万行。

**第三范式（3NF）：消除传递依赖**——非主键列之间不能相互依赖。

- 反例：emp 表里存了 `dept_id`，又存 `dept_name`、`dept_location`——dept_name 依赖 dept_id（传递依赖主键），部门改名照样要改一万行。正确做法：部门信息单独一张 dept 表，emp 只留 dept_id。

一句话记忆：**1NF 拆列、2NF 拆联合主键、3NF 拆表**。

前端对照：JS 对象随便嵌套、随便塞，不存在「设计范式」这回事——`{ user: { dept: { name } } }` 想怎么嵌怎么嵌；关系型数据库里每一层嵌套都要「拆成一张表 + 一个外键」。另外别走火入魔：实际项目常为查询性能做**反范式冗余**（故意存重复列免掉一次 JOIN），范式是基础不是教条。

## 4. 表与表之间的关系

| 关系   | 例子        | 实现方式                                     |
| ------ | ----------- | -------------------------------------------- |
| 一对一 | 人 ↔ 身份证 | 合成一张表；或拆两表、从表主键 = 主表主键    |
| 一对多 | 部门 ↔ 员工 | **「多」的一方存对方的外键**：emp.dept_id    |
| 多对多 | 学生 ↔ 课程 | **中间表**：存两个外键，一行代表一条选课记录 |

JS 对照：对象里嵌对象、数组套数组，关系是「摆」出来的；关系型数据库里关系是「存」出来的——全靠外键列和中间表。这是从文档思维到关系思维最大的一次转弯。

## 5. 外键约束

### 5.1 一对多：多方加外键

```sql
CREATE TABLE dept (
    id   INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(20) NOT NULL
);

CREATE TABLE emp (
    id      INT PRIMARY KEY AUTO_INCREMENT,
    name    VARCHAR(20) NOT NULL,
    dept_id INT,
    CONSTRAINT fk_emp_dept FOREIGN KEY (dept_id) REFERENCES dept(id)
);
```

外键保证**参照完整性**：emp 里不允许出现 dept 表里不存在的 dept_id；dept 里还有员工的行，默认删不掉。

配套的级联策略（可选项，默认无动作）：

- `ON DELETE CASCADE`——删部门，其员工跟着删（危险，慎用）；
- `ON DELETE SET NULL`——删部门，员工的 dept_id 置空。

**一个行业现状要交代**：互联网公司大多不用物理外键，只建普通索引列（逻辑外键），关系由应用层代码保证。原因：外键检查有性能开销、不利于分库分表、数据订正不灵活。教学场景把外键学扎实，工作里看到建表规范写着「禁用物理外键」就不奇怪了。

### 5.2 多对多：中间表两个外键

```sql
CREATE TABLE student (
    id   INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(20) NOT NULL
);

CREATE TABLE course (
    id   INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(20) NOT NULL
);

CREATE TABLE student_course (
    student_id INT,
    course_id  INT,
    PRIMARY KEY (student_id, course_id),   -- 联合主键：顺便防重复选课
    FOREIGN KEY (student_id) REFERENCES student(id),
    FOREIGN KEY (course_id)  REFERENCES course(id)
);
```

中间表的联合主键一箭双雕：既是主键，又保证「同一学生同一课程」只有一条记录。

## 6. 多表查询

数据准备：dept 表里三个部门，emp 表里若干员工，其中「后勤部」故意没有员工。

### 6.1 交叉查询（笛卡尔积）

```sql
SELECT * FROM emp, dept;
-- 或 SELECT * FROM emp CROSS JOIN dept;
```

结果 = emp 行数 × dept 行数，每一行员工和每一个部门两两配对，基本没有直接使用价值——它是所有连接查询的起点，靠 ON 条件从笛卡尔积里「筛」出有意义的数据。

### 6.2 内连接

```sql
-- 显式写法（主流）
SELECT e.name, e.salary, d.name AS dept_name
FROM emp e
INNER JOIN dept d ON e.dept_id = d.id;

-- 隐式写法（老式，效果一样）
SELECT e.name, d.name FROM emp e, dept d WHERE e.dept_id = d.id;
```

内连接只保留**两边都能对上**的行：没有部门的员工、没有员工的后勤部，都不会出现在结果里。

### 6.3 外连接

```sql
-- 左连接：左表（emp）全保留，右边对不上补 NULL
SELECT e.name, d.name AS dept_name
FROM emp e
LEFT JOIN dept d ON e.dept_id = d.id;

-- 右连接：右表（dept）全保留 —— 没有员工的后勤部也会出现，员工列为 NULL
SELECT e.name, d.name AS dept_name
FROM emp e
RIGHT JOIN dept d ON e.dept_id = d.id;
```

经典场景：「列出所有部门及其员工，**没有员工的部门也要列出**」就必须用外连接。实战里 LEFT JOIN 用得远多于 RIGHT JOIN——表顺序调换一下，RIGHT 都能改写成 LEFT，可读性更好。

JS 对照：`LEFT JOIN` 相当于 merge 时不丢弃左数组元素、缺失处补 null——JS 数组没有原生对应物，得手写循环或上 lodash。

### 6.4 UNION

```sql
SELECT name FROM emp_2025
UNION                    -- 合并并去重
SELECT name FROM emp_2026;

SELECT name FROM emp_2025
UNION ALL                -- 合并不去重，性能更好
SELECT name FROM emp_2026;
```

要求：两边 SELECT 的**列数和类型对应一致**。适用场景：两张结构相同的历史表合并查询。UNION 会去重排序（有开销），确认无重复或允许重复时用 UNION ALL。

### 6.5 子查询

查询嵌套查询，先执行内层再执行外层：

```sql
-- 标量子查询：结果是一个值
SELECT * FROM emp WHERE salary > (SELECT AVG(salary) FROM emp);

-- 列子查询：结果是一列，配合 IN
SELECT * FROM emp
WHERE dept_id IN (SELECT id FROM dept WHERE name LIKE '%部%');
```

### 6.6 子查询作为伪表（派生表）

子查询写在 FROM 里，把结果当一张临时表来用，**必须起别名**：

```sql
-- 按部门统计人数和平均薪资，再只看人数超过 2 的部门
SELECT d.name, t.cnt, t.avg_salary
FROM dept d
INNER JOIN (
    SELECT dept_id, COUNT(*) AS cnt, AVG(salary) AS avg_salary
    FROM emp
    GROUP BY dept_id
) t ON d.id = t.dept_id
WHERE t.cnt > 2;
```

套路是「先 GROUP BY 聚合成虚拟表，再和主表 JOIN 补全信息」——聚合结果里只有 dept_id，部门名得连回去取。这是业务 SQL 里出场率极高的组合拳。

### 6.7 JS ↔ SQL 对照总表

| 想做的事       | JS                    | SQL                             |
| -------------- | --------------------- | ------------------------------- |
| 过滤           | `filter()`            | `WHERE` / `HAVING`              |
| 取字段/转换    | `map()`               | `SELECT 列 / 表达式`            |
| 排序           | `sort()`              | `ORDER BY`                      |
| 聚合统计       | `reduce()`            | `COUNT / SUM / AVG / MAX / MIN` |
| 分组统计       | `groupBy()`（lodash） | `GROUP BY`                      |
| 去重           | `[...new Set()]`      | `DISTINCT`                      |
| 截取           | `slice()`             | `LIMIT`                         |
| 两数组配对合并 | 手写双重循环          | `JOIN ... ON`                   |

## 7. 今日学习总结

1. 单表查询七件事：SELECT 列、WHERE 条件、ORDER BY 排序、聚合函数、GROUP BY + HAVING、DISTINCT、LIMIT 分页；
2. **判空只能 `IS NULL`**，`= NULL` 永远查不到；模糊匹配 `%` 多字符、`_` 单字符；
3. WHERE 过滤原始行、HAVING 过滤聚合结果；MySQL 的 `only_full_group_by` 要求 SELECT 非聚合列必须出现在 GROUP BY 里；
4. 执行顺序 FROM → WHERE → GROUP BY → HAVING → SELECT → ORDER BY → LIMIT，解释了「为什么这里用不了那个别名」；
5. 备份还原是 DBA 入门功：`mysqldump 导出`、`mysql < 文件` 导入，动表结构前先备份；
6. 三范式一句话：**1NF 列不可再分、2NF 完全依赖主键、3NF 消除传递依赖**；实际项目允许为性能做反范式冗余；
7. 一对多在「多」方存外键；多对多靠中间表 + 联合主键；互联网公司常用逻辑外键，物理外键由应用层保证；
8. 内连接只留对得上的行；LEFT JOIN 左表全保留、右表补 NULL——「没有数据的主体也要列出」就用它；
9. UNION 合并结果集（去重），UNION ALL 不去重但快；派生表（FROM 里的子查询）必须起别名；
10. 一条 SQL 顶前端十行数组操作，而且快得多。表和查询都齐了，下一步是把 Java 和数据库连起来——**JDBC**，在 Java 代码里执行今天写的这些 SQL。
