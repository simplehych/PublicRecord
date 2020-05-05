
## 关键字

同步，异步，事件流，上游，下游，线程调度，操作符，背压，Hot/Cold Observable（电台/CD）

## 1. 优缺点

### 1.1 优点：

1. 响应式编程，被观察者主动拿到数据传递给观察者，解耦
2. 链式调用，随着逻辑变复杂，代码结构依然简洁
3. 线程任意切换，不限次数
4. 强大的操作符功能

### 1.2 缺点：

1. 操作符群强大，内部基本实现日常需求的功能，我们可能不认识或者不会组合

### 1.3 区别对比：

同步没有争议；

异步情况：

1. Callback/Listener Hell
2. Future<Future<T>>
3. Observable 默认按异步处理，下游不关心上游事件的处理

## 2. 观察者模式

普通的生产者消费者模式，面向 Callback 编程

`生产者` --Callback--> `消费者`

取消订阅：在事件生产层完成

RxJava模式

`生产者` -> `RxJava Operators` -> `消费者`

1. RxJava 取代了 Callback
2. RxJava 对生产者的事件进行重新组织，进行“预处理”
3. 消费者这一层代码变轻，RxJava 取代 Callback 这一层代码变厚

取消订阅：下放到观察者这层，实现了彻底解耦


## 3. Rx2.0 新特性

Observable -> Observer 不支持背压 Backpressure

Flowable -> Subscriber 支持背压

四个新增核心 API：`Publisher`、`Subscriber`、`Subscription`、`Processor`

五种事件类型

1. Observable
2. Single
3. Maybe
4. Completable
5. Flowable 支持背压

发送事件：ObservableEmitter

管理时间：Disposable 可支配的 / CompositeDisposable

其他：

1. Subject
2. RxJavaPlugins

## 4. 线程调度

`.subscribeOn()`：指定上游，第一次有效

`.observeOn()`：指定下游，多次有效，使用一次下游切换一次

几种线程：

Schedules.io() / .computation() / .newThread() / .single() / .trampoline() / .from(Executor)

AndroidSchedule.main()

## 5. 背压

BackPressure 上游发送事件的快慢，上下游流速不均衡
同步：同一线程，下游接收完毕 然后上游才发送
异步：不同线程，任意发送

Flowable
区分：
1. 多了一个被压策略参数
2. Disposable -> Subscription
3. Observer -> Subscriber
4. Subscription.request() 响应式拉取

同步：不调用request认为下游没有处理事件的能力，抛MissBackpressureException

异步：可以不调用，默认有一个128存储buffer，超过128抛异常，

背压策略 BackpressureStrategy
1. ERROR：128
2. BUFFER：不限
3. DROP：存不下的丢弃
4. LASTEST：只保留最新的

上游获取下游处理事件能力：FlowableEmitter.requested()

## 6. 操作符

> 注意，实现使用都用对象操作，而不是简单的数字

顺序操作符 | 描述
--- | ---
.startWith() | 之前操作
.doOnNext() | 之后操作


类时间操作符 | 描述 | 举例
--- | --- | ---
.debounce() | 去抖动，去除发送频率过快的事件 | -
.distinc() | 去重 | -
.filter() | 过滤 | -
.sample() | 采样 | -
.last() |  最后 | -
.timer() |  定时任务，默认在新线程 | -
.interval() |  间隔任务，默认在新线程，注意销毁 | -
.skip() | 跳过 | (1, 2, 3, 4) -- .skip(2) --> (3, 4)
.take() | 接收几个事件 | (1, 2, 3, 4) -- .take(2) --> (1, 2)


组合变换操作符 | 描述 | 举例
--- | --- | ---
.compose() | 配合 ObservableTransformer  |  -
.map() | 一对一 | -
.flatMap() |  一对多，无序 | -
.concatMap() |  一对多，有序 | -
.zip() |  多对一，有序，合并以最少的事件队列为准，注意在一个线程（没有切换线程）时会出现水管1先发送，然后再发送水管2的，因为是单线程。| log: 1，2，3，A，1A，B，2B，C，3C
.concat() | 连接，A 调用了onComplete才会执行下一个 B | -
.merge() |  组合，根据事件发送时间 A B 交叉组合，不用等A发送完毕 | -
.buffer() :Observable<List<T>> | - |  (1, 2, 3, 4, 5, 6) --.buffer(3,2)--> (123,345,5)
.window() :Observable<Observable<T>>  | - | -
.reduce() | 简化 | (1, 2, 3) --.reduce((a, b) -> a + b) --> 6
.scan() |  | (1, 2, 3) --.scan((a, b) -> a + b) --> 1,3,6

## 7. 应用场景

### 7.1 空间维度

场景 1：页面先加载缓存后加载网络

.startWith

.distinct

场景 2：页面加载多个接口加载视图

.map

.flatMap

.collectInto

.combieLatest

### 7.2 时间维度

场景 1：点击事件防抖动

.debounce

场景 2：社交软件点赞和取消点赞

频繁点击增加服务器压力

优化点：
1. 防抖动
2. 连续点击 2 下无效情况

 场景 3：搜索提示

 考虑点：
 1. 防抖动
 2. 多次请求，返回结果的顺序问题，1. 展示最后一次请求 2. 取消之前的请求

.switchMap


## 参考资料

[RxJava 沉思录系列](https://www.jianshu.com/p/55c78b0f876e)

[拥抱RxJava 系列：关于Observable的冷热，常见的封装方式以及误区](https://www.jianshu.com/p/f5f327c8b612)

[给初学者的RxJava2.0教程系列](https://www.jianshu.com/p/464fa025229e)

[这可能是最好的RxJava 2.x 系列](https://www.jianshu.com/p/0cd258eecf60)
