- [ ] Guava 缓存的使用

[移动开发：如果没做好这些准备及面试题，找工作还是先缓缓吧](https://www.jianshu.com/p/02ecfa0a3bbc)

## Android

领域划分：

1. 应用层开发：各种App开发及性能优化
2. 系统开发：ROM维护，包括系统级别app定制、framework层定制、HAL、底层驱动、内核等（嵌入式开发范畴）
3. 嵌入式开发：物联网、智能家居、车联网
4. 安全开发：逆向、反编译、混淆
5. 音视频开发：直播、点播、音视频通话、短视频
6. 图形图像开发：OpenGL ES、EGL、GLSurfaceView、滤镜、美颜
7. 跨平台开发：ReactNative、Flutter
8. SDK开发
9. 编译方向：日常Android编译效率、项目构建、Android ROM编译差量包大小
10. 插件化、组件化、热修复方向
11. VR / AR

计算机基础：

1. 操作系统
2. 网络协议
2. 数据结构和算法，hash碰撞
3. 设计模式 GoF Gang of Four 四人组，23种，1. 深入了解和使用，优缺点；2. 运用它们，结合Android源码，如建造者模式Dialog的创建，单例模式、适配器模式、观察者模式



Java技术清单：

1. 泛型
2. 反射 Hook  https://github.com/simplehych/HookApp
3. 注解
4. 线程池
5. JVM
6. 序列化
6. OOP -\> AOP
6. IoC Inversion of Control 控制反转，面向对象编程的设计原则，常见的方式叫做依赖注入DI Dependency Injection，还有一种方式依赖查找 Dependency Lookup

https://blog.csdn.net/whale\_kyle/article/details/91049282?depth\_1-utm\_source=distribute.pc\_relevant.none-task&utm\_source=distribute.pc\_relevant.none-task

Android 技术清单：

1. OS体系结构，平台机制，进程管理机制，任务管理机制
2. 熟悉Android底层框架原理 （了解Linux操作系统、JVM、Native层核心原理； 对系统级 CPU/GPU/内存/网络 等原理有深入了解）
2. 四大组件知识体系，生命周期、任务栈、启动模式、Fragment、Service
2. 系统服务，AMS、PMS、WMS、Android虚拟机
3. IPC，进程间通信（Socket、Binder、AIDL、Messenager、ContentProvider），应用AMS、PMS等
3. 消息机制，Handler、Looper、MessageQueue
4. 多线程，并发、锁、线程（AsyncTask、HandlerThread、IntentService）、线程池（ThreadPoolExecutor）
5. UI，事件传递分发、View绘制流程、自定义View、动画等
6. 适配，UI适配、Android版本差异化适配（权限）、机型适配
7. 网络编程，http/https、socket（对TCP/IP协议的封装，本身不是协议，而是一个调用接口API）https://www.jianshu.com/p/6676ddf97eb8
8. 数据库
9. 缓存，LRU，图片缓存 / 网络缓存 / 本地缓存 
9. json/xml
10. 第三库的使用，okhttp、rxjava、glide、自建库上传jcenter
11. Gradle构建及编译
12. 跨平台，RN、Flutter
14. 框架模式，MVC、MVP、MVVM https://blog.csdn.net/singwhatiwanna/article/details/80904132
15. 组件化、插件化、热修复
16. 测试，单元测试（Junit），UI自动化测试工具（Monkey随机、Instrumentation早期、UiAutomator、Robotium、Espresso）、工具（Appium、postman、charles） https://www.jianshu.com/p/2abcf1e312ec
17. 调试，log开发习惯、debug断点、adb shell dumpsys、手机开发者模式（严苛模式StrictMode）
18. 调优，应用优化（内存、ANR、启动时间、流畅度、包体积）、底层优化（framework、HAL、Kernel、filesystem、network）、监控系统、工具（profile、SysTrace、TraceView、MAT、LeakMemory，https://blog.csdn.net/qq\_16206535/article/details/80307612）
8. JNI / NDK；（蓝牙开发、摄像头）
7. 音视频处理，流媒体分发调度以及传输协议
9. 安全，混淆/反混淆、加固/脱壳、反编译、加解密、dex/elf/smali、Xposed/Frida/Cydia、后门注入Metasploit

工具：

1. 版本控制工具
2. 构建工具
3. 正则表达式

其他：

1. [REST接口][1]



机器码指令：
Operation code 操作码 / operand 操作数 都是二进制
LD A, 01H 其中 LD代表加载的指令，A代表内部的存储器A，01 操作数， H代表十六进制
 

## 参考资料

[一份来自亚马逊工程师的Google面试指南][2]
[技术面试必备知识][3]
https://github.com/CyC2018/CS-Notes
https://github.com/HIT-Alibaba/interview
https://github.com/Moosphan/Android-Daily-Interview
https://github.com/AweiLoveAndroid/CommonDevKnowledge
https://github.com/wolverinn/Waking-Up
https://github.com/JsonChao/Awesome-Android-Interview
https://github.com/stormzhang/android-interview-questions-cn
https://github.com/sucese/android-interview-guide
https://hit-alibaba.github.io/interview/index.html

[Github CS-Notes](https://github.com/CyC2018/CS-Notes/blob/master/notes/Java%20%E5%B9%B6%E5%8F%91.md)
[Android面试相关文章以及github整理](https://mp.weixin.qq.com/s/CQABJNacnsf8_s6l93JKUw)
[Android 知识体系脑图「android篇」](https://juejin.im/entry/5adf719e6fb9a07ab773db0f)
[Android 知识体系脑图「java篇」](https://juejin.im/entry/5ab9343b6fb9a028df229ff4)

[国内一线互联网公司内部面试题库](https://jackyandroid.github.io/AndroidInterview-Q-A/interview/)



---
---

## 数据库

### 1. 概念

#### 1.1 事务
满足ACID特性的一组操作，要不Commit成功，要么Rollback失败回滚

#### 1.2 ACID
原子性 atomicity：不可分割的最小单元
一致性 consistency：所有事务对同一数据的读取结果都是相同的
隔离性 isolation：一个事务所做的修改在最终提交前，对其他事务是不可见的
持久性 durability：一旦事务提交，则其所做的修改将永久保存到数据库中。即使发生崩溃，事务执行的结果也不能丢失。崩溃可以重做日志 Redo Log 进行恢复，从而实现持久性。与回滚日志记录数据的逻辑修改不同，重做日志记录的是数据页的**物理修改**。

#### 1.3 理解ACID
一致性，为了事务的执行结果才能正确。
无并发时，多个事务执行，隔离性一定满足，此时满足 **原子性**，一定能满足一致性。
有并发时，多个事务执行，必须同时满足 **隔离性** 和 **原子性**，才能满足一致性。
持久化，为了应对系统崩溃的情况。

### 2. 并发一致性问题

在并发环境下，事务的 **隔离性** 很难保证，不满足 **原子性**，因此会出现很多并发一致性问题。

#### 2.1 丢失修改

T1 和 T2 两个事务都对一个数据进行修改，**T1 先修改，T2 随后修改**，T2 的修改覆盖了 T1 的修改。

#### 2.2 读脏数据

T1 修改一个数据，T2 随后读取这个数据。如果 **T1 撤销了这次修改**，那么 T2 读取的数据是脏数据。

#### 2.3 不可重复读

T2 读取一个数据，T1 对该数据做了修改。如果 T2 再次读取这个数据，此时读取的结果**和第一次读取的结果不同**。

#### 2.4 幻影读

**T1 读取某个范围的数据**，T2 在这个范围内插入新的数据，T1 再次读取这个范围的数据，此时读取的结果和和第一次读取的结果不同。

### 3. 封锁

#### 3.1 封锁粒度
MySQL两种：行级锁和表级锁

加锁需要消耗资源，锁的各种操作（获取锁、释放锁、检查锁状态）都会增加系统开销。

需要在并发程度和锁开销之间做一个权衡。

#### 3.2 封锁类型

1. 读写锁

	互斥锁，Exclusive，X锁，写锁
	共享锁，Shared，S锁，读锁
	* 加了 X 锁，可以读取和更新，加锁期间其他事务**不能对 A 加任何锁**
	* 加了 S 锁，可以读取，不可更新，加锁期间**能对 A 加 S 锁，不能加 X 锁**。
2. 意向锁

	Intention Locks，属于表级锁，在原来的 X/S 锁之上引入了 IX/IS。
	* 一个事务在获取某个数据 **行对象的 S 锁**之前，必须获取 **表的IS 锁** 或 **更强的锁**
	* 一个事务在获取某个数据 **行对象的X锁** 之前，必须先获得 **表的IX锁**
3. 兼容关系
	  | X锁 | S锁
	--- | --- | ---
	X锁 | x | x
	S锁 | x | 



	 | X锁 | IX锁 | S锁 | IS锁
	--- | --- | --- | --- | ---
	X锁 | x | x | x | x
	IX锁 | x |  | x | 
	S锁 | x | x |
	IS锁 | x |

#### 3.3 封锁协议

##### 3.3.1 三级封锁协议

* 一级

	事务 T 修改数据 A 必须加 X 锁，直到 **T 结束**才释放 X 锁

	解决：丢失修改问题

* 二级

	在一级基础上，要求读取 A 时必须加 S 锁，**读取完**马上释放 S 锁
	解决：读取脏数据问题

* 三级

	在二级的基础上，要求读取数据 A 时必须加 S 锁，直到**事务结束**了才能释放 S 锁。
	解决：不可重复读问题。

##### 3.3.2 两段锁协议

**加锁** 和 **解锁** 氛围两个阶段进行。

**可串行化调度**：通过并发控制，使得并发执行的事务结果与某个串行执行的事务结果相同。串行执行的事务互不干扰，不会出现并发一致性问题。

事务遵循两段锁协议是保证可串行化调度的**充分条件**。

例如以下操作满足两段锁协议，它是可串行化调度。

```
lock-x(A)...lock-s(B)...lock-s(C)...unlock(A)...unlock(C)...unlock(B)
```

但**不是必要条件**。

例如以下操作不满足两段锁协议，但它还是可串行化调度。

```
lock-x(A)...unlock(A)...lock-s(B)...unlock(B)...lock-s(C)...unlock(C)
```

#### 3.4 MySQL隐式与显示绑定

隐式锁定： 的Inno存储引擎采用两段锁协议，会根据隔离级别在需要的时候自动加锁，并且所有的锁在同一时刻被释放

显示锁定：特定语句

```
SELECT ... LOCK In SHARE MODE;
SELECT ... FOR UPDATE;
```

### 4 隔离级别

#### 4.1 未提交读 *READ UNCOMMITTED*

一个事务中的修改，即使没有提交，对其他事务也是可见的。

#### 4.2 提交读 *READ COMMITTED*

一个事务只能读取已经提交的事务所做的修改。换句话，提交之前的修改对其他事务是不可见的。

#### 4.3 可重复读 *REPEATABLE READ*

保证在同一个事务中 **多次读取** 同一数据的 **结果是一样的**。

#### 4.4 可串行化 *SERIALIZABLE*

强制事务串行执行，这样多个事务互不干扰，不会出现并发一致性问题。

该隔离级别需要**加锁实现**，因为要使用加锁机制保证同一时间只有一个事务执行，也就是保证事务串行执行。

### 5 多版本并发控制

多版本并发控制（Multi-Version Concurrency Control, **MVCC**）是 MySQL 的 InnoDB 存储引擎实现 **隔离级别** 的一种具体方式。

用于实现 **提交读** 和 **可重复读** 这两种隔离级别。

而 **未提交读** 隔离级别总是读取最新的数据行，要求很低，无需使用 MVCC。

而 **可串行化** 隔离级别需要对所有读取的行都加锁，单纯使用 MVCC 无法实现。

#### 5.1 基本思想

加锁能解决多个事务同时执行时出现的并发一致性问题。

在实际场景中读操作往往多于写操作，因此又引入了读写锁来避免不必要的加锁操作，例如读和读没有互斥关系。读写锁中读和写操作仍然是互斥的，而 MVCC 利用了多版本的思想，写操作更新**最新的版本快照**，而读操作去读**旧版本快照**，没有互斥关系，这一点和 **CopyOnWrite** 类似。

在 MVCC 中事务的修改操作（DELETE、INSERT、UPDATE）会为数据行新增一个版本快照。

**脏读 和 不可重复读 \_最根本的原因\_是事务读取到其它事务未提交的修改。**在事务进行读取操作时，为了解决脏读和不可重复读问题，MVCC 规定只能读取已经提交的快照。当然一个事务可以读取自身未提交的快照，这不算是脏读。

#### 5.2 版本号

* **系统版本号 SYS\_ID**：是一个递增的数字，每开始一个新的事务，系统版本号就会自动递增。
* **事务版本号 TRX\_ID** ：事务开始时的系统版本号。

#### 5.3 Undo 日志

MVCC 的多版本指的是多个版本的快照，快照存储在 Undo 日志中，该日志通过回滚指针 ROLL\_PTR 把一个数据行的所有快照连接起来。

例如在 MySQL 创建一个表 t，包含主键 id 和一个字段 x。我们先插入一个数据行，然后对该数据行执行两次更新操作。

```
INSERT INTO t(id, x) VALUES(1, "a");
UPDATE t SET x="b" WHERE id=1;
UPDATE t SET x="c" WHERE id=1;
```

因为没有使用 START TRANSACTION 将上面的操作当成一个事务来执行，根据 MySQL 的 AUTOCOMMIT 机制，每个操作都会被当成一个事务来执行，所以上面的操作总共涉及到三个事务。快照中除了记录事务版本号 TRX\_ID 和操作之外，还记录了一个 bit 的 DEL 字段，用于标记是否被删除。

INSERT、UPDATE、DELETE 操作会创建一个日志，并将事务版本号 TRX\_ID 写入。DELETE 可以看成是一个特殊的 UPDATE，还会额外将 DEL 字段设置为 1。

#### 5.4 ReadView

MVCC 维护了一个 **ReadView 结构**：
1. 当前系统未提交的事务列表 **TRX\_IDs** {TRX\_ID\_1, TRX\_ID\_2, ...}
2. 该列表的最小值 **TRX\_ID\_MIN** 
3. 该列表的最大值 **TRX\_ID\_MAX**

**在进行 SELECT 操作时**，根据数据行快照的 TRX\_ID 与 TRX\_ID\_MIN 和 TRX\_ID\_MAX 之间的关系，从而判断数据行快照是否可以使用：

* **TRX\_ID \< TRX\_ID\_MIN** 时，表示该数据行快照时在当前所有未提交事务之前进行更改的，因此**可以使用**。

* **TRX\_ID \> TRX\_ID\_MAX** 时，表示该数据行快照是在事务启动之后被更改的，因此**不可使用**。

* **TRX\_ID\_MIN \<= TRX\_ID \<= TRX\_ID\_MAX** 时，需要根据隔离级别再进行判断：
	* 提交读：如果 TRX\_ID 在 TRX\_IDs 列表中，表示该数据行快照对应的事务还未提交，则该快照不可使用。否则表示已经提交，可以使用。
	* 可重复读：都不可以使用。因为如果可以使用的话，那么其它事务也可以读到这个数据行快照并进行修改，那么当前事务再去读这个数据行得到的值就会发生改变，也就是出现了不可重复读问题。

在数据行快照不可使用的情况下，需要沿着 Undo Log 的回滚指针 ROLL\_PTR 找到下一个快照，再进行上面的判断。


#### 5.5 快照读 与 当前读

1. 快照读

MVCC 的 SELECT 操作是快照中的数据，不需要进行加锁操作。

```
SELECT * FROM table ...;
```

2. 当前读

MVCC 其他会对数据库进行修改的操作（INSERT、UPDATE、DELETE）需要进行加锁操作，从而读取最新的数据。

**可以看到 MVCC 并不是完全不用加锁，而只是避免了 SELECT 的加锁操作。**

在进行 SELECT 操作时，可以强制指定进行加锁操作。以下第一个语句需要加 S 锁，第二个需要加 X 锁。

```
SELECT * FROM table WHERE ? lock in share mode;
SELECT * FROM table WHERE ? for update;
```


### 6 Next-Key Locks

Next-Key Locks 是 MySQL 的 InnoDB 存储引擎的一种 **锁实现**。

**MVCC 不能解决幻影读问题，Next-Key Locks 就是为了解决这个问题而存在的。** 在可重复读（REPEATABLE READ）隔离级别下，使用 MVCC + Next-Key Locks 可以解决幻影读问题。

#### 6.1 Record Locks

锁定一个记录上的索引，而不是记录本身。

如果表没有设置索引，InnoDB会自动在主键上创建隐藏的聚族索引，因此 Record Locks 依然可以使用。

#### 6.2 Gap Locks

锁定索引之间的间隙，但是不包含索引本身。

例如当一个事务执行以下语句，其他事务就不能在 t.c 中插入 15。

```
SELECT c FROM t WHERE c BETWEEN 10 and 20 FOR UPDATE;
```

#### 6.3 Next-Key Locks

它是 Record Locks 和 Gap Locks 的结合，不仅锁定一个记录上的索引，也锁定索引之间的间隙。

是一个前开后闭的区间，例如一个索引包含以下值：10、11、13、20，那么久需要锁定以下区间：

```
(-∞, 10]
(10, 11]
(11, 13]
(13, 20]
(20, +∞)
```

### 7 关系数据库设计理论

#### 7.1 函数依赖

记 A-\>B 表示 A 函数决定 B，也可以说 B 函数依赖于 A。

如果 {A1，A2，... ，An} 是关系的一个或多个属性的集合，该集合函数决定了关系的其它所有属性并且是最小的，那么该集合就称为键码。

对于 A-\>B，如果能找到 A 的真子集 A'，使得 A'-\> B，那么 A-\>B 就是部分函数依赖，否则就是完全函数依赖。

对于 A-\>B，B-\>C，则 A-\>C 是一个传递函数依赖。


#### 7.2 异常

不符合范式的关系，会产生很多异常，主要有以下四种异常：

* **冗余数据**：例如 学生-2 出现了两次。
* **修改异常**：修改了一个记录中的信息，但是另一个记录中相同的信息却没有被修改。
* **删除异常**：删除一个信息，那么也会丢失其它信息。例如删除了 课程-1 需要删除第一行和第三行，那么 学生-1 的信息就会丢失。
* **插入异常**：例如想要插入一个学生的信息，如果这个学生还没选课，那么就无法插入。


#### 7.3 范式

范式理论是为了解决以上提到四种异常。

高级别范式的依赖于低级别的范式，1NF 是最低级别的范式。

1. 第一范式 (1NF)

	属性不可分。

2. 第二范式 (2NF)

	每个非主属性完全函数依赖于键码。

3. 第三范式 (3NF)

	非主属性不传递函数依赖于键码。

### 8 ER 图

Entity-Relationship

有三个组成部分：**实体、属性、联系**。

用来进行关系型数据库系统的概念设计。

#### 8.1 实体的三种联系

一对一
一对多
多对多

#### 8.2 关系

1. 表示出现多次的关系
2. 联系的多向性
3. 表示子类


### 9. 基本操作 CRUD

  
---
---

## 缓存

[缓存][4] [缓存那些事][5] [一致性哈希算法][6]

互联网应用的整体流程：

```
用户界面（浏览器/App） <——> 网络转发（ISP <->反向代理服务器） <——> 应用服务 <——> 存储（数据库/文件系统）
```

### 1 背景

随着用户数和访问量越来越大，我们应用需要支撑更多的并发量，同时应用服务器和数据库服务器所做的计算越来越多。

但是应用服务器是有限的，数据库每秒接受的请求次数也是有限的，如何有效的利用资源来提供尽可能大的吞吐量？

一个办法就是引入缓存，每个环节可以重缓存中直接获取目标数据返回。
1. 对于服务器，减少计算量，有限的资源服务更多的用户；
2.对于用户来说，可以提升响应速度；

### 2 缓存特征

缓存也是一个数据模型对象，那么必然有它的一些特征：

#### 2.1 命中率
 
**命中率 = 返回正确结果数 / 请求缓存次数**

是衡量缓存有效性的重要指标，命中率越高说明，表明缓存的使用率越高。

#### 2.2 最大元素（最大空间）

缓存通常位于内存中，内存的空间通常比磁盘空间小的多，因此缓存的最大空间不可能非常大。

缓存中可以存放的最大元素的数量，一旦缓存中元素数量超过这个值（或者缓存数据所占空间超过其最大支持空间），那么将会触发缓存启动**清空策略**根据不同的场景合理的设置最大元素值往往可以一定程度上提高缓存的命中率，从而更有效的时候缓存。

#### 2.3 清空策略

* FIFO(first in first out)

	先进入的优先清理
* LFU(less frequently used)

	最少使用，根据 **被使用次数** 判断

* LRU(least recently used)

	最近最少使用，根据 **最后一次使用的时间** 判断

* 其他简单策略
	* 根据过期时间，清理过期时间最长的元素
	* 根据过期时间，清理最近要过期的元素
	* 根据关键字长短清理
	* 随机清理
### 3 缓存分类

虽然从硬件介质上来看，无非是内存和硬盘两种，但从技术上，可以分为内存、硬盘文件、数据库。

* **内存**：将缓存存储于内存中是最快的选择，无需额外的I/O开销，但是内存的缺点是没有持久化落地物理磁盘，一旦应用异常break down而重新启动，数据很难或者无法复原。
* **硬盘**：一般来说，很多缓存框架会结合使用内存和硬盘，在内存分配空间满了或是在异常的情况下，可以被动或主动的将内存空间数据持久化到硬盘中，达到释放空间或备份数据的目的。
* **数据库**：前面有提到，增加缓存的策略的目的之一就是为了减少数据库的I/O压力。现在使用数据库做缓存介质是不是又回到了老问题上了？其实，数据库也有很多种类型，像那些不支持SQL，只是简单的key-value存储结构的特殊数据库（如BerkeleyDB和Redis），响应速度和吞吐量都远远高于我们常用的关系型数据库等。

在目前的应用服务框架中，比较常见的，是根据缓存于应用的藕合度，分为：

* **local cache（本地缓存）** 
	**是在应用中的缓存组件。**
	**优点**：是应用和cache是在同一个进程内部，请求缓存非常快速，没有过多的网络开销等，在单应用不需要集群支持或者集群情况下各节点无需互相通知的场景下使用本地缓存较合适；
	**缺点**：因为缓存跟应用程序耦合，多个应用程序无法直接的共享缓存，各应用或集群的各节点都需要维护自己的单独缓存，对内存是一种浪费。

*  **remote cache（分布式缓存）**

   **是与应用分离的缓存组件或服务**
   **优点**：自身就是一个独立的应用，与本地应用隔离，多个应用可直接的共享缓存。

#### 3.1 实现

* 本地缓存
	* 编程直接实现
		* 成员变量或局部变量
		* 静态变量
	* Ehcache（服务端）
	* Guava Cache  
* 分布式缓存
	* memcached（服务端）
	* Redis（服务端）
	* Spring注解缓存

### 4 缓存位置

根据互联网产品的整体过程，每个层级都可以缓存。

1. **浏览器 / App；** 用户本地缓存
2. **网络服务提供商；** [ISP CDN][7] 网络访问第一跳，缓存在此
3. **反向代理；** 位于服务器之前，缓存在此
4. **本地缓存；** 服务器本地缓存，如使用 Guava Cache
5. **分布式缓存；**服务器分布式缓存，如使用 Redis
6. **数据库缓存；**
7. **Java内部缓存；** 字符串常量池和基本数据类型包装类缓冲池等等
8. **CPU多级缓存；** CPU 为了解决运算速度与主存 IO 速度不匹配的问题，引入了多级缓存结构，同时使用 MESI 等缓存一致性协议来解决多核 CPU 缓存数据一致性的问题。

### 5 CDN

内容分发网络（Content distribution network，CDN）是一种互连的网络系统，它利用 **更靠近用户的服务器** 从而更快更可靠地将 HTML、CSS、JavaScript、音乐、图片、视频等静态资源分发给用户。

它位于网络的边缘，距用户仅有"一跳"（Single Hop）之遥。

### 6 缓存问题

#### 6.1 缓存穿透

#### 6.2 缓存雪崩

#### 6.3 缓存一致性

#### 6.4 缓存“无底洞”现象


### 7 数据分布

#### 7.1 哈希分布

一致性哈希

#### 7.2 顺序分布

### 8 一致性哈希

Distributed Hash Table（DHT） 是一种哈希分布方式，其目的是为了克服传统哈希分布在服务器节点数量变化时大量数据迁移的问题

#### 8.1 基本原理

将哈希空间 [0, 2n-1] 看成一个哈希环，每个服务器节点都配置到哈希环上。每个数据对象通过哈希取模得到哈希值之后，存放到哈希环中 **顺时针方向第一个大于等于** 该哈希值的节点上。

#### 8.2 虚拟节点

上面描述的一致性哈希存在数据分布不均匀的问题，节点存储的数据量有可能会存在很大的不同。

解决方式是通过增加虚拟节点，然后将虚拟节点映射到真实节点上。虚拟节点的数量比真实节点来得多，那么虚拟节点在哈希环上分布的均匀性就会比原来的真实节点好，从而使得数据分布也更加均匀。

### 9 LRU

基于 `双向链表 + HashMap` 的 LRU 算法实现，对算法的解释如下：

* 访问某个节点时，将其从原来的位置删除，并重新插入到链表头部。这样就能保证链表尾部存储的就是最近最久未使用的节点，当节点数量大于缓存最大空间时就淘汰链表尾部的节点。
* 为了使删除操作时间复杂度为 O(1)，就不能采用遍历的方式找到某个节点。HashMap 存储着 Key 到节点的映射，通过 Key 就能以 O(1) 的时间得到节点，然后再以 O(1) 的时间将其从双向队列中删除。

```
public class LRU<K, V> implements Iterable<K> {

    private Node head;
    private Node tail;
    private HashMap<K, Node> map;
    private int maxSize;

    private class Node {

        Node pre;
        Node next;
        K k;
        V v;

        public Node(K k, V v) {
            this.k = k;
            this.v = v;
        }
    }
}
```

---
---


[1]:	https://www.runoob.com/w3cnote/restful-architecture.html
[2]:	https://mp.weixin.qq.com/s/0omyRAAUk4G5NLHIzrgmyA
[3]:	https://github.com/CyC2018/CS-Notes
[4]:	https://github.com/CyC2018/CS-Notes/blob/master/notes/%E7%BC%93%E5%AD%98.md
[5]:	https://tech.meituan.com/2017/03/17/cache-about.html
[6]:	https://my.oschina.net/jayhu/blog/732849
[7]:	http://dy.163.com/v2/article/detail/D9AOMRPR0511AULS.html

---
---

## Java内存模型

### 1 基础

#### 1.1 并发编程模型的分类

#### 1.2 Java内存模型的抽象

#### 1.3 重排序

#### 1.4 volatile

1. 可见性；对一个volatile变量的读，总能看到任意线程对这个volatile变量最后的写入。
2. 原子性；单个volatile变量具有原子性，多个或复合操作整体上不具有原子性。


#### 1.5 锁

java并发编程的重要同步机制。

1. 临界区互斥执行
2. 让释放锁的线程 向获取同一个锁的线程发送消息



