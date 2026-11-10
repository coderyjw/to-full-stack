# Day 12｜前端转全栈第 12 天：JS 的 arr.map()，在 Java 叫 Stream

Day 11 把数据装进了 List/Set/Map，但真正写业务时，一半的代码是"从一堆数据里筛出想要的"。在 JS 里这就是 `arr.filter().map().reduce()` 一条链的事，Java 里昨天的 List 一个都没有——它把这件事交给了 **Stream**。

> 💬 第一次看到 `list.stream().filter(x -> ...).collect(Collectors.toList())`，我第一反应是：不就一个 filter 吗，为什么要 `stream()` 又要 `collect()`，前后各挂一串？写了几天才明白：JS 的链式调用是"每一步都生成一个新数组"，Java 的 Stream 是"先把流水线搭好，最后开机一次跑完"。这个差别决定了性能，也决定了你能不能在中间随便插 `distinct()`、`limit()`。

## 1. Stream 是什么

它不是集合，也不是数据结构，**它不存数据**。可以理解成"给数据源套的一条流水线"：

```
数据源（集合 / 数组 / 文件 / 生成器）
        ↓  stream()
   [ 中间操作：filter → map → sorted … ]   ← 搭流水线，不执行
        ↓  终端操作（collect / forEach / count）
      结果
```

四个必须记住的特点：

| 特点         | 含义                                           |
| ------------ | ---------------------------------------------- |
| 不存储数据   | 数据还在原集合里，Stream 只是"视图 + 操作"     |
| 不修改数据源 | 所有操作都返回新的流，原集合一个元素都不会变   |
| 惰性求值     | 中间操作只是"登记"，没有终端操作就一行都不执行 |
| 只能消费一次 | 一个流调过终端操作就废了，再用直接抛异常       |

> 最后两条是面试高频，第 5 节专门用代码验证。

## 2. 三步走

```java
List<String> words = new ArrayList<>(List.of("java", "js", "rust", "go", "python"));

List<String> result = words.stream()                       // 1. 创建流
        .filter(w -> w.length() > 2)                        // 2. 中间操作（可多个）
        .map(String::toUpperCase)                           //    中间操作
        .sorted()                                           //    中间操作
        .collect(Collectors.toList());                      // 3. 终端操作（只能一个）

System.out.println(result);   // [JAVA, PYTHON, RUST]
```

### 2.1 创建流的常见方式

| 数据源       | 写法                                       |
| ------------ | ------------------------------------------ |
| 集合         | `list.stream()` / `set.stream()`           |
| 数组         | `Arrays.stream(arr)`                       |
| 一堆现成的值 | `Stream.of("a", "b", "c")`                 |
| 数字区间     | `IntStream.range(1, 10)`（1~9，不含 10）   |
| Map          | 没有直接方法，先 `map.entrySet().stream()` |

### 2.2 中间操作 vs 终端操作

| 类型     | 特点                     | 常见方法                                                                          |
| -------- | ------------------------ | --------------------------------------------------------------------------------- |
| 中间操作 | 返回 Stream，可以继续接  | `filter` `map` `flatMap` `distinct` `sorted` `limit` `skip` `peek`                |
| 终端操作 | 返回具体结果，流到此为止 | `collect` `forEach` `count` `reduce` `max` `min` `anyMatch` `findFirst` `toArray` |

口诀：**返回 Stream 的就是中间操作，返回值"不是 Stream"的就是终端操作**。

## 3. 中间操作逐个过

| 方法                       | 作用                         | JS 里的对应                |
| -------------------------- | ---------------------------- | -------------------------- |
| `filter(Predicate)`        | 过滤                         | `filter`                   |
| `map(Function)`            | 一对一转换                   | `map`                      |
| `flatMap(Function)`        | 一对多，把嵌套摊平           | `flatMap`                  |
| `distinct()`               | 去重（靠 hashCode+equals）   | `[...new Set(arr)]`        |
| `sorted()` / `sorted(cmp)` | 排序（返回新流，不动原集合） | `sort`（**原地改数组**）   |
| `limit(n)` / `skip(n)`     | 取前 n 个 / 跳过前 n 个      | `slice(0, n)` / `slice(n)` |
| `peek(Consumer)`           | 偷看一眼，调试用             | 无                         |

几个例子：

```java
// map：一对一，String → int
List<Integer> lengths = words.stream().map(String::length).collect(Collectors.toList());

// flatMap：把"多个小集合"摊成一个流，等价于 arr.flat()
List<List<Integer>> nested = List.of(List.of(1, 2), List.of(3, 4));
List<Integer> flat = nested.stream()
        .flatMap(List::stream)
        .collect(Collectors.toList());          // [1, 2, 3, 4]

// distinct：底层还是 hashCode + equals（Day 09 那套），自定义对象要重写
List<String> unique = words.stream().distinct().collect(Collectors.toList());

// sorted：默认自然排序，要求元素实现 Comparable，否则传 Comparator
List<String> byLen = words.stream()
        .sorted((a, b) -> Integer.compare(a.length(), b.length()))
        .collect(Collectors.toList());          // [js, go, java, rust, python]
```

> 注意 `sorted()` 和 JS 的 `sort()` 完全不同：JS 是**原地排序**，改的是原数组；Java 的 `sorted()` 返回一条新流，原集合纹丝不动。这个"不修改数据源"的约束贯穿整个 Stream。

## 4. 终端操作：拿结果的地方

### 4.1 collect：最常用

`collect` 是"把流收进容器"，靠 `Collectors` 工具类指定收成什么：

```java
List<String> toList  = words.stream().filter(w -> w.length() > 2).collect(Collectors.toList());
Set<String>  toSet   = words.stream().collect(Collectors.toSet());
String       joined  = words.stream().collect(Collectors.joining(", ", "[", "]"));
// 输出：[java, js, rust, go, python]
```

`Collectors` 里最值钱的三个（前端天天用 lodash 的应该很眼熟）：

```java
List<String> words = List.of("java", "js", "rust", "go", "python");

// 1. 分组：等价于 lodash 的 groupBy
Map<Integer, List<String>> byLength = words.stream()
        .collect(Collectors.groupingBy(String::length));
// {2=[js, go], 4=[java, rust], 6=[python]}

// 2. 分组 + 统计个数
Map<Integer, Long> countByLength = words.stream()
        .collect(Collectors.groupingBy(String::length, Collectors.counting()));

// 3. 转 Map：key 是单词，value 是长度
Map<String, Integer> lenMap = words.stream()
        .collect(Collectors.toMap(w -> w, String::length));
```

`toMap` 有两个必须知道的坑：

```java
// 坑 1：key 重复直接抛 IllegalStateException（不像 put 会覆盖）
List<String> dup = List.of("a", "a");
dup.stream().collect(Collectors.toMap(w -> w, String::length));  // 抛异常

// 正确姿势：传第三个参数指定"冲突了留谁"
dup.stream().collect(Collectors.toMap(w -> w, String::length, (oldV, newV) -> newV));

// 坑 2：value 为 null 会抛 NullPointerException（toMap 内部用 Map.merge，不接受 null）
```

### 4.2 其他终端操作

| 方法                             | 作用                         | JS 里的对应       |
| -------------------------------- | ---------------------------- | ----------------- |
| `count()`                        | 个数                         | `length`          |
| `forEach(Consumer)`              | 逐个处理，没有返回值         | `forEach`         |
| `max(cmp)` / `min(cmp)`          | 最大 / 最小，返回 `Optional` | 手写 `reduce`     |
| `reduce(初始值, BinaryOperator)` | 归约成一个值                 | `reduce`          |
| `anyMatch(Predicate)`            | 有一个满足就 true            | `some`            |
| `allMatch` / `noneMatch`         | 全部满足 / 全都不满足        | `every` / `!some` |
| `findFirst()` / `findAny()`      | 取第一个，返回 `Optional`    | `find`            |
| `toArray()`                      | 转数组                       | —                 |

```java
List<Integer> amounts = List.of(199, 39, 599, 89, 1200);

int total = amounts.stream().mapToInt(Integer::intValue).sum();          // 2126
OptionalInt max = amounts.stream().mapToInt(Integer::intValue).max();    // OptionalInt[1200]
double avg = amounts.stream().mapToInt(Integer::intValue).average().orElse(0);

boolean hasBig = amounts.stream().anyMatch(a -> a > 1000);   // true
long cheapCount = amounts.stream().filter(a -> a < 100).count();  // 1
```

`reduce` 是"万物归约"的兜底方案，能把整条流压成一个值：

```java
int sum = Stream.of(1, 2, 3, 4).reduce(0, Integer::sum);        // 10（0 是初始值）
Optional<Integer> maxOpt = Stream.of(1, 5, 3).reduce(Integer::max);  // 没有初始值 → Optional
```

## 5. 惰性求值和"只能消费一次"

这是 Stream 最反直觉的两点，也是最容易写出 bug 的地方。

### 5.1 没有终端操作，中间操作一行都不执行

```java
Stream<String> stream = words.stream().filter(w -> {
    System.out.println("filter 执行了：" + w);
    return w.length() > 2;
});

System.out.println("流已经建好了，但上面那行 filter 一次都没跑");
stream.forEach(System.out::println);   // 到这里才开始真正执行
```

输出顺序是：

```
流已经建好了，但上面那行 filter 一次都没跑
filter 执行了：java
JAVA
filter 执行了：js        ← 没通过，直接跳过
filter 执行了：rust
RUST
...
```

> 💬 这个特性其实很香：`filter` 之后再 `limit(3)`，Java 拿到第 3 个满足条件的元素就停了，不会把整个集合扫一遍。JS 的 `filter().slice(0, 3)` 是先把整条数组过滤完再切。数据量一大，差别就出来了——第一次真正理解"惰性"的用处。

### 5.2 一个流只能用一次

```java
Stream<String> s = words.stream();
System.out.println(s.count());   // 5
System.out.println(s.count());   // java.lang.IllegalStateException: stream has already been operated upon or closed
```

流内部记着"我跑到哪了 / 我关没关"，二次消费直接报错。想要就重新 `words.stream()`。

### 5.3 想在中间看一眼：`peek`

```java
List<String> res = words.stream()
        .filter(w -> w.length() > 2)
        .peek(w -> System.out.println("过滤后：" + w))   // 调试用，别写业务逻辑
        .map(String::toUpperCase)
        .collect(Collectors.toList());
```

`peek` 也是中间操作，同样要等终端操作才执行——所以"加个 peek 看看为什么没执行"的答案通常是：**你忘了写终端操作**。

## 6. 和 JS 数组方法对照表

| 想做什么     | JS                               | Java Stream                                          |
| ------------ | -------------------------------- | ---------------------------------------------------- |
| 过滤         | `arr.filter(fn)`                 | `.filter(x -> ...)`                                  |
| 转换         | `arr.map(fn)`                    | `.map(x -> ...)`                                     |
| 摊平         | `arr.flatMap(fn)`                | `.flatMap(x -> ...)`                                 |
| 去重         | `[...new Set(arr)]`              | `.distinct()`                                        |
| 排序         | `arr.sort(cmp)`（原地改）        | `.sorted(cmp)`（返回新流）                           |
| 求和         | `arr.reduce((a, b) => a + b, 0)` | `.mapToInt(...).sum()` 或 `.reduce(0, Integer::sum)` |
| 找第一个     | `arr.find(fn)`                   | `.filter(...).findFirst()`                           |
| 有没有满足的 | `arr.some(fn)`                   | `.anyMatch(...)`                                     |
| 是否全部满足 | `arr.every(fn)`                  | `.allMatch(...)`                                     |
| 拼接成字符串 | `arr.join(', ')`                 | `Collectors.joining(", ")`                           |
| 截取前 n 个  | `arr.slice(0, n)`                | `.limit(n)`                                          |
| 分组         | 没有原生，要 lodash `groupBy`    | `Collectors.groupingBy(...)`                         |

**Stream 做不到、JS 数组能做的事**，也顺手记一下：

- **按下标取元素**：Stream 没有"第 3 个"这种概念，要下标就别用流，老老实实 `List.get(3)`；
- **原地增删**：`splice` 这类操作 Stream 完全没有，流的哲学就是"不改数据源"；
- **复用**：JS 数组方法链的中间结果还能再给别的逻辑用，流过了终端操作就废了。

## 7. 一个真实的改造：for 循环 → Stream

需求：从单词列表里筛出长度大于 2 的，转大写，按字母序排，收集成 List。

**改造前（Java 8 之前的写法）**

```java
List<String> result = new ArrayList<>();
for (String w : words) {
    if (w.length() > 2) {
        result.add(w.toUpperCase());
    }
}
Collections.sort(result);
```

**改造后**

```java
List<String> result = words.stream()
        .filter(w -> w.length() > 2)
        .map(String::toUpperCase)
        .sorted()
        .collect(Collectors.toList());
```

行数少了一半，读起来就是"筛 → 转 → 排 → 收"四步，和业务描述的顺序一致。

但也别走火入魔：

- **循环里有 `break` / `continue` / 复杂分支**：流写起来比 for 更难读，就用 for；
- **只是遍历打印**：`list.forEach(System.out::println)` 可以，但换成 for 也不会错；
- **需要下标、需要在循环里改多个外部变量**：流不合适（lambda 捕获的变量必须是 final，Day 10 说过）。

## 8. 几个容易踩的坑

1. **流只能消费一次**：报 `IllegalStateException` 时先检查是不是把流存进了变量又用了两次；
2. **不写终端操作，中间操作全不执行**：特别是 `peek` 调试时最容易自己骗自己；
3. **`toMap` 的 key 重复 / value 为 null 都会炸**：要么传 merge 函数，要么先 `filter(Objects::nonNull)`；
4. **`findFirst()` 返回 `Optional`，别直接 `get()`**：没有值时会抛 `NoSuchElementException`，用 `orElse` / `orElseThrow` / `ifPresent`；
5. **别在流里改外部变量**：lambda 捕获的局部变量必须 effectively final，想累加用 `reduce` 或 `mapToInt().sum()`；
6. **`parallelStream()` 不要随手加**：它底层就是多线程（Day 10 的坑全套适用），共享可变状态会出错，数据量小时反而更慢——顺序流能跑对，再考虑并行。

> 第 6 条我第一次就吃亏：以为"加个 parallel 就变快"，结果往 `ArrayList` 里 add，数据错乱还找不出原因。多线程那一课没有白学。

## 9. 今日学习总结

1. **Stream 是流水线，不是容器**：不存数据、不改数据源、惰性求值、只能消费一次；
2. **三步走**：数据源 `.stream()` → 中间操作（返回 Stream）→ 终端操作（返回结果）；
3. **中间操作负责"怎么处理"，终端操作负责"拿结果"**：少了终端操作，整条流一行都不跑；
4. **`collect` 是主力出口**：`toList` / `toSet` / `toMap` / `joining`，加上 `groupingBy` 基本够日常用；
5. **`groupingBy` 补上了 Java 一直缺的 groupBy**：还能再套 `counting()` / `mapping()` 做二级统计；
6. **`toMap` 两个坑**：key 重复要传 merge 函数，value 不能为 null；
7. **和 JS 对照着记最快**：`filter/map/flatMap/reduce/some/every/find` 全都能对上，`sorted` 不动原集合这一点要特别注意；
8. **流不是银弹**：需要下标、需要 break/continue、需要在循环里改多个变量时，for 循环更好读；
9. **`parallelStream` 是双刃剑**：底层是多线程，共享状态不安全，小数据更慢；
10. 下一篇：**IO 流**——今天这条流处理的是内存里的数据，下一篇让数据从文件、从网络里流进来。
