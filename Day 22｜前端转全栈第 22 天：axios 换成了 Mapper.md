# Day 22｜前端转全栈第 22 天：axios 换成了 Mapper

昨天把工程那套东西搞明白了，今天终于轮到数据。

写了一堆接口，数据还是 `List.of` 里手写的，看着就假。Day 17 用 JDBC 连过库，那套代码我至今记得——加载驱动、拿连接、拼 SQL、遍历 ResultSet、一个个 set 到对象里、finally 里关连接，一个 `findById` 三十行。今天换 MyBatis，同样的事，两行。

## 1. MyBatis 是数据库版 axios

我对它的第一层理解特别朴素：**axios 是发 HTTP 请求的库，MyBatis 是发 SQL 请求的库。**

```js
// 前端要数据，调接口
axios.get('/api/users').then(res => { ... })
```

```java
// 后端要数据，调 Mapper
List<User> users = userMapper.selectAll();
```

都是「我不关心底下怎么连、怎么发、怎么收回来的，我只说要什么」。区别是 axios 对面是另一个服务，MyBatis 对面是自己的数据库。

第二层理解是 **ORM**：把数据库表和 Java 对象对起来。

| 数据库           | Java                |
| ---------------- | ------------------- |
| 一张表 `user`    | 一个类 `User`       |
| 一行记录         | 一个对象            |
| 一列 `user_name` | 一个属性 `userName` |

这件事看着不起眼，但省掉的正是 JDBC 里最烦的那段：`while (rs.next()) { user.setId(rs.getLong("id")); ... }`。表有二十个字段，这段就写二十行，加一个字段忘了改，还查半天。

## 2. 先配置：还是那个 yml

Day 21 说了配置都在 `application.yml`，MyBatis 也就加几行：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/demo?serverTimezone=Asia/Shanghai
    username: root
    password: 123456
    driver-class-name: com.mysql.cj.jdbc.Driver

mybatis:
  mapper-locations: classpath:mapper/*.xml # SQL 写在哪
  type-aliases-package: com.example.demo.entity # 实体类包，XML 里就不用写全类名
  configuration:
    map-underscore-to-camel-case: true # 下划线转驼峰
```

三个配置项各自干嘛：

- `mapper-locations`：告诉框架去哪个目录找 XML。写错了症状很统一——启动没问题，一调接口就报 `Invalid bound statement (not found)`；
- `type-aliases-package`：配了之后 XML 里 `resultType` 直接写 `User` 就行，不用写 `com.example.demo.entity.User`；
- `map-underscore-to-camel-case`：数据库 `user_name` 自动对上 `userName`。**原生 MyBatis 默认是关的**，MyBatis-Plus 默认开——这个差别我记了一下，省得以后换框架时莫名查不出数据。

## 3. Mapper 接口 + XML：一个声明，一个实现

MyBatis 最奇怪的地方在这：**Mapper 是个接口，没有实现类，但能直接调用。**

```java
@Mapper
public interface UserMapper {
    List<User> selectAll();
    User selectById(Long id);
}
```

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.example.demo.mapper.UserMapper">

    <select id="selectAll" resultType="User">
        select * from user
    </select>

    <select id="selectById" resultType="User">
        select * from user where id = #{id}
    </select>
</mapper>
```

靠两条约定把两边接起来：

- XML 的 `namespace` = 接口的**全限定名**（包名 + 类名）；
- 每个标签的 `id` = 接口里的**方法名**。

对上之后，容器启动时 MyBatis 会为这个接口**动态生成一个实现类**，方法一调用，它就按「接口名 + 方法名」去 XML 里找那段 SQL，执行，再把结果塞回对象。

> 💬 我的第一版报的是 `Invalid bound statement (not found): com.example.demo.mapper.UserMapper.selectAll`。SQL 明明写了，方法名也对。折腾半小时发现 `namespace` 写成了 `UserMapper`——省了包名。MyBatis 只认全限定名，少一个包名就整张表找不到。这个报错的字面意思是「找不到语句」，实际九成是 namespace 或 id 对不上。

前端有个很像的东西：`new Proxy(target, { get(t, prop) { ... } })`。访问 `userMapper.selectAll` 的时候，被拦截了一下，转成另一件事去做。MyBatis 这边拦截之后，转成的是一次 SQL 执行。

## 4. 增删改查和 `#{}` 这个坑

```xml
<insert id="insert" useGeneratedKeys="true" keyProperty="id">
    insert into user (user_name, age) values (#{userName}, #{age})
</insert>

<update id="update">
    update user set user_name = #{userName}, age = #{age} where id = #{id}
</update>

<delete id="deleteById">
    delete from user where id = #{id}
</delete>
```

`useGeneratedKeys` 那句是拿自增主键：插入完，`user.id` 会被回填，不用再查一次。

**`#{}` 和 `${}` 一定要分清**，这是今天最有价值的一条：

| 写法    | 实际做的事                                 | 安全性                    |
| ------- | ------------------------------------------ | ------------------------- |
| `#{id}` | 变成 `?` 占位符，用 PreparedStatement 传参 | 安全，参数当值处理        |
| `${id}` | 直接把字符串拼进 SQL                       | 危险，`1 or 1=1` 能拼进去 |

`${}` 就是前端那个反面教材：

```js
// 千万别这么拼
db.query(`select * from user where id = ${id}`);
```

`#{}` 相当于用参数化查询，SQL 结构在编译期就定了，参数只是值，塞不进语句结构里。所以除了「表名、排序字段这种没法用占位符的地方」，其余一律 `#{}`。

## 5. 结果集映射：字段名对不上怎么办

如果数据库字段和属性风格一致（都叫 `userName`），`resultType="User"` 就够了。但真实库里基本全是下划线，所以有了前面那个 `map-underscore-to-camel-case`。

碰上更离谱的——字段叫 `u_name`、`dept_id`，或者要拼多表——就得手写映射：

```xml
<resultMap id="userMap" type="User">
    <id column="u_id" property="id"/>
    <result column="u_name" property="userName"/>
    <result column="dept_name" property="deptName"/>   <!-- 联表查出来的 -->
</resultMap>

<select id="selectWithDept" resultMap="userMap">
    select u.id as u_id, u.name as u_name, d.name as dept_name
    from user u left join dept d on u.dept_id = d.id
</select>
```

写 `resultMap` 有点像在写 `res.data.map(item => ({ id: item.u_id, userName: item.u_name }))`——把后端的原始结构，挨个映射成想要的样子。

## 6. 动态 SQL：终于不用拼字符串了

前端拼查询条件，一般是这么写的：

```js
let sql = "select * from user where 1=1";
if (name) sql += ` and name like '%${name}%'`;
if (age) sql += ` and age = ${age}`;
```

那个 `where 1=1` 是拼接界的古老智慧，为的是后面随便 `and` 都不报错。MyBatis 的动态 SQL 就是把这套搬进 XML，但不用自己管 `where 1=1` 和多余的 `and`：

```xml
<select id="search" resultType="User">
    select * from user
    <where>
        <if test="name != null and name != ''">
            and user_name like concat('%', #{name}, '%')
        </if>
        <if test="age != null">
            and age = #{age}
        </if>
        <if test="ids != null and ids.size() > 0">
            and id in
            <foreach collection="ids" item="id" open="(" separator="," close=")">
                #{id}
            </foreach>
        </if>
    </where>
</select>
```

`<where>` 会自己判断：里面有条件就加 `where`，并且把开头多出来的 `and` 吃掉；一个条件都没有，就整段不加。`<foreach>` 负责 `in (...)` 那种循环拼接，跟 `ids.map(id => '?').join(',')` 是一回事，只是它顺手把参数也处理好了。

另外 `<set>` 管 update 时的尾逗号，`<choose>` 相当于 `if/else if`。用下来就一个感受：以前拼接拼到半夜的地方，现在是几行标签。

## 7. 分页：手写一遍才知道有多烦

不用插件的话，一次分页要两条 SQL：

```xml
<select id="page" resultType="User">
    select * from user order by id desc limit #{offset}, #{size}
</select>

<select id="count" resultType="long">
    select count(*) from user
</select>
```

两条还不算完，`offset` 得自己算（`(pageNum - 1) * pageSize`），总数也得自己带回去给前端。分页一多，全是重复代码。

所以实际项目里都用插件：老一点的是 PageHelper，`PageHelper.startPage(1, 10)` 下一句查询就自动被改写成带 limit 的，还会顺手 count。MyBatis-Plus 里换成 `PaginationInnerInterceptor`，用法是传一个 `Page` 对象进去。

## 8. MyBatis-Plus：把单表 CRUD 全免了

写到这儿我发现一件事：`selectAll`、`selectById`、`insert`、`update`、`deleteById` 这五个方法，每张表都要写一遍，XML 也一模一样，只是表名不同。这就是 MyBatis-Plus 要解决的事。

它不替代 MyBatis，是**增强**——底层还是 MyBatis，只是把重复的部分封装掉了。

```java
public interface UserMapper extends BaseMapper<User> {
    // 什么都不用写
}
```

继承 `BaseMapper` 之后，`insert`、`deleteById`、`updateById`、`selectById`、`selectList` 全都有了。实体类上标一下对应的表：

```java
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)   // 自增主键
    private Long id;
    private String userName;
    private Integer age;
}
```

条件查询也不用写 SQL，用 `LambdaQueryWrapper` 链式拼：

```java
List<User> list = userMapper.selectList(
    new LambdaQueryWrapper<User>()
        .like(StringUtils.hasText(name), User::getUserName, name)
        .gt(age != null, User::getAge, age)
        .orderByDesc(User::getId)
);
```

这个写法前端应该很眼熟——knex、Prisma 那种链式查询构造器：

```js
knex("user").where("age", ">", 18).orderBy("id", "desc");
```

第一个参数是条件开关（`StringUtils.hasText(name)` 为 false 时这个条件直接不拼），等于把 `<if>` 那套搬进了 Java 代码里。好处是不用去 XML 里翻，IDE 还能提示字段名、改字段名能全局重构，比手写字符串 `"user_name"` 安全得多。

分页也是配置一个插件就完事：

```java
@Bean
public MybatisPlusInterceptor mybatisPlusInterceptor() {
    MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
    interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
    return interceptor;
}
```

```java
Page<User> page = userMapper.selectPage(new Page<>(1, 10), wrapper);
page.getTotal();    // 总数
page.getRecords();  // 当前页数据
```

## 9. 两套怎么选

我原本以为用了 Plus 就不用碰 MyBatis 了，看了一圈发现不是：

- **单表增删改查、条件拼接** → MyBatis-Plus，一行不写，全自动；
- **多表联查、复杂统计、窗口函数那种** → 回 XML 手写 SQL。Plus 也支持在 XML 里写，`BaseMapper` 和自定义方法是共存的；
- 团队里老项目多半是纯 MyBatis，面试问的也多是 `#{}` 和 `${}`、动态 SQL 这些底子。

所以顺序还是得先把 MyBatis 搞明白，Plus 只是省事，不改变「SQL 长什么样」。

## 10. 今日学习总结

1. MyBatis 就是数据库版 axios：我说要什么，它负责连库、发 SQL、把结果搬回来。
2. ORM = 表对类、行对对象、列对属性；省掉的是 JDBC 里那段 `while(rs.next())` 手写赋值。
3. Mapper 是接口、没有实现类，靠 `namespace` = 接口全限定名、`id` = 方法名这两条约定，由动态代理在运行时生成实现。
4. `Invalid bound statement (not found)` 九成是 namespace 少了包名，或者 id 和方法名不一致，不是 SQL 写错了。
5. `#{}` 是占位符、参数化，安全；`${}` 是字符串拼接，等于前端模板字符串直接插变量，能注入。除了表名/排序字段，一律 `#{}`。
6. 结果集映射：`map-underscore-to-camel-case` 搞定下划线转驼峰（原生 MyBatis 默认关，Plus 默认开），对不上就手写 `resultMap`。
7. 动态 SQL 的 `<where>` 自己处理 `where` 和多余的 `and`，`<foreach>` 管 `in (...)`，再也不用写 `where 1=1`。
8. 分页手写要两条 SQL 加自己算 offset，实际都用插件；MyBatis-Plus 里配个 `PaginationInnerInterceptor`。
9. MyBatis-Plus 用 `BaseMapper` 免掉单表 CRUD，`LambdaQueryWrapper` 把条件写成链式（像 knex/Prisma），但复杂 SQL 还是回 XML。

数据这边通了，接口终于能查真东西了。不过现在这些接口谁来都能调——下一篇把 Day 18 学的 Session 捡回来，配上拦截器，看看「没登录就别想看」这件事在后端是怎么拦的。
