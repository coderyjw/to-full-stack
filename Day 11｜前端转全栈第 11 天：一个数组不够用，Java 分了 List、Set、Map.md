# Day 11｜前端转全栈第 11 天：一个数组不够用，Java 分了 List、Set、Map

Day 04 学数组时就发现：Java 的数组一旦创建，长度就锁死了。当时还安慰自己"前端也很少往数组里塞东西"。写了几天就发现根本不够用——要按名字找数据、要去重、要排序、要边遍历边删，数组一个都干不了。今天正式进集合。

> 💬 前端是一个 `[]` 走天下的：列表用数组，去重用 `[...new Set(arr)]`，字典用 `{}` 或 `new Map()`，排序 `sort()` 一行搞定。到 Java 第一天就懵了——它把"有序列表""不可重复的集合""键值对"拆成了三套东西，还各自有七八个实现类。后来才想通：这不是啰嗦，是把数据结构摆到明面上让你选。

## 1. 数组到底差在哪

| 维度   | 数组                  | 集合                                    |
| ------ | --------------------- | --------------------------------------- |
| 长度   | 创建即定死，不能改    | 自动扩容                                |
| 功能   | 只有下标和 `length`   | 增删查改、去重、排序、键值对、队列……    |
| 类型   | 一种类型              | 一种类型（泛型，但只能是引用类型）      |
| 定位   | 内存里连续的一段      | 按数据结构分工，底层可能是数组或链表    |

一句话：**数组是"一段连续内存"，集合是"一堆现成的数据结构和算法"**。

## 2. 体系总览：先记住这张树

```
Iterable
└── Collection（单列集合：一个元素一个元素地存）
    ├── List     有序、可重复、有索引
    │   ├── ArrayList
    │   ├── LinkedList
    │   └── Vector（老古董，基本不用）
    ├── Set      不可重复
    │   ├── HashSet         无序
    │   ├── LinkedHashSet   保留插入顺序
    │   └── TreeSet         自动排序
    └── Queue    队列
        ├── LinkedList
        └── ArrayDeque

Map（双列集合：键值对，不继承 Collection）
├── HashMap
├── LinkedHashMap
└── TreeMap
```

前端对照表，先建立直觉：

| 我想要                | JS 里怎么写           | Java 里用什么                     |
| --------------------- | --------------------- | --------------------------------- |
| 有序列表、可重复      | `[]`                  | `List`（`ArrayList`）             |
| 去重                  | `new Set()`           | `Set`（`HashSet`）                |
| 键值对                | `{}` / `new Map()`    | `Map`（`HashMap`）                |
| 自动排序的集合        | `arr.sort()`          | `TreeSet` / `TreeMap`             |
| 队列（先进先出）      | `arr.shift()`         | `Queue`（`ArrayDeque`）           |

## 3. Collection 的通用方法

List 和 Set 都继承 `Collection`，所以这些方法都能用：

| 方法                     | 说明                                   |
| ------------------------ | -------------------------------------- |
| `boolean add(E e)`       | 添加元素，Set 里重复添加会返回 `false` |
| `boolean remove(Object o)` | 删除**元素**（不是下标）             |
| `boolean contains(Object o)` | 是否包含                           |
| `int size()`             | 元素个数                               |
| `boolean isEmpty()`      | 是否为空（别用 `size() == 0`）         |
| `void clear()`           | 清空                                   |
| `Object[] toArray()`     | 转成数组                               |

```java
import java.util.*;

public class Demo01 {
    public static void main(String[] args) {
        Collection<String> c = new ArrayList<>();
        c.add("Java");
        c.add("还");
        c.add("挺好玩");
        System.out.println(c.size());        // 3
        System.out.println(c.contains("还")); // true
        c.remove("还");
        System.out.println(c);               // [Java, 挺好玩]
    }
}
```

> 泛型只能写引用类型：`List<Integer>` 可以，`List<int>` 编译不过。基本类型要换成包装类，这一点和 JS 里数组什么都能塞完全相反。

## 4. List：最像 JS 数组的那一个

### 4.1 ArrayList 和 LinkedList 怎么选

| 维度             | ArrayList                | LinkedList                     |
| ---------------- | ------------------------ | ------------------------------ |
| 底层结构         | 数组                     | 双向链表                       |
| 按下标取值       | 快（O(1)）               | 慢（要从头找，O(n)）           |
| 头部/中间增删    | 慢（要挪元素）           | 快（改指针即可）               |
| 内存             | 紧凑，但扩容会留空位     | 每个节点多存两个指针           |
| 额外技能         | —                        | 实现了 `Deque`，能当队列/栈    |

`ArrayList` 默认容量是 10，满了就扩到原来的 1.5 倍（`old + (old >> 1)`）。所以**知道大概多少个元素时，直接 `new ArrayList<>(100)` 能少扩容几次**。

结论：**日常 90% 用 ArrayList**。LinkedList 只在"频繁在头部增删"或"要当队列用"时才有优势，而队列场景又有更好的 `ArrayDeque`。

### 4.2 常用方法

| 方法                          | 说明                        |
| ----------------------------- | --------------------------- |
| `add(E e)` / `add(int i, E e)` | 尾部添加 / 指定位置插入    |
| `E get(int i)` / `E set(int i, E e)` | 取值 / 改值          |
| `E remove(int i)`             | 删**下标**，返回被删元素    |
| `boolean remove(Object o)`    | 删**元素**                  |
| `int indexOf(Object o)`       | 第一次出现的下标，没有返回 -1 |

这里有个必须踩过的坑：`remove` 有重载，传 `int` 走的是"删下标"，传对象走的才是"删元素"。

```java
List<Integer> list = new ArrayList<>(List.of(10, 20, 30));
list.remove(1);                     // 删下标 1 → [10, 30]
list.remove(Integer.valueOf(10));   // 删元素 10 → [30]
System.out.println(list);
```

> 💬 我第一次想删掉元素 `1`，写了 `list.remove(1)`，结果删掉的是下标 1 那个值。在 JS 里 `indexOf + splice` 是两件事，Java 把它们塞进了一个方法名里，`List<Integer>` 时特别容易翻车。

### 4.3 三种遍历方式

```java
List<String> list = new ArrayList<>(List.of("a", "b", "c"));

// 1. 普通 for：需要下标时用
for (int i = 0; i < list.size(); i++) {
    System.out.println(list.get(i));
}

// 2. 增强 for：只要元素时用，最常用（相当于 JS 的 for...of）
for (String s : list) {
    System.out.println(s);
}

// 3. 迭代器：需要在遍历中删除元素时用
Iterator<String> it = list.iterator();
while (it.hasNext()) {
    String s = it.next();
    if (s.equals("b")) {
        it.remove();   // 安全删除
    }
}
System.out.println(list);   // [a, c]
```

**为什么不能在增强 for 里 `list.remove()`？**

增强 for 底层就是迭代器，但它把迭代器藏起来了。你调 `list.remove()` 改的是集合结构，迭代器并不知道，下一次 `next()` 时它发现"期望的修改次数对不上"，直接抛 `ConcurrentModificationException`。

要么用 `it.remove()`，要么用带 lambda 的 `removeIf`（内部走的也是迭代器）：

```java
list.removeIf(s -> s.startsWith("a"));   // 一行搞定
```

## 5. Set：专门用来去重

### 5.1 HashSet 靠什么判断重复

`HashSet` 底层就是一个 `HashMap`（元素当 key，value 是一个固定常量）。判断重复分两步：

1. 先算 `hashCode()`，找到对应的桶；
2. hash 相同，再用 `equals()` 比内容。

所以**自定义类想放进 HashSet 去重，`hashCode` 和 `equals` 必须同时重写**。

> 💬 Day 09 我重写了 `equals`，以为"相等"这件事搞定了。结果把对象丢进 HashSet，两个内容一模一样的 Person 全存进去了——因为没重写 `hashCode`，它俩落进了不同的桶，压根没机会走到 `equals`。`equals` 和 `hashCode` 是绑定的一对，拆开用等于没写。

IDEA 里 `Alt + Insert` → `equals() and hashCode()` 可以自动生成，生成出来大概是：

```java
@Override
public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Person person = (Person) o;
    return age == person.age && Objects.equals(name, person.name);
}

@Override
public int hashCode() {
    return Objects.hash(name, age);
}
```

### 5.2 三个 Set 的区别

| 实现            | 顺序                       | 底层            | 什么时候用                     |
| --------------- | -------------------------- | --------------- | ------------------------------ |
| `HashSet`       | 无序                       | HashMap         | 只要去重，不管顺序（最快）     |
| `LinkedHashSet` | 保留**插入顺序**           | 链表 + 哈希表   | 去重又要保持原来的顺序         |
| `TreeSet`       | **自动排序**               | TreeMap（红黑树） | 去重 + 一直有序，或者要取最大最小 |

`TreeSet` 要求元素能比较：要么类实现 `Comparable` 重写 `compareTo`，要么创建时传一个 `Comparator`——这里正好用上 Day 10 的 lambda：

```java
// 默认升序
Set<Integer> asc = new TreeSet<>(List.of(3, 1, 2, 3));
System.out.println(asc);   // [1, 2, 3]

// 降序：传入比较器
Set<Integer> desc = new TreeSet<>((a, b) -> Integer.compare(b, a));
desc.addAll(List.of(3, 1, 2));
System.out.println(desc);  // [3, 2, 1]
```

> 比较器别写成 `(a, b) -> b - a`。两个数一正一负、差值超出 int 范围时会溢出，排序结果直接错乱。规范写法是 `Integer.compare(b, a)`。

## 6. Map：键值对

### 6.1 常用方法

| 方法                                   | 说明                                     |
| -------------------------------------- | ---------------------------------------- |
| `V put(K key, V value)`                | 存，**key 已存在会覆盖**并返回旧值       |
| `V get(Object key)`                    | 取，没有返回 `null`                      |
| `V getOrDefault(K key, V def)`         | 取，没有就返回默认值（避免判空）         |
| `V remove(Object key)`                 | 按 key 删                                |
| `boolean containsKey(Object key)`      | 是否包含某个 key（**常用**）             |
| `boolean containsValue(Object value)`  | 是否包含某个值（要遍历，慢）             |
| `Set<K> keySet()`                      | 所有 key                                 |
| `Collection<V> values()`               | 所有 value                               |
| `Set<Map.Entry<K,V>> entrySet()`       | 所有键值对                               |
| `V putIfAbsent(K key, V value)`        | key 不存在才放（做计数、缓存很常用）     |

```java
Map<String, Integer> map = new HashMap<>();
map.put("张三", 18);
map.put("李四", 20);
map.put("张三", 19);                     // key 重复 → 覆盖，返回旧值 18
System.out.println(map.get("张三"));      // 19
System.out.println(map.getOrDefault("王五", 0)); // 0
```

### 6.2 遍历三种方式

```java
// 1. 只要 key
for (String key : map.keySet()) {
    System.out.println(key + " = " + map.get(key));   // 每次 get 都是一次查找
}

// 2. 只要 value
for (Integer v : map.values()) {
    System.out.println(v);
}

// 3. 键值一起拿（推荐，只遍历一次）
for (Map.Entry<String, Integer> e : map.entrySet()) {
    System.out.println(e.getKey() + " = " + e.getValue());
}

// 4. lambda 版（Day 10 学的，最简洁）
map.forEach((k, v) -> System.out.println(k + " = " + v));
```

### 6.3 坑：`Integer` 的 `==` 在集合里立刻反咬一口

Day 09 立过一条规矩：**只要出现 `==`，先问对象是从哪来的**。自动装箱让这件事变得极其阴险：

```java
Integer a = 127;
Integer b = 127;
System.out.println(a == b);      // true

Integer c = 128;
Integer d = 128;
System.out.println(c == d);      // false
System.out.println(c.equals(d)); // true
```

原因：`Integer.valueOf()` 会缓存 **-128 ~ 127** 的对象，在这个范围内装箱直接返回缓存里的同一个实例；超出范围就每次 `new` 一个。所以 `127 == 127` 是 true，`128 == 128` 是 false。

放到集合里更明显：

```java
Map<String, Integer> map = new HashMap<>();
map.put("count", 200);

Integer v1 = map.get("count");
Integer v2 = map.get("count");
System.out.println(v1 == v2);       // true：取出来的是同一个对象
System.out.println(v1 == 200);      // false：右边 200 是新装箱的，地址不同
```

结论：**包装类比较一律用 `equals`**，包括 `Map` 里取出来的 value。这条不写清楚，面试官一问一个准。

### 6.4 HashMap 底层（够用版）

- JDK 8 之后是**数组 + 链表 + 红黑树**：数组是桶，hash 冲突了就挂链表，链表太长转红黑树；
- 默认容量 16，负载因子 0.75，元素超过 `16 × 0.75 = 12` 就扩容到 32，以此类推（每次翻倍）；
- 链表长度 ≥ 8 **并且**数组长度 ≥ 64 才树化，否则优先扩容（扩容能打散冲突）；
- key 允许为 `null`，放在下标 0 的桶里。

这也解释了为什么自定义对象当 key 必须重写 `hashCode` + `equals`——`HashMap` 全靠这两个方法找桶、比 key。

## 7. 泛型：集合离不开的搭档

```java
List<String> names = new ArrayList<>();
names.add("张三");
// names.add(123);          // 编译期就报错，不用等到运行时
String first = names.get(0); // 不用强转
```

三句话讲完：

1. **作用**：把类型检查从"运行时"提前到"编译期"，顺便省掉强制类型转换；
2. **只能是引用类型**：所以要写 `List<Integer>`，不是 `List<int>`；
3. **类型擦除**：泛型只在编译期存在，运行时 `List<String>` 和 `List<Integer>` 都是 `List`，所以不能 `new T()`、不能 `instanceof List<String>`。

顺带一个写工具方法才会遇到的点——通配符：

| 写法               | 能做什么           | 记忆         |
| ------------------ | ------------------ | ------------ |
| `? extends Number` | 只能读，不能写     | 生产者（读） |
| `? super Integer`  | 能写，读只能当 Object | 消费者（写） |

（这套 PECS 规则日常业务代码很少直接写，遇到再回来看。）

## 8. Collections 工具类

`java.util.Collections` 是一堆静态方法，专门伺候集合：

```java
List<Integer> nums = new ArrayList<>(List.of(3, 1, 4, 1, 5));

Collections.sort(nums);                                  // 升序
Collections.sort(nums, (a, b) -> Integer.compare(b, a));  // 降序
Collections.reverse(nums);                                // 反转
Collections.shuffle(nums);                                // 打乱
System.out.println(Collections.max(nums));                // 最大值
```

还有两个"不可变"的入口，返回的集合**只能读不能改**，改就抛 `UnsupportedOperationException`：

```java
List<String> fixed = List.of("a", "b");                 // Java 9+，长度固定
List<String> view = Collections.unmodifiableList(fixed); // 只读视图
```

> 注意 `Arrays.asList(...)` 返回的是**固定长度**的列表：`set` 可以，`add`/`remove` 会抛异常。这个坑在写测试数据时特别常见。

## 9. 选型速查

| 需求                             | 用它                        |
| -------------------------------- | --------------------------- |
| 就是存一串东西，按下标取         | `ArrayList`                 |
| 频繁在头部插入/删除、当队列用     | `ArrayDeque`（而不是 LinkedList） |
| 去重，不关心顺序                 | `HashSet`                   |
| 去重，还要保持插入顺序           | `LinkedHashSet`             |
| 去重，还要一直有序               | `TreeSet`                   |
| 键值对，按 key 快速查            | `HashMap`                   |
| 键值对，还要按插入顺序遍历       | `LinkedHashMap`             |
| 键值对，key 要一直有序           | `TreeMap`                   |

一句话总结：**先想清楚"要不要顺序、要不要去重、是不是键值对"，答案基本就唯一了**。

## 10. 今日学习总结

1. **数组定长、功能少，集合是"现成数据结构"**：List 有序可重复、Set 不可重复、Map 键值对，Queue 走队列；
2. **`ArrayList` 是默认选择**：底层数组，随机访问快，默认容量 10、扩容 1.5 倍；LinkedList 只在频繁头插头删或当队列时才有优势；
3. **`remove` 有重载陷阱**：`List<Integer>` 里 `remove(1)` 删的是下标，删元素要写 `remove(Integer.valueOf(1))`；
4. **遍历中删元素不能用增强 for**：会抛 `ConcurrentModificationException`，用迭代器的 `remove()` 或 `removeIf`；
5. **去重靠 `hashCode` + `equals`**：自定义类必须两个一起重写，只重写 `equals` 在 HashSet 里无效；
6. **三种 Set 按"顺序需求"选**：无序 HashSet、插入序 LinkedHashSet、自动排序 TreeSet（配 Comparator，别写 `b - a`）；
7. **Map 遍历推荐 `entrySet` 或 `forEach`**：`keySet + get` 是两次查找；
8. **包装类比较永远用 `equals`**：`Integer` 只缓存 -128~127，127 相等、128 不等，这个坑在 Map 取值时最容易踩；
9. **泛型是编译期的类型检查**：集合里装不了基本类型，运行时会被擦除；
10. 下一篇：**Stream 流**——Collection 只是把数据装起来，真正让前端兴奋的是"像 `map`/`filter` 一样写循环"，下一篇把它一行写完。
