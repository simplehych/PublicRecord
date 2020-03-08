# Android 开发艺术探索

## 1 Activity的生命周期和启动模式
### 1.1 Activity的生命周期的全面分析
#### 1.1.1 典型情况下的生命周期分析
onCreate -\> onRestart -\> onStart -\> onResume -\> onPause -\> onStop -\> onDestroy

![activity\_lifecycle][image-1]

Note：
（1）onStart和onStop是从Activity是否可见这个角度来回调的，而onResume和onPause是从Activity是否位于前台这个角度来回调的。所以当新打开的Activity
是透明的可以看到下面的Activity时，下面的Activity不会走onStop方法
（2）旧Activity先onPause，然后新Activity再启动onCreate -\> onStart -\> onResume -\> onStop ，所以不能再onPause中做重量级的操作，因为onPause执行完之后新的Activity才能Resume。
#### 1.1.2 异常情况下的生命周期分析
1. 情况1:资源相关的系统配置发生改变导致Activity被杀死并重新创建
举例：图片资源drawable-mdpi、drawable-hdpi、drawable-land。当旋转屏幕时，Activity会重新创建

![activity\_exit\_accident][image-2]

onSaveInstanceState - onRestoreInstanceState

2. 情况2:资源不足导致低优先级的Activity被杀死
（1）前台Activity - 正在和用户交互的Activity，优先级最高
（2）可见但非前台Activity - 比如Activity中弹出一个对话框，导致Activity可见，但是后台无法和用户直接交互。不走onStop
（3）后台Activity - 已经被暂停的Activity，比如执行了onStop，优先级最低

3. 屏幕旋转不重新创建Activity
在AndroidManifest.xml声明configChanges属性，orientation |screenSize，将不执行onSaveInstanceState和onRestoreInstanceState来存储和恢复数据，取而代之的调用了onConfigurationChanged方法。

### 1.2 Activity的启动模式和标志位
#### 1.2.1 LaunchMode
启动模式:
（1）standard，标准模式
（2）singleTop，栈顶复用模式
（3）singleTask，栈内复用模式，单实例模式
（4）singleInstance，单实例模式，加强版的singleTask模式

![activity\_lauch\_mode\_stack\_1][image-3]

![activity\_lauch\_mode\_stack\_1][image-4]

指定启动模式方式:
（1）清单文件

```
`\<activity
android:name="com.chapter.chapter_1.SecondActivity"
android:configChange="screenLayout"
android:launchMode="singleTask" /\>
```
\`
（2）代码Intent设置标志位

```
`Intent intent = new Intent();
intent.setClass(MainActivity.this, SecondActivity.class);
intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
startActivity(intent);
```
\`


两种区别：
优先级上，第二种的优先级高于第一种，当两种同时存在时，以第二种为准。
第一种无法直接为Activity设定FLAG\_ACTIVITY\_CLEAR\_TOP标识，而第二种无法为Activity指定singleInstance模式。

某个Activity所需的任务栈：
taskAffinity，任务相关性，这个参数标志了一个Activity所需要的任务栈的名字，默认情况下都是包名。该属性的值为字符串，且中间必须含有包名分隔符“.”。
（1）当TaskAffinity和singleTask配对使用，待启动的Activity会运行在和TaskAffinity相同的任务栈中。
（2）当TaskAffinity和allowTaskReparenting配对使用，会产生特殊的效果。应用A 启动 应用B（未启动） 的某个Activity，如果这个Activity的allowTaskReparenting属性为true，那么当应用B 从桌面被启动后，此Activity会直接从应用A的任务栈转移到应用B的任务栈中。

#### 1.2.2 Activity 的 Flags
FLAG\_ACTIVITY\_NEW\_TASK，singleTask启动模式
FLAG\_ACTIVITY\_SINGLE\_TOP，singleTop启动模式
FLAG\_ACTIVITY\_CLEAR\_TOP，具有此标记的Activity，当它启动时，同一任务栈中所有位于它上面的Activity都要出栈，一般和FLAG\_ACTIVITY\_NEW\_TASK配合使用，在这种情况下，被启动的Activity的实例如果已经存在，那么系统就会调用它的onNewIntent。如果被启动的Activity采取的standard模式启动，那么它连同它之上的Activity都要出栈，系统会创建新的Activity实例并放入栈顶。singleTask启动模式默认就具有此标记的效果。
FLAG\_ACTIVITY\_EXCLUDE\_FROM\_RECENTS，等同于xml中指定Activity的属性android:excludeFromRecents="true"，具有这个标记的Activity不会出现在历史Activity的列表中。

### 1.3 IntentFilter 的匹配规则
启动Activity分为两种，显示调用和隐式调用。
显示调用需要明确地指定被启动对象的组件信息，包括包名和类名。
隐式调用则不需要明确指定组件信息。
二者共存的话以显示调用为主。

IntentFilter中的过滤信息有action/category/data。

## 2 IPC机制
### 2.1 IPC简介
Inter-Process Communication，进程间通信/跨进程通信

线程是CPU调度的最小单元，同时线程是一种有限的系统资源
进程是一个执行单元，在PC和移动设备上指一个程序或者一个应用

进程和线程是包含与被包含的关系

Android间进程通信的方式：Binder，Socket

Socket还支持**两个终端**之间的通信。

多进程的情况分两种：
第一种：应用因为某些原因自身需要采用多进程模式实现，比如，有些模块由于特殊原因需要运行在单独的进程，或者为了加大一个应用可使用的内存需要多进程来获取多份内存空间。
第二种：向其他应用获取数据，两个应用跨进程获取数据。

### 2.2 多进程模式
#### 2.2.1 开启多进程模式
方式一：四大组件在AndroidManifest中指定 android:process 属性
方式二：通过JNI在native层fork一个新的进程

查看进程信息：adb shell ps | grep com.ryg.chapter\_2

进程名以":"开头的进程属于应用的私有进程
不以":"开头的进程属于全局进程，其他应用通过ShareUID方式可以和它跑在同一进程中。
#### 2.2.2 多进程模式的运行机制
Android为每一个应用分配了独立的虚拟机，或者说为每个进程分配一个独立的虚拟机。
不同的虚拟机在内存分配上有不同的地址空间，这就导致在不同的虚拟机中访问同一个类的对象会产生多份副本。
运行在同一个进程中组件是属于同一个虚拟机和同一个Application的。

多进程造成的问题：
（1）静态成员和单例模式完全失效
（2）线程同步机制完全失效
（3）SharePreferences的可靠性下降
（4）Application会多次创建

### 2.3 IPC基础概念
#### 2.3.1 Serializable接口
#### 2.3.2 Parcelable接口
#### 2.3.3 Binder
Binder 是Android中的一个类，它实现了IBinder接口。
从IPC角度来说，Binder是Android中的一种跨进程通信方式；
Binder还可以理解为一种虚拟的物理设备，它的设备驱动是/dev/binder，该通信方式在Linux中没有；
从Android Framework角度来说，Binder是ServiceManager连接各种Manager和相应ManagerService的桥梁；
从Android应用层来说，Binder是客户端和服务端进行通信的媒介，当bindService的时候，服务端会返回一个包含了服务端业务调用的Binder对象，通过这个Binder对象，客户端就可以获取服务端提供的服务或者数据，这里的服务包括普通服务和基于AIDL的服务。

### 2.4 IPC方式
#### 2.4.1 使用Bundle
三大组件Activity/Service/Receiver支持Intent中传递Bundle数据
#### 2.4.2 使用文件共享
适合在对数据同步要求不高的进程之间通信，并且要妥善处理并发读/写的问题
#### 2.4.3 使用Messenger-信使
串行处理，并发请求不合适
底层实现是Binder，它对AIDL做了封装
#### 2.4.4 使用AIDL
(1)使用自定义的Parcelable对象，必须在新建一个和它同名的AIDL文件，并在其中声明Parcelable类型
（2）参数必须标上方向，in：输入型参数；out：输出型参数；inout：输入输出型参数（3）AIDL接口只支持方法，不支持声明静态常量
(4) AIDL 方法是在服务端的Binder线程池中执行的

客户端调用远程服务的方法，被调用的方法运行在服务端的Binder线程池汇总，同时客户端线程会挂起，如果服务端方法执行比较耗时，就会导致客户端线程长时间阻塞在这里，而如果客户端的线程是UI线程的话，就会导致客户端ANR。

客户端调用服务端的方法和服务端调用客户端的方法 运行在 Binder线程池中

Binder是可能意外死亡的，需要重新连接，两种方法：

1. 给Binder设置DeathRecipient监听，当Binder死亡时，会收到binderDied方法的回调
2. 在onServiceDisconnected中重连远程服务。

区别：
onServiceDisconnected在**客户端的UI线程**中被回调，而binderDied在**客户端的Binder线程池**中被回调。也就是说binderDied不能访问UI

在AIDL中进程权限验证，两种常用方法：

1. 在客户端onBind中进行验证，验证不通过就直接返回null，这样验证失败客户端直接无法绑定服务，至于验证方式有多种，比如permission验证，这种方法需在AndroidManifest中声明所需的权限。
2. 在服务端的onTransact方法进行权限验证，验证失败就直接返回false.

除了校验permission，还可以验证Uid、Pid、包名等，服务端通过getCallingUid和getCallingPid拿到客户端所属应用的Uid和Pid

为Service指定android:permission属性等


为什么有的操作需要权限？？
https://blog.csdn.net/weixin\_37077539/article/details/56279789





#### 2.4.5 使用ContentProvider
底层实现是Binder
是Android提供的专门用于不同应用间进行数据共享的方式
对ContentProvider进行CRUD增删改查操作
#### 2.4.6 使用Socket
套接字，是网络通信中的概念，分为#流式套接字#和用户数据报套接字两种，分别对应于网络的传输控制层中的#TCP#和#UDP#协议。
TCP协议是面向连接的协议，提供稳定的双向通行功能，TCP连接的建立需要经过“三次握手”才能完成，为了提供稳定的数据传输功能，其本身提供了超时重传机制，因此具有很高的稳定性。
UDP是无连接的，提供不稳定的单向通信功能，当然UDP也能实现双向通信功能。
在性能上，UDP具有更好的效率，其缺点是不保证数据不一定能正确传输，尤其是在网络拥塞的情况下。
### 2.5 Binder 连接池

### 2.6 选用合适的IPC方式

名称 | 优点 | 缺点 | 使用场景
--- | --- | --- | ---
Bundle | 简单易用 | 只传输Bundle 支持的数据类型 | 四大组件间的进程间通信
文件共享 | 简单易用 | 不适合高并发场景，并且无法做到进程间的及时通信 | 无并发访问情形，交换简单的数据实时性不高的场景
AIDL | 功能强大，支持一对多并发通信，支持实时通信 | 使用稍微复杂，需要处理好线程同步 | 一对多通信且有RPC需求 Remote Procedure Call 远程过程调用
Messenger | 功能一般，支持一对多串形通信，支持实时通信 | 不能很好处理高并发情形，不支持RPC数据通过Message进程传输，因此只能传输Bundle支持的数据类型 | 低并发的一对多即时通信，无RPC需求，或者无须要返回结果的RPC需求
ContentProvider | 在数据源访问方面功能强大，支持一对多并发数据共享，可通过Call方法扩展其他操作 | 可以理解为受约束的AIDL，主要提供数据源的CRUD操作 | 一对多的进程间的数据共享
Socket | 功能强大，可以通过网络传输直接流，支持一对多并发实时通信 | 实现细节稍微有点繁琐，不支持直接的RPC | 网络数据交换 

## 3. View的事件体系

### 3.1 View的基础知识
#### 3.1.1 什么是View
#### 3.1.2 View的位置参数
#### 3.1.3 MotionEvent 和 TouchSlop
#### 3.1.4 VelocityTracker、GestureDetector 和 Scroller

### 3.2 View的滑动
#### 3.2.1 使用scrollTo/scrollBy
#### 3.2.2 使用动画
#### 3.2.3 改变布局参数
#### 3.2.4 各种滑动方式的对比

### 3.3 弹性滑动
#### 3.3.1 使用Scroller
#### 3.3.2 通过动画
#### 3.3.3 使用延时策略
Handler#postDelayed 以及 Thread#sleep 等。
对于sleep方法来说，通过在while循环中国不断地滑动View和sleep

### 3.4 View的事件分发机制
#### 3.4.1 点击事件的传递规则
dispatchTouchEvent/onInterceptTouchEvent/onTouchEvent


## 8 理解Window和WindowManager
Window是一个抽象类，它的具体实现是PhoneWindow。
创建一个Window，只需要通过WindowManager即可完成。
WindowManager是外界访问Window的入口，具体实现位于WindowManagerService中。
WindowManager和WindowManagerService的交互是一个IPC过程。
Android 中所有的视图都是通过Window来呈现的，不管是Activity、Dialog还是Toast，实际上都是附加在Window上的，因此Window实际是View的直接管理者。

### 8.1 Window和WindowManager

比较重要的两个参数
flags：表示Window的属性
FLAG\_NOT\_FOCUSABLE，表示不需要获取焦点，也不需要接收各种输入事件，此标记会同时启用FLAG\_NOT\_TOUCH\_MODAL，最终事件会直接传递给下层的具有焦点的Window
FLAG\_NOT\_TOUCH\_MODAL，在此模式下，系统会将当前Window区域以外的单击事件传递给底层的Window，当前Window区域以外的单击事件则自己处理。这个标记很重要，一般来说都需要开启此标记，否则其他Window将无法收到单击事件
FLAG\_SHOW\_WHEN\_LOCKED，开启此模式可以让Window显示在锁屏的界面上

type：表示Window的类型
三种：应用Window、子Window、系统Window
应用类Window对应着一个Activity。
子Window不能单独存在，它需要附属在特定的父Window之中，比如Dialog
系统Window是需要声明权限才能创建的Window，比如Toast和系统状态栏

Window是分层的，每个Window都有对象的z-ordered，**层级大** 的会**覆盖**在 **层级小** 的Window的上面。
应用Window的层级范围是 1～99 FIRST\_APPLICATION\_WINDOWLASTAPPLICATIONWINDOW
子Window的层级范围是 1000～1999 FIRST\_SUB\_WINDOWLASTSUBWINDOW
系统Window的层级范围是 2000～ 2999 FIRST\_SYSTEM\_WINDOWLASTSYSTEMWINDOW

系统类型的Window需要检查全县，没有申请会报错。`<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />`

WindowManager提供的功能很简单，常用的只有三个方法，即 添加View、更新View、删除View，这三个方法定义在ViewManager中，而WindowManager继承了ViewManager。

Q:WindowManager$BadTokenException...permission denied for window type 2000
A:type为2000异常，设置type有问题，其次检查权限申请

### 8.2 Window的内部机制
Window是一个抽象的概念，每一个Window都对应着一个View和一个ViewRootImpl，Window和View通过ViewRootImpl来建立联系，因此Window并不是实际存在的，它是以View的形式存在的。

提供的三个接口都是针对View的，这说明View才是Window的存在实体。
为了分析Window的内部机制，这里从Window的添加、删除以及更新说起

#### 8.2.1 Window的添加过程
Window - WindowManager - WindowManagerImpl - WindowManagerGlobal 

1. 检查参数是否合法，如果是子Window那么还需要调整一些布局参数
2. 创建ViewRootImpl并将View添加到列表中
3. 通过ViewRootImpl来更新界面并完成Window的添加过程
	WindowSession最终来完成Window的添加过程，它是一个Binder对象，添加过程是一次IPC调用
	Session内部会通过WindowManagerService来实现Window的添加

#### 8.2.2 Window的删除过程

WindowManagerGlobal#removeView - WindowManagerGlobal#removeViewLocked - ViewRootImpl#die() - ViewRootImpl#doDie() - ViewRootImpl#dispatchDetachedFromWindow ( mView.dispatchDetachedFromWindow();mWindowSession.remove(mWindow);) - WindowManagerGlobal.getInstance().doRemoveView(this);

在doDie的内部会调用dispatchDetachedFromWindow方法，真正删除View的逻辑在dispatchDetachedFromWindow方法的内部实现：

1. 垃圾回收相关的工作，比如清除数据和消息、移除回调
2. 通过Session的remove方法删除Window，mWindowSession.remove(mWindow)，这是一个IPC过程，最终会调用WindowManagerService的removeWindow方法
3. 调用View的dispatchDetachedFromWindow方法，在内部会调用View的onDetachedFromWindow()，这个方法不会陌生，在View从Window中移除，这个方法会被调用，做一些资源回收的工作，比如终止动画、停止线程等。

#### 8.2.3 Window的更新过程
更新View的LayoutParams并替换掉老的LayoutParams，
接着更新ViewRootImpl中的LayoutParams，这一步是通过ViewRootImpl的setLayoutParams方法来实现的。
在ViewRootImpl中会通过scheduleTraversals方法来对View重新布局，包括测量、布局、重绘着三个过程。
除了View本身的重绘以外，ViewRootImpl还会通过WindowSession来更新Window的视图 relayoutWindow()，它同样是一个IPC过程

### 8.3 Window的创建过程

View是Android中的视图呈现方式，但是View不能单独存在，它必须附着在Window这个抽象的概念上面，因此有视图的地方就有Window。

#### 8.3.1 Activity 的 Window 创建过程
Window的创建过程必须了解Activity的启动过程，详细过程在第九章，Activity的启动过程很复杂，最终会由ActivityThread中的performLaunchActivity()来完成


#### 8.3.2 Dialog的Window创建过程
和Activity类似
> 必须采用Activity的Context，用applicationContext否则会报错`token null is not for an application`
> 另外，系统对话框的Window比计较特殊，可以不要token，需要制定系统的type值 2000～2999

#### 8.3.3 Toast的Window创建过程





## 9 四大组件的工作过程
远程过程调用
### 9.1 四大组件的运行状态
Activity 展示型组件
Service 计算型组件
BroadcastReceiver 消息型组件
ContentProvider 共享型组件

#### 9.2 Activity 的工作过程

#### 9.3 Service 的工作过程
两种状态：
**启动状态**，执行后台计算,startService(intentService);
**绑定状态**，用于其他组件和Service的交互,bindService(intentService,mServiceConnection,BIND\_AUTO\_CREATE);
两种状态可以共存
##### 9.3.1 Service 的启动过程

最终在 ActivityThread 的 handleCreateService 方法完成Service的最终启动

handleCreateService主要完成如下几件事：
首先通过类加载器创建Service的实例
然后创建Application对象并调用其onCreate，当然Application的创建过程只会有一次
接着创建ContextImpl对象并通过Service的attach方法建立二者之间的关系，这个过程和Activity实际上是类似的，毕竟Service和Activity都是一个Context
最后调用Service的onCreate方法并将Service对象存储到ActivityThread中的一个列表中。该表为 `final ArrayMap<IBinder, Service> mServices = new ArrayMap<>();`

##### 9.3.2 Service 的绑定过程
使用方式 `bindService(intentService,mServiceConnection,BIND_AUTO_CREATE);`

从 ContextWrapper#bindService()开始 -\> ContextImpl#bindService() -\> ContextImpl#bindServiceCommon()

binderServiceCommon 完成两件事
1.将客户端的 **ServiceConnection** 对象转化为**ServiceDispatcher.InnerConnection** 对象。之所以不能直接使用ServiceConnection对象，因为**服务的绑定有可能是跨进程的**，因此ServiceConnection对象必须借助于Binder才能让远程服务端回调自己的方法，而ServiceDispatcher的内部类InnerConnection刚好充当了Binder这个角色。

2.通过AMS完成Service的具体并定过程
AMS会调用ActiveServices#bindServiceLocked() -\> bringUpServiceLocked -\> realStartServiceLocked -\> 最终通过 ApplicationThread来完成Service实例的创建并执行onCreate()方法。

和启动Service不同的是，Service的绑定过程会调用app.thread的scheduleBindService方法。这个过程的实现在 ActiveServices#bindServiceLocked() -\> ActiveServices#requestServiceBindingLocked()

**app.thread** 这个对象多次出现过，它实际上就是**ApplicationThread**。ApplicationThread的一系列以**schedule开头的方法**，其内部通过**Handler H**来中转，对于scheduleBindService方法也是如此。

在H内部，接收到BIND\_SERVICE消息后，会交给ActivityThread的handleBindService方法来处理。

原则上来说，Service的onBind方法被调用以后，Service就处于绑定状态了，但是onBind方法是Service的方法，客户端并不知道已经成功连接Service了，所以必须调用客户端的ServiceConnection中的onServiceConnected，这个过程是在 AMS的publishService中完成的。

AMS 的 publishService 方法将具体的工作交给了 ActiveServices 类型的 mServices 对象来处理，核心代码一句话 c.conn.connected(r.name, service, false)，其中 c.conn 的类型是 ServiceDispatcher.InnerConnection，service 就是Service 的onBind返回的Binder对象

#### 9.4 BroadcastReceiver 的工作过程
主要包括两方面的内容：

1. 广播的注册过程 （静态注册-AndroidManifest，动态注册-代码）
2. 广播的发送和接收过程

使用方法：

1. 定义接收者（继承BroadcastReceiver并重写onReceive方法）

```
`public class MyReceiver extends BroadcastReceiver {
@Override
public void onReceive(Context context, Intent intent){
// 不能做耗时操作，参考值：10s
String action = intent.getAction();
// do some works
}
}
```
\`
2. 注册
	静态注册

	```
	`\<receiver android:name=".MyReceiver"\>
	\<intent-filter\>
	\<action android:name="com.simple.receiver.LAUNCH"\>
	\</intent-filter\>
	\</receiver\>
	```
	\`
	动态注册，需要解注册

	```
	`MyReceiver receiver = new MyReceiver();

	// 注册
	IntentFilter filter = new IntentFilter();
	filter.addAction("com.simple.receiver.LAUNCH");
	registerReceiver(receiver, filter);

	// 解注册
	unregisterReceiver(receiver);
	```
	\`
3. 发送

```
`Intent intent = new Intent();
intent.setAction("com.simple.receiver.LAUNCH");
sendBroadcast(intent);
```
\`
##### 9.4.1 广播的注册过程

静态注册的广播在应用安装时由系统自动完成注册，由**PMS-PackageManagerService**来完成整个注册过程，除了广播以外，其他三大组件也都是在应用安装时由PMS解析并注册的。

Activity#registerReceiver() -\> ContextImpl#registerReceiver() -\> ContextImpl#registerReceiverInternal()

registerReceiverInternal()中系统先从 mPackageInfo 获取 IIntentReceiver 对象，然后再采用跨进程的方式向AMS发送广播注册的请求。

之所以采用**IIntentReceiver**而不是直接采用BroadcastReceiver，这是因为上述注册过程是一个进程间通信的过程，而BroadcastReceiver作为Android的一个组件是不能直接跨进程传递的，所以需要IIntentReceiver来中转一下。

IIntentReceiver必须是一个Binder接口，它的具体实现是LoadedApk.ReceiverDispatcher.InnerReceiver，ReceiverDispatcher 的内部同时保存了BroadcastReceiver和InnerReceiver。

由于注册广播的真正实现在AMS中，registerReceiver方法会把远程的InnerReceiver对象以及IntentFileter对象存储起来，这样整个广播的注册过程就完成了

##### 9.4.2 广播的发送和接收过程

当send发送广播时，AMS会查找出匹配的广播接收者，并将广播发送给他们处理。

广播的发送几种类型：普通广播、有序广播和粘性广播。
有序广播和粘性广播 与普通广播相比具有不同的特性，但是发送/接收过程的流程时类似的。

### 9.5 ContentProvider 的工作过程

ContentProvider 是一种内容共享型组件，它通过Binder向其他组件乃至其他应用提供数据

当一个应用启动时，入口方法为ActivityThread的main()方法。

ActivityThread会创建Application对象并加载ContentProvider。需要注意的是，ActivityThread会**先加载ContentProvider**，然后**再调用Application的onCreate()方法**。








## 10 Android 的消息机制
Handler 是 Android 消息机制的上层接口。
**Android 的消息机制主要是指Handler的运行机制**，Handler的运行需要底层的MessageQueue和Looper的支撑。
MessageQueue采用单链表的数据结构来存储消息列表。
Looper以无限循环的形式去查找是否有新消息，如果有的话就处理消息，否则就一直等待着。

### 10.1 Android 的消息机制概述

### 10.2 Android 的消息机制分析
#### 10.2.1 ThreadLocal的工作原理
#### 10.2.2 MessageQueue 的工作原理
尽管叫消息队列，但是它的内部实现是**单链表**的数据结构来维护消息列表，在插入和删除上比较有优势
插入：enqueueMessage，往消息队列中插入一条消息
读取：next，从消息队列中取出一条消息，并将其从消息队列中移除
next()是一个无限循环的方法，如果，消息队列中没有消息，那么next方法会一直阻塞在这里。当有新消息到来时，next方法会返回这条消息并将其从单链表中移除。
#### 10.2.3 Looper 的工作原理
#### 10.2.4 Handler 的工作原理
Handler 的工作主要包含消息的发送和接收过程。
消息的发送通过post()/send()等一系列方法实现，post()最终通过send()实现。
Handler 发送消息的过程仅仅是向消息队列中插入了一条消息，MessageQueue的next方法就会返回这条消息给Looper，Looper收到消息后就开始处理了，最终消息由Looper交由Handler处理，即Handler的dispatchMessage方法会被调用。这时Handler就进入了处理消息的阶段，dispatchMessage的实现如下：

```
`public void dispatchMessage(Message msg){
if( msg.callback != null ){
handleCallback(msg);
} else {
if( mCallback != null ){
if( mCallback.handleMessage(msg)){
return;
}
}
handleMessage(msg);
}  
}
```
\`处理消息的过程如下：
首先，检查Message的Callback是否为null，不为null就通过handleCallback来处理消息，Message的callback是一个Runnable对象，实际上就是Handler的post方法所传递的Runnable参数。

### 10.3 主线程的消息循环




## 11 Android的线程和线程池

1. 在操作系统中，线程是操作系统调度的最小单元；
2. 同时线程又是一种受限的系统资源，不可无限制地产生，并且创建和销毁都会有相应的开销；
3. 当系统存在大量的线程时，系统会通过**时间片轮转的方式**调度每个线程，因此线程**不可能绝对的并行**，**除非**线程数量小于CPU的核心数
4. 在一个进程中频繁地创建和销毁线程，这显然不是高效的做法，正确的做法是采用**线程池**，一个线程池会**缓存一定数量的线程**，避免频繁创建和销毁线程所带来的系统开销。

线程分为 **主线程** 和 **子线程**
主线程：运行四大组件以及处理它们和用户的交互（即处理界面交互逻辑），也叫**UI线程**
子线程：执行耗时操作（网络请求，I/O操作等），也叫**工作线程**

扮演程池的角色：AsyncTask / IntentService / HandlerThread
只是表现形式区别于传统线程Thread，但是本质都是传统线程Thread

表现形式 | 底层实现 | 说明 | 目的
--- | --- | --- | ---
AsyncTask |  线程池 | 封装了线程池和Handler | 方便开发者在子线程中更新UI
HandlerThread |  线程 | 消息循环的线程，内部可以使用 Handler
IntentService | 线程 | 1.是一个服务，执行后台任务 2.内部采用 HandlerThread 来执行任务 3.当任务执行完毕IntentService自动退出

### 11.1 主线程和子线程
主线程是指**进程所拥有的线程**，在Java中默认情况下一个进程只有一个线程。
主线程主要处理界面交互相关的逻辑，因为用户**随时**会和界面发生交互

避免主线程由于被耗时操作阻塞从而出现ANR现象。

### 11.2 Android中的线程形态
AsyncTask、HandlerThread、IntentService
不同的API版本AsyncTask具有不同的表现，尤其是多任务的并发执行上。
#### 11.2.1 AsyncTask\<Params, Progress, Result\>
#### 11.2.2 AsyncTask的工作原理
注意其中处理并发，atomic原子的使用；synchronized；
#### 11.2.3 HandlerThread
#### 11.2.4 IntentService

### 11.3 Android中的线程池
#### 11.3.1 ThreadPollExecutor
#### 11.3.2 线程池的分类
FixedThreadPool
CacheThreadPool
ScheduleThreadPool
SingleThreadExecutor






## 12 Bitmap 的加载和Cache
Android 对单个应用所施加的内存限制，比如 16MB，导致加载Bitmap的时候很容易出现内存溢出。
缓存策略：LruCache内存缓存 和 DiskLruCache存储缓存。
Lru：Least Recently Used，最近最少使用算法，当缓存快满时，会淘汰近期最少使用的缓存目标。
### 12.1 Bitmap 的高效加载
1. 如何加载一个Bitmap
BitmapFactory类提供了四类方法：**decodeFile**、**decodeResource**、**decodeStream**、**decodeByteArray**。分别支持从 **文件**系统、**资源**、**输入流**、**字节数组** 中加载一个Bitmap对象。其中 decodeFile 和 decodeResource 间接调用了 decodeStream 方法。

2. 如何高效加载 Bitmap
核心：采用BitmapFactory.Options 来加载尺寸图片，主要用到了它的 **inSampleSize** 参数，即采样率。
举例：一张 1024 * 1024 像素的图片，采用格式 ARGB8888 格式存储，内存占用 1024 * 1024 * 4，即 4M。如果 inSampleSize 为 2，那么采样后的图片其内存占用只有 512 * 512 \* 4，即 1M。宽和高均减少，导致压缩后的图片以采样率为2次方递减。
只有inSampleSize大于1才有缩小的效果，小于1作用相当于1，无缩放效果。
如果不为2的指数，那么会向下取整数选择一个接近2的指数代替，比如3，系统会选择2代替。经验证并非所有的Android版本上都成立，因此把它当成一个开发建议即可。

步骤：
（1）将BitmapFactory.Options的inJustDecodeBounds参数设为true，只会解析图片的原始宽高信息，并不会真正加载图片。
（2）从BitmapFactory.Options中取出图片的原始宽高信息，它们对应于outWidth和outHeight参数
（3）根绝采样率的规则并结合目标View的所需大小计算出采样率的inSampleSize

```
`public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight){
final int height = option.outHeight;
final int width = option.outWidth;
int inSampleSize = 1;

if(heigh \> reqHeight || width \> reqWidth){
final int halfHeight = heigh / 2;
final int halfWidth = width / 2;
while( (halfHeight / inSampleSize) \>= reqHeight 
&& (halfWidth / inSampleSize) \>= reqWidth)){
inSampleSize *= 2;
}
}
return inSampleSize;
}
```
\`
（4）将BitmapFactory.Options的inJustDecodeBounds参数设为false，然后重新加载图片

### 12.2 Android 中的缓存策略
缓存，内存 - 存储设备 - 网络
策略，包括缓存的添加、获取和删除这三类操作。内存缓存和存储设备缓存的大小都是有限制的。

LRU，Least Recently Used，近期最少使用算法。

#### 12.2.1 LruCache

LruCache 是一个**泛型类**，内部采用一个 **LinkedHashMap** 以 **强引用** 的方式存储外界的缓存对象，提供了 get 和 put 方法来完成缓存的获取和添加操作，

强引用：直接对象引用
软引用：系统内存不足时，被gc回收
弱引用，随时，被gc回收

是线程安全的，方法都加锁，sychronized
```
`public class LruCache\<K,V\> {
private final LinkedHashMap\<K,V\> map;
}
```
\`
典型的初始化过程：
```
`int maxMemory = (int)(Runtime.getRuntime().maxMemory() / 1024);
int cacheSize = maxMemory / 8;
mMemoryCache = new LruCache\<String, Bitmap\>(cacheSize) {
@override
protected int sizeOf(String key, Bitmap bitmap){
return bitmap.getRowBytes() * bitmap.getHeight() / 1024;
}
}
```
\`
重写sizeOf方法，计算缓存对象的大小。

获取缓存 `mMemory.get(key)`
添加缓存 `mMemory.put(key, bitmap)`
删除缓存 `mMemory.remove(key)`

### 12.2.2 DiskLruCache
用于实现存储设备缓存，即磁盘缓存。但不属于Android SDK的一部分

1. 创建
并不能通过构造方法创建，提供了open方法
```
`public static DiskLruCache open(File directory, int appVersion, int valueCount, long maxSize);
```
\`2. 添加，Editor
url作为key，但是可能有特殊字符，一般采用 url 的 md5作为key

```
`DiskLruCache.Editor editor = mDiskLruCache.edit(key);
editor.commit(); 提交操作，真正将图片写入到文件系统
// editor.abort(); 回退
```
\`
3. 查找，Snapshot
通过Snapshot对象即可得到缓存的文件输入流
```
`DiskLruCache.Snapshot snapShot = mDiskLruCache.get(key);
FileInputStream fileInputStream = (FileInputStream)snapShot.getInputStream();
```
\`4. remove/delete等操作

### 12.2.3 ImageLoader 的实现
功能如下：
图片的同步加载
图片的异步加载
图片压缩
内存缓存
磁盘缓存
网络拉取

## 12.3 ImageLoader 的使用

### 12.3.1 照片墙效果
### 12.3.2 优化列表的卡顿现象
1. 不要在 getView 中执行耗时操作
2. 控制异步的执行频率，当列表停止是加载图片
3. 开启硬件加速


# 13 综合技术
## 13.1 使用 CrashHandler 来获取应用的 crash 信息
## 13.2 使用 multidex 来解决方法数越界
（1）使用方法
（2）指定 主dex 文件中所要包含的类
（3）局限。应用启动速度降低，因为会加载额外的dex文件；Dalvik linearAlloc的bug，multidex无法在Android4.0以前的手机上运行，需要做大量的兼容性测试

## 13.3 Android的动态加载技术
动态加载技术，也叫插件化技术
三个基础问题：资源访问，Activity生命周期的管理，ClassLoader的管理。

## 13.4  反编译初步
### 13.4.1 使用 dex2jar 和 jd-gui 反编译apk dex -\> jar
### 13.4.2 使用 apktool 对 apk 进行二次打包  dex -\> smali

## 14 JNI 和 NDK 编程
JNI：java native interface （Java本地接口），为了方便Java调用C、C++等本地代码所封装的一层接口
NDK是Android所提供的一个工具集合，通过NDK可以在Android中更加方便地通过JNI来访问本地代码。  NDK还提供了交叉编译器，开发人员只需要简单修改mk文件就可以生成特定CPU平台的动态库
### 14.1 JNI的开发流程
1. 编写带有 native 声明的方法的 Java 类
2. 使用 javac 命令编译编写的Java类，生成 .class 文件
3. 使用 javah 命令生成后缀名为 .h 的头文件
4. 使用 C/C++实现本地方法
5. 将本地方法编写的文件生成动态链接库

在src主目录下：javac Hello.java  生成 -\> Hello.class
在src主目录下：javah Hello 生成 -\> com.simple.Hello.h
新建 jni 文件，编写C/C++文件 HelloC.c，并将 com.simple.Hello.h 转移到该目录
gcc -shared -I /Library/Java/JavaVirtualMachines/jdk1.8.0\_144.jdk/Contents/Home/include -fPIC HelloC.c -o libhello.so
回到src主目录下：java -Djava.library.path=jni com.simple.Hello，其中 -Djava.libarary.path=jni 指定so库的路径。或者在 代码中设置System.setProperty("java.library.path", ".");
最终 链接库失败，问题原因

### 14.2 NDK的开发流程
### 14.3 JNI的数据类型和类型签名
类的签名，采用`L/包名/类名；`，例如`Ljava/lang/String;`其中末尾的`；`也是签名的一部分

Java类型 | 签名 | 备注
--- | --- | ---
String | Ljava/lang/String; | 类的签名，统一格式
boolean | Z | B已经被byte占用
byte | B |
char | C |
short | S |
int | I |
long | J | L 是类的签名
float | F |
double | D |
void | V |

方法的签名：(参数类型签名)返回值类型签名
举例：
boolean fun1(int a, double b, int[]() c) -\> (ID[I)Z
]()int fun1() -\> ()I
void fun1(int i) -\> (I)V

### 14.4 JNI调用Java方法的流程

## 15 Android 性能优化

### 15.1 Android的性能优化方法
#### 15.1.1 布局优化
（1）首先**删除无用**的控件和层级
（2）RelativeLayout的功能比较复杂，布局过程需要花费更多的CPU时间。所以优先选择**LinearLayout**和F**rameLayout**。但是嵌套还是建议采用**RelativeLayout**，因为嵌套相当于增加了布局的层级，同样会降低性能。
（3） 
`<include>`， 用于布局重用
`<merge>`，和`include>`搭配使用减少层级
`ViewStub`，按需加载，通过 `setVisibility` 或者 `inflate` 方法才加载。不支持\<merge\>标签

#### 15.1.2 绘制优化
View的 onDraw 方法避免执行大量的操作
（1）**不要创建新的局部对象**。onDraw会被频繁调用，会一瞬间产生大量的临时对象。这样导致 1、占用过多的内存 2、导致系统频繁的gc。最终降低了程序的执行效率。
（2）**不要做耗时操作，不做大量循环操作**。大量循环抢占CPU时间片，造成绘制不流畅，不能保证60fps，每秒60帧，1000/60=16ms

#### 15.1.3 内存泄漏优化
（1）避免写内存泄漏的代码
1 声明周期长的对象持有声明周期短的对象
2 未解注册对象
3 使用弱应用
（2）通过工具找出潜在的内存泄漏继而解决

内存泄漏场景：

1. 静态变量导致的内存泄漏

```
`public class MainActivity extends Activity {
private static Context sContext;

protect void onCreate(Bundle savedInstanceState){
sContext = this;
}
}
```
\`
2. 单例模式导致的内存泄漏。缺少解注册监听
3. 属性动画导致的内存泄漏。没有在onDestroy中停止动画，虽然无法在洁面看到动画效果了，当Activity的View会被动画持有，而View又持有了Activity，最终Activity无法释放。
4. 响应速度和ANR日志分析。Activity 5s，BroadcastReceiver 10s。ANR日志在 /data/anr 目录下  `adb pull /data/anr/traces.txt`。注意子线程和主线程抢占同步锁的情况
5. ListView优化：1. ViewHolder并避免在getView中执行耗时操作 2.根据滑动状态来控制任务的执行频率，比如快滑动不适合开启大量的异步任务 3.开启 硬件加速
	Bitmap优化：使用BitmapFactory，并用到 BitmapFactory.Options 的 inSampleSize参数
6. 线程优化：线程池。线程池可以重用内部的线程，从而避免线程的穿件和销毁所带来的性能开销；同时还能有效的控制线程池的最大并发数，避免大量的线程因抢占系统资源从而导致阻塞现象的发生。
7. 优化建议
	避免创建过多的对象
	不要过多使用枚举，占用空间比整型大
	常量使用static final来修饰
	使用Android特有的数据结构，SparseArray和Pair
	使用软引用和弱引用
	采用内存缓存和磁盘缓存
	尽量采用静态内部类，这样可以避免潜在的由于内部类而导致的内存泄漏

### 15.2 内存泄漏分析之 MAT 工具
MAT 全称是 Eclipse Memory Analyzer。
Histogram 矩阵图 - 直观地看出内存中不同类型的**buffer**的数量和占用的内存大小
Dominator Tree - 内存中的对象按照从大到小的顺序排序，并且分析对象之间的引用关系。例如，开始垃圾回收一下 -\> bitmap -\> 右击鼠标右键 -\> Path To GC Roots -\> exclude wake / soft references 排除弱引用和软引用，因为二者有较大的几率被gc回收，并不能造成内存泄漏







[image-1]:	file:///.file/id=6571367.12976185490 "activity_lifecycle"
[image-2]:	file:///.file/id=6571367.12976185489 "activity_exit_accident"
[image-3]:	file:///.file/id=6571367.12976185492
[image-4]:	file:///.file/id=6571367.12976185491