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

