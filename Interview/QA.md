## Context
创建时机 Application 和 Activity
上述二者的区别，与视图有关的用 Activity

## Activity

1. 生命周期
2. 启动模式
3. IntentFilter 匹配规则

### 1. 生命周期

#### 1.1 典型的生命周期
**Act.onCreate** -> Fg.onAttach -> Fg.onCreate -> Fg.onCreateView ->
 -> Fg.onActivityCreated ->
**Act.onRestart()** ->
**Act.onStart** -> Fg.onStart -> **非前台**，用户看不到，本身可见
**Act.onResume** -> Fg.onResume -> **前台**，可见
Fg.onPause -> **Act.onPause** -> **不能太耗时**，影响新 Activity 的显示
Fg.onStop -> **Act.onStop** -> 
Fg.onDestroyView -> Fg.onDestroy -> Fg.onDetach -> **Act.onDestroy**

注意 Activity 和 Fragment 的顺序

#### 1.2 Activity 和 Fragment 生命周期区别
* Fragment 声明周期是由托管 **Activity.FragmentController** 调用的，Activity 是由系统 **Instrumentation-AMS(ActivityStack)-ActivityThread** 调用的
* Activity 声明周期的方法是 **protected**，而Fragment 是 **public**

#### 1.3 Act_A 打开 Act_B
* **A.onPause** -> B.onCreate -> B.onStart -> B.onResume -> **A.onStop**；
* 如果 Act_B 是**透明主题**则 Act_A 不会回调 onStop

#### 1.4 异常销毁重建场景

1. 系统配置更改，如旋转屏幕
2. 资源内存不足导致的回收

##### 1.4.1 系统配置改变,**异常销毁**重建流程

* **onSaveInstanceState** (与 onPause 不分先后) -> onStop -> onDestroy ->    onCreate -> **onRestoreInstanceState** 

> onRestoreInstanceState 和 onCreate 区别： r.state 有保存值才会调用 onRestoreInstanceState，不用判空；而 onCreate 有没有保存都会调用
> 参见： `Activitythread.mInstrumentation.callActivityOnRestoreInstanceState(activity, r.state);`

* 系统默认为我们保存当前 Activity 视图结构

* View 同样有 onSaveInstanceState/onRestoreInstanceState

* Activity 委托 Window 保存，Window 委托顶层容器 DecorView 保存，然后再一一通知子元素保存数据。这种思想同 View 的绘制机制和事件分发等

**禁止系统配置销毁重建方式：** 
配置 `android:configChanges="orientation|keyboardHidden"`等等则不销毁不重走生命周期
常用的configChanges 有：
1. locale 系统语言
2. keyboard 键盘类型（外接）；keyboardHidden 调用了键盘
3. screenLayout 屏幕布局
4. orientation 屏幕方向（旋转）（监听：onConfigurationChanged）
5. screenSize 屏幕尺寸（分屏）；smallestScreenSize 物理屏幕尺寸（外接显示设备）（这两个行为和编译选项有关，和运行环境无关, > API 13 需要添加）

##### 1.4.2 资源不足导致低优先级的 Activity 被杀死

存储和恢复过程同上一种情况
* 没有四大组件的进程容易被系统杀死
* 优先级从高到低：
    1. **前台 Activity**，正在交互中
    2. **可见非前台 Activity**，如出现对话框，导致 Activity 可见但不能交互
    3. **后台 Activity**，暂停的 Activity，执行了 onStop

    > 拓展：Service 的优先级从高到低，系统 - 前台 - 可见 - 服务 A - 服务 B - 后台 - 空进程

### 2. 启动模式

区分：当前任务栈，目标任务栈

#### 2.1 Activity 的 LaunchMode

启动四种模式：
1. standard，标准模式；跟随启动它的任务栈，非 Activity 的 Context 没有栈
2. singleTop，栈顶复用模式
3. singleTask，栈内复用模式，单实例模式；先判断是否存在该栈，不存在则创建，存在则判断是否有实例，不存在则创建压栈，存在则调到栈顶clearTop调用 onNewIntent
4. singleInstance，堆内单实例模式，加强版 singleTask，单独的位于一个新的任务栈，同一个 affinity 不同栈

指定方式：
1. 清单文件指定 AndroidManifest.xml `android:launchMode="singleTask"`
2. 代码启动 Activity 设置标志位 `intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)`
区别：
1. 方式 2 优先级高于方式 1
2. 限制范围不同；方式 2 不能指定 singleInstance 模式，而方式 1 不能设定 FLAG_ACTIVITY_CLEAR_TOP 标识

复用模式：onNewIntent 会回调，onCreate、onStart 不会调用

任务栈：taskAffinity 任务相关性，任务栈的名字，默认是包名，可单独指定，分为前台任务栈和后台任务栈

taskAffinity 和 allowTaskReparenting 结合：重新归还到父任务栈
条件：App_1（Stack_1） 和 App_2（Stack_2），Activity_B 属于 App_2，App_2 未启动
过程：App_1 启动 Activity_B，此时因为 App_2 没有启动 Activity_B 属于 Stack_1，按 Home 键返回桌面，点击 App_2 的桌面图标启动，这是显示 Activity_B 而不是 App_2 的 主Activity。这就是归还

taskAffinity 和 singleTask  配对使用

```
adb shell dumpsys activity
adb shell dumpsys activity activities 
```

区别：
多任务栈：Activity 任务栈
Android 多任务机制：多进程管理优先级

#### 2.2 Activity 的 Flags

* 设定 Activity 启动模式：
FLAG_ACTIVITY_NEW_TASK：同 singleTask
FLAG_ACTIVITY_SINGLE_TASK：同 singleTop

* 影响 Activity 的运行状态：
FLAG_ACTIVITY_CLEAR_TOP：
配合 singleTask，若实例已经存在则不创建调用 onNewIntent；
配合 standard，连同它及之上的 Activity 都要出栈，创建新的放入栈顶
FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS：不会出现在历史 Activity 列表中，同 excluedeFromRecents="true"

其他标志位

### 3. IntentFilter匹配规则

Activity 启动方式：
1. 显示调用
2. 隐式调用，匹配目标组件多个 IntentFilter 中的其中一个信息（action/category/data），
二者共存以显示调用为主

重点是 action 和 data
* **action**：自定义或系统预定义
   
    匹配规则：
    1. 必须存在
    2. 和过滤规则中的一个 action 相同，区分大小写

* **category**：自定义或系统预定义
    
    匹配规则：
    1. 可以不存在，有默认指定 "android.intent.category.DEFAULT"
    2. 必须是组件已经声明的，包含关系

* **data**

    匹配规则：和 action 类似，匹配一组 data 即可
    由两部分组成：
    1. **URI**：`scheme://host:port/path|pathPrefix|pathPatter`
    2. **mineType**：媒体类型 eg: image/jpeg、text/plain、video/*

    注意点：
    1. URI 有默认值 scheme 为 content 和 file，因此必须指定才能匹配
    2. intent.setData / intent.setType / intent.setDataAndType 三个方法会彼此清除
    3. 使用 PackageManager#resolveActivity 或 Intent#resolveActivity 判断是否存在 intent

### 4. Activity 的启动流程

应用： [插件开发 Hook startActivity](https://blog.csdn.net/u011068702/article/details/53208825)

1. Instrumentation#execStartActivity
2. AMS#startActivityAsUser -> ActivityStarer#startActivityUnchecked -> ActivityStackSupervisor#realStartActivityLocked -> app.thread.scheduleLaunchAcivity
3. AMS - [ApplicationThread(binder) - H - ActivityThread]
4. Activity#performLaunchActivity（创建 Context、创建 Activity、关联 context 和window、设置主题、mInstrumentation#callActivityOnCreate）
5. Activity#handleResumeActivity(wm.addView(decor)、创建ViewRootImpl.setView(decor)、View 绘制流程开始)


## Service

两种方式
1. startService
2. bindService
可组合使用

声明周期：
startService：onCreate -> onStartCommand -> onDestroy，多次 startService 时 onStartCommand 会重复执行，onCreate 不重复执行，一直存在，直到调用了 stopService
bindService：onCreate -> onBind -> onUnbind -> onDestroy，多次 bindService 不会重复执行，生命周期跟随调用者，

Service 与调用者交互且长期执行任务，startService+bindService 同时使用（解绑同时解）
1. EventBus
2. Broadcast（IPC）
3. 文件（IPC）
4. ContentProvider（IPC）
5. Messenger（IPC）
6. AIDL（IPC）
7. Socket（IPC）
8. 管道
9. SIGNAL

**IntentService** 
使用：继承+重写 onHandleIntent(intent)
处理异步请求的 Service，内部使用了 HandlerThread，一个执行一个请求Handler 机制，任务结束自动退出

## BroadcastReceiver

两种方式
1. 静态注册（常驻），程序关闭后也会被系统调用？？
2. 动态注册，跟随 Activity 生命周期

有序广播 sendOrderedBroadcast ：优先级高的先接收（不分静态和动态）；同优先级广播，动态优于静态；

同优先级同类广播：静态，先扫描的优于后扫描的；动态，先注册的优于后注册的；

默认广播满足以上规则

## ContentProvider

跨进程通信一种方式


## IPC

Inter-Process Communication

序列化和 Binder

Bundle、文件共享、AIDL、Messenger、ContentProvider、Socket、广播

## [Handler](https://www.yuque.com/docs/share/0adad94b-b20c-437d-a1fc-f8d329517f37?#)

1. 是什么（线程消息通信机制（共享内存模型和消息传递模型）；主要场景是子线程切换到主线程）
2. 为什么存在（主线程不能进行耗时操作；子线程不能更新 UI（UI 线程不安全；加锁耗时+逻辑复杂）；）
3. 核心 API（Message、MessageQueue、Looper、Handler、ThreadLocal）
4. 原理
    1. Looper.loop() 和 MessageQueue.next() 内部都是 for(;;) 无限循环，
    2. Looper.loop() 通过 MessageQueue.next() 获取 msg，然后调用 msg.target.dispatchMessage(msg) 即交给 Handler 处理消息
    3. MessageQueue.next() 1. 有消息时返回msg；2.没消息时通过 nativePollOnce 阻塞；3. 队列退出返回 null
    4. 根据 msg.when 系统相对时间进行 MessageQueue 的插入或排队
    5. dispatchMessage 处理优先级 msg.callback > mCallback > handleMessage
5. 使用方式
    1. 主线程
    2. 子线程
    3. 场景：反射获取队列的值
    4. HandlerThread，IntentService
    5. 第三方库 RxJava、EventBus
6. 注意事项
    1. 切换线程的本质 ThreadLocal
    2. MessageQueue 是一个链表
    3. Handler 依赖 Looper 处理消息，而 Looper 和线程绑定
    4. Handler.post/postAtTime/postDelayed/postAtFrontOfQueue 系列方法通过 getPostMessage(Runnable r){msg.callback = r} 最终调用 Handler.sendMessage/sendMessageDelayed/sendMessageAtTime/sendEmptyMessage/sendEmptyMessageDelayed/sendEmptyMessageAtTime/sendMessageAtFrontOfQueue 系列方法
    5. 内存泄漏（静态内部类+弱引用；移除消息；）
    6. 主线程死循环问题（程序运行本质保证不会退出；主线程的消息循环机制；Linux的循环异步pipe/epoll方式不占用资源）
    7. 主线程卡顿问题（运行在子线程交互；onCreate/onStart/onResume的超时 ANR）
7. 其他
    1. 子线程更新 UI 的方式。1. 实现 Handler；2. runOnUiThread；3.view.post(Runnable)
    2. 子线程不能更新 UI 的原因，ViewRootImpl 的 checkThread()在 Activity 维护的 View 树的行为
    3. 子线程 Toast 、showDialog，是在 Window.addView，并非在 ViewRootImpl，所以可以在子线程更新 UI，需要初始化子线程的 Looper.prepare()/.loop
    4. 切记调用 Looper.myLooper().quit()
    5. interface IdleHandler { boolean queueIdle()}，空闲 Handler，方法返回 false表示只回调一次
    

MessageQueue：
入队操作 boolean enqueueMessage(msg, when)：
1. nextPollTimeoutMills -1,0,1
2. msg.target==null, postSyncBarrier (ViewRootImpl.scheduleTraversals)

出队操作 Message next():
1. 处理 barrier，忽略同步消息，找出第一个异步消息
2. 设置 mBlocked 是否阻塞
    
Native 层：

nativePollOnce()
nativeWake()

`/frameworks/base/core/jni/android_os_MessageQueue.cpp`
`/system/core/libutils/Looper.cpp`
NativeMessageQueue - Looper(Native) - epoll_create 创建 - epoll_ctl(mEpollFd, EPOLL_CTL_ADD) 添加监听描述符事件-  pollOnce - pollInner - epoll_wait 返回已经准备好的事件数目 - wait_event_interruptible - read

nativeWake() - wake_up_interruptible - write

Linux 层：

32为操作系统寻址空间为 4G（2 的 32 次方）
用户空间 <-> 内核空间
高 1G 字节 <-> 低 3G 字节
0xC0000000-0xFFFFFFFF <->  0x00000000-0xBFFFFFFF
[Linux IO模式及 select、poll、epoll详解](https://segmentfault.com/a/1190000003063859)

[阻塞 IO](https://www.linuxidc.com/Linux/2011-08/41747p6.htm)
挂起
睡眠
阻塞

文件描述符 File Description，适用于 UNIX/Linux
文件句柄 File Handle，使用于 Windows
都是一种表述指向文件的引用的抽象化概念

缓存 I/O 即标准 I/O，先拷贝到内核缓冲区（文件系统的页缓存 Page Cahce），然后从内核缓冲区拷贝到应用程序的地址空间。
缺点，多次数据拷贝，对 CPU 和内存开销较大

synchronous IO：blocking io, non-blocking io, I/O multiplexing, singal-driven I/O
asynchronous IO

多路复用机制：一个进程监视多个描述符，
1. select 阻塞轮训 文件描述符
    1. 监视 3 类文件描述符 writefds、readfds、exceptfds
    2. 缺点：数量存在最大限制，Linux一般为 1024
2. poll 阻塞轮询 事件
    1. 使用 pollfd 的指针实现，包含要监视的 event 和发生的 event
    2. 缺点：和 select 一样，需要通过**遍历文件描述**符获取已经就绪的 Socket
3. epoll（Event-Poll），阻塞通知监听回调 callback，select 和 poll 的增强版，一个文件描述符管理多个描述符存放到内核的事件表中，这样用户空间和内核空间的 copy 只需一次
    1. 核心方法
        1. epoll_create
        2. epoll_ctl
        3. epoll_wait
    2. 工作模式 
        1. LT(level trigger水平出发)默认。epoll_wait 检测到事件发送给应用程序，应用程序可以**不立即处理**。下次调用 epoll_wait 时，会**再次响应**应用程序并通知此事件。好脾气，缺省工作方式，支持 block 和 non-block Socket
        2. ET(edge trigger边缘出发)。同上应用程序必须**立即处理**，如果不处理，下次**不会响应**应用程序和事件直到你改变。任性，高速工作方式，只支持 non-block Socket，避免阻塞读写
`/external/ltp/testcases/kernel/syscalls/epoll2/include/epoll.h`
`/external/libevent/epoll_sub.c`

[深入理解 MessageQueue](https://pqpo.me/2017/05/03/learn-messagequeue/)
[深入理解 Android 消息机制](https://juejin.im/post/5a27b3db6fb9a045211e950a)
[Linux IO模式及 select、poll、epoll详解](https://segmentfault.com/a/1190000003063859)

存在背景：
1. 主线程不能执行耗时操作
2. 子线程不能更新 UI（控件非线性安全，锁耗时，逻辑复杂）

laucher -> Zygote -> forkProcess -> ActivityThread.main -> Looper.prepareMainLooper -> attach() 与 AMS 绑定 ApplicationThread Binder线程池 -> Looper.loop()

主要 API：

Handler <-- Looper
Looper <-- MessageQueue, ThreadLocal

Looper.loop()
```
# Looper
public static void loop() {
    final Looper me = myLooper();
    for(;;) {
        Message msg = me.mQueue.next()
        msg.target.dispatchMessage(msg);
    }
}    
```

MessageQueue.next()

```
# MessageQueue
Message next() {
    for(;;){
        nativePollOnce(ptr, nextPollTimeoutMillis);
        return msg;
        return null;
    }
}
```
Handler.postMessage -> sendMessage -> MessageQueue.enqueueMessage
```
boolean enqueueMessage(Message msg, long when) {
    // 根据 when 排队列
    when < p.when
    ... 
}

```

Message.what / arg1 / arg2 / when / target


线程使用消息机制前提
1. 创建 Looper.prepare()，内部创建了MessageQueue，并和当前线程绑定
2. 开启循环 Looper.loop()

Handler 处理消息靠 dispatchMessage，内部有三种处理消息方式优先级依次为：
1. message 中的设置的 Callback，msg.callback
2. 实例化 new Handle(Callback)
3. Handler 重写的 handleMessage() 方法
4. 原理 
5. Handler、Thread、HandlerThread 区别
6. 子线程使用方式
7. 主线程的动力
8. 内存泄漏
9. Looper 死循环没有阻塞的原因



使用：Messenger，HandlerThread

## AIDL
Android 接口定义语言，Android Interface Definition Language
目的：进程间通信，尤其是多进程并发情况下的多进程间通信

[Android：学习AIDL，这一篇文章就够了(上)](https://blog.csdn.net/luoyanglizi/article/details/51980630)

对比：
1. BroadcastReceiver：占用系统资源较多
2. Messenger：跨进程通信时的请求队列是同步进行的，无法并发执行 

语法：
1. 文件类型 .aidl 而不是 .java
2. 数据类型
    1. 八种基本类型
    2. String 类型
    3. CharSequence
    4. List 类型
    5. Map类型
    6. 自定义类型必须导包，即使在同一个包下
3. 定向 tag
    1. in，client -> server
    2. out，client <- server
    3. inout，client <-> server
4. oneway [oneway 以及in、out、inout参数的理解](https://blog.csdn.net/anlian523/article/details/98476033)
    1. 不能有返回值，可以在接口前或方法前添加 oneway
    2. 不能有带 out/inout 的参数
5. 两种 AIDL 文件，只是定义 没有实现
    1. 定义 parcelable 对象，即非默认数据类型
    2. 定义方法接口，供系统使用完成跨进程通信
6. 注意事项
    1. 文件路径 `java.srcDirs = ['src/main/java', 'src/main/aidl']`
    2. Book.aidl 和 Book.java 两个文件
    3. Book.java 实现 Parcelable，添加readFromParcel（out）和writeToParcel（in）
7. 使用步骤
    1. 定义 AIDL
    2. 编写服务端
    3. 编写客户端
8. API
    1. mRemote.transact 挂起当前线程
    2. onTransact
    3. _data, _reply, _result
  
  
## Binder
[Weishu Binder学习指南](http://weishu.me/2016/01/12/binder-index-for-newer/)
[Android 面试考 Binder，看这一篇就够了](https://xiaozhuanlan.com/topic/9405168327)
[从源码角度看Binder](https://xiaozhuanlan.com/topic/7248096135)

binder 粘合剂，意为将系统不同模块粘合成一个整体
传统的 Linux 通信机制：Socket、管道等都是内核支持的。
Binder 不是 Linux 内核的一部分，而是 LKM 机制

LKM Loadable Kernel Module 动态可加载内核模块，模块是具有独立功能的程序，可以被单独编译，但不能独立运行。运行时被链接到内核作为内核的一部分在内核空间运行。

Key：
涉及 Application、framework、native、kernel
用户空间、内核空间
Binder 线程池
Binder 框架负责管理对象间的引用计数、内核空间缓冲区、通信模型等
ServiceManager 进程管理各个系统服务



Binder是什么？
1. 面向对象的思想，不用关心驱动、SM、等实现细节，一个 Binder 对象代表了所有，这就是抽象，Binder 模糊了进程边界，淡化了进程间的通信，整个系统仿佛运行于一个面向对象的程序之中。形形色色的 Binder 对象以及星罗棋布的引用粘结各个应用程序的胶水
2. 通常来说：指一种通信机制
3. 对 Server进程 来说：指 Binder 本地对象 I.Stub Binder BBinder  binder_node 实体
4. 对 Client进程 来说：指 Binder 代理对象 I.Proxy  BinderProxy BpBinder binder_refs 引用（句柄）
5. 对   传输过程  来说：Binder 是可以进行跨进程传递的对象；Binder 驱动会对具有跨进程传递能力的对象做特殊处理：自动完成代理对象和本地对象的转换

四个角色：
Client进程 - Binder 驱动（ServiceManager 进程）- Server 进程

`/frameworks/base/core/java/android/app/IActivityManager.aidl`
`/frameworks/base/core/java/android/app/IApplicationThread.aidl`

```
oneway interface IApplicationThread {
// oneway 说明 AMS 发往应用的通知不用挂起等待
...
```

对比：
IPC 方式：Binder、Socket/消息队列/管道、共享内存
1. 传输性能，拷贝次数
2. 安全性，Binder 协议本身支持对通信双方做身份验证
3. 易用性

API：
1. IBinder 代表跨进程传输的能力
2. IInterface 代表调用契约
3. Stub-Binder
4. Proxy-BinderProxy

步骤：
1. 注册服务
2. 获取服务
3. 使用服务
    
    
Binder 设计需求，通信模型（C/S）和通信协议 `/bionic/libc/kernel/uapi/linux/android/binder.h`
Binder 在系统不同部分的表述方式和作用
Binder 在数据接收端的设计考虑，线程池管理、内存映射、等待队列管理
    
## [Android 系统启动流程](https://mp.weixin.qq.com/s/G0TMCvOOC57_2GiheesKYQ)

fork() 对多线程不友好，容易造成死锁

Binder多线程，所以 Zygote 不采用 Binder

```
ROM BootLoader RAM
	init 进程解析 init.rc
		挂载文件系统
		创建工作目录
		启动系统服务进程 Zygote、service manager、media
					system_server
						AMS、WMS、PMS 等
						Launcher 应用的 HomeActivity 桌面
```

## 性能优化

### 1. 布局优化
本质减少 View 的层级

1. Stub 实现懒加载
2. inclue + merge 减少层级
3. 自定义 View 优化
    1. 绘制从 ViewRoot 的 performTraversals
    2. 视图通过 Window 呈现，WindowManager 管理 View，Window 和 DecorView 的通信依赖 ViewRoot
 
4. RelativeLayout 和 LinearLayout 的选择
    1. LinearLayout 复杂布局会深层次嵌套
    2. RelativeLayout 由于子 View 彼此依赖需要做两次 measure；而 LinearLayout 先判断横竖方向只测量一次，内部如果使用了 weight 属性则会测量两次
    3. onMeasure、onLayout、onView 耗时比较
    4. padding 代替 margin，RelativeLayout 的两次测量受子 View 高度限制
    5. 综上：不影响层级选择 LinearLayout 和 FrameLayout，而不是 RelativeLayout

5. 工具检验
    1. [Hierarchy Viewer](https://developer.android.google.cn/studio/profile/hierarchy-viewer.html)
    2. [Layout Inspector](https://developer.android.google.cn/studio/debug/layout-inspector.html)
    3. [Lint](https://developer.android.google.cn/studio/write/lint#commandline)

### 2. 内存优化

### 3. 启动优化

### 4. 网络优化

### 5. 安装包优化

## 自定义 View

涉及内容：布局、绘制、触摸反馈
[扔物线 自定义 View 系列](https://hencoder.com/)
[自定义 View 全解](https://www.jianshu.com/p/705a6cb6bfee)

1. 分类
    1. 自定义组合空间
    2. 继承系统 View/ViewGroup 控件
    3. 继承 View/ViewGroup
2. 绘制流程, 从 ViewRootImpl.doTraversal() 执行下面三个方法
    1. measure: measure(), setMeasureDimension(), onMeasure()
    2. layout: layout(),setOpticalFrame()/setFrame(), onLayout()
    3. draw: draw()
        1. drawBackground(canvas), draw the background
        2. save canvas
        3. onDraw(canvas), draw the content
        4. dispatchDraw(canvas), draw the children
        5. draw the fading edges
        6. onDrawForeground(canvas), 绘制装饰, 如滚动条等
    4. 注意
        1. onMeasure() 中对 wrap_content 属性进行处理
        2. onDraw()中对 padding 的处理
        3. ViewGroup 对子 View 的负责处理
        
3. 坐标系
    1. Android 坐标系
    2. View 坐标系
        1. 相对于自身: getX()/getY()
        2. 相对于原点: getRawX()/getRawY()
        3. 相对于父 View: getLeft()/getTop()/getRight()/getBottom()
        4. 自身宽高 width = getRight() - getLeft(); height = getBottom() - getTop();
4. 构造函数
    1. TestView(Content context) 自动调用在 java 代码
    2. TestView(Context context, AttributeSet attrs) 自动调用在 xml 中
    3. TestView(Context context, AttributeSet attrs, int defStyleAttr) 不自动调用
    4. TestView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) API>21使用, 不自动调用
5. 自定义属性
    1. 步骤
        1. 创建 values/attrs.xml 文件
        2. 编写 `declare-styleable` 及 `attr`属性
        3. 在布局文件中使用自定义的属性
        4. 在自定义 View 的构造方法中通过 TypeArray 获取属性
    2. attr 属性的类型 
        1. format: reference, color, boolean, dimension, float, integer, string, fraction
        2. enum 枚举值 
        3. flag 位运算
        4. 混合类型 |

        ```
        <attr name="actionBarTheme" format="reference" />
        <flag name="none" value="0x00000000" />
        <enum name="horizontal" value="0" />
        <attr name="background" format="reference|color" />
        ```

    
        
    
    