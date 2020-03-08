
[https://blog.csdn.net/freekiteyu/article/details/70082302]()(https://blog.csdn.net/freekiteyu/article/details/70082302)

[Weishu Binder 学习指南][2]
[ Weishu  Android插件化原理解析——Hook机制之Binder Hook][3]
[hook startActivity 的几种姿势][4]
[一篇文章了解相见恨晚的AndroidBinder进程间通讯机制 ][5]



---

BpBinder，Binder代理服务对象。
BnBinder，Binder本地服务对象。

binder\_node 与 binder\_ref 都存在与内核空间。
binder\_node 是实体对象；binder\_ref 是引用对象；
binder\_node 被 binder\_ref 引用，binder\_ref  被 BpBinder 引用。

---

Linux 原有的 IPC 机制为何不用，却偏偏使用 Binder ？

原因 | 说明
--- | ---
传输性能 (数据拷贝次数)| Binder：1次；\<br/\>  Socket / 消息队列 / 管道：2次；\<br/\>  共享内存：1次； \<br/\> 虽然共享内存只需一次，但使用复杂
安全性 | 传统的IPC方式没有严格的安全措施。\<br/\> 比如传统的Socket，IP地址可以由客户端手动填入，调用者的身份很容易伪造。 \<br/\>而Binder，因为 Android 系统为会每个已安装的应用添加属于自己的UID，Binder基于此特性会根据请求端的UID来鉴别调用者的身份，保证了安全性。
易用性 | Binder使用的 C/S 通信方式，一个进程可以开启服务专门负责处理某个模块的业务，多个进程可以作为Client同时向Server发起请求。\<br/\> 使用了面向对象的设计，发起一次binder call就像在调用本地方法一样简单。

---

如何获取和添加Binder服务？

获取和添加Binder服务的操作是交给大管家 ServiceManager 去做的；
ServiceManager 也是一种Binder服务。
ServiceManager进程启动后会通过 binder\_loop 睡眠等待客户端的请求，如果进程被客户端唤醒就会调用 svcmgr\_handler 来处理获取或者添加服务的请求。

---

ServiceManager 也是一种Binder服务，应用该如何获取它的服务呢？

典型鸡生蛋还是蛋生鸡的问题。

当应用获取ServiceManager服务的代理时，它的handle句柄固定为0，所以才不需要去查找。

---

Binder 都有什么主要的协议？

BC\_TRANSACTION
BR\_TRANSACTION
BC\_REPLY
BR\_REPLY

xx\_TRANSACTION：传输请求数据
xx\_REPLY：回复数据

BC\_：Binder Command，进程发送给Binder驱动数据时携带的协议。
BR\_：Binder Return，Binder驱动发送给进程数据时携带的协议。

本质上就是一种约定字段，和日常开发的约定一样

---

Binder驱动加载过程中有哪些重要的步骤？

1. binder\_init：初始化Binder驱动环境、内核工作队列、文件系统节点、misc设备等
2. binder\_open：打开Binder设备，获取Binder驱动的文件描述符fd
3. binder\_mmap：将用户进程地址空间映射到Binder驱动设备内存。这也是Binder能够实现内存一次拷贝依赖的根本
4. binder\_ioctl：Binder驱动的核心功能，用来进行数据的读写操作，IO Control

---

Binder 的死亡通知机制的作用是什么，它是如何实现的？

Binder服务端进程死亡后，依赖着Binder实体对象的客户端代理对象也会失效。

当Binder服务无效时，驱动程序会发送死亡通知给各个已注册服务的客户端进程，已方便客户端做些销毁之类的操作。

举例：死亡通知机制最常用在APP与AMS服务，典型的C/S架构，当APP端的进程死亡后，其ApplicationThreadNative会被销毁，随后Binder驱动会发出死亡讣告给AMS，方便清理已经失效了的四大组件及应用进程信息。

//  TODO如何实现

---

binderService所绑定的服务概念 和 Binder中的服务Server有什么区别？

本质上，binderService所绑定的服务和Binder的服务是同一类，他们实现进程间通信的原理都是借助了Binder这一套机制。

binderService绑定的服务是四大组件的Service；content.bindService可以让Activity和Service形成一种绑定概念。

Binder的服务则是一个真正的C/S架构中的服务端角色。

writeStrongBinder 与 readStrongBinder 的作用和原理？

作用：实现两个进程之间的双工通信。

原理：都是直接调用native方法实现的。

Parcel 在Binder IPC 担任这信息的载体的角色，通过它，App之间可以互相声明与调用跨进程的服务。

---

每个进程最多存在多少个Binder线程，这些线程都被占满后会导致什么问题？

```
`// --frameworks/native/libs/binder/ProcessState.cpp
# define DEFAULT_MAX_BINDER_THREADS 15

static int open_driver()
{
int fd = open("/dev/binder", O_RDWR);
if (fd \>= 0) {
...
size_t maxThreads = DEFAULT_MAX_BINDER_THREADS;
result = ioctl(fd, BINDER_SET_MAX_THREADS, &maxThreads);
}
return fd;
}
```
\`
线程数量是在Binder驱动初始化时被定义的；进程池中线程个数上线为15个，加上主线程Binder线程，一共最大能存在16个binder线程。

占满后，会阻塞等待。

---

使用Binder 传输数据的最大限制是多少，被占满后会导致什么问题？

```
`// --frameworks/native/libs/binder/ProcessState.cpp
```
\`
同步空间是1016K，异步空间只有它的一般508K。

当占满后，Binder驱动不再处理，并在c++层抛出DeadObjectException到binder客户端。

---

Binder 驱动什么时候释放缓冲区的内存？

是在binder call完成之后，调用Parcel.recycle来完成释放内存的

---

为什么使用广播传输 2MB的Bitmap会抛异常，而使用AIDL生成的Binder接口传输Bitmap就不会抛异常呢？

Binder 传输Bitmap，如果Bitmap大于128K，那么传输Bitmap内容的方式就会使用ashmem，Binder只需要负责传输ashmem的fd到服务端即可。
广播传输数据，Bitmap被填入Bundle中，随后以Parcelable的序列化方式传输，如果Bundle的数据量大于800K，就会抛出TransactionTooLargeException的异常。

依据Bitmap是否过大来使用ashmem还是Binder方式传输内容的逻辑在native层的Bitmap\_createFromParcel。

这种 Binder + ashmem的方式在Android中很常见，比如四大组件中的ContentProvider.query方法，从Provider中查找的数据通常会超过1016K这个限制，使用ashmem不仅能突破这个限制，还有提高大量数据传输的效率。

---

应用进程为什么支持Binder通信，直接可以使用四大组件呢？

应用在调用Application.onCreate之前，Binder驱动的初始化就已经完成了，所以直接就可以使用Binder来通信。

1. 所有应用的进程都是通过AMS.startProcessLocked方法来fork Zygote进程创建的；Zygote在启动时会在preloadClasses中预先加载上千个类，而在fork子进程中，这些操作就不需要再做，大大节约了子进程的启动时间。
2. 应用进程的Binder驱动初始化操作是在Zygote fork自身之后做的
3. system\_server 与 Zygote 的通信使用的是Socket，之所以不使用binder的原因很简单，想想
4. Zygote进程初始化操作完成后，Zygote会通过socket返回给 system\_server 的 pid，随后 AMS会将pid和应用的Application进行绑定。

---

[Android 面试考 Binder，看这一篇就够了]()(https://xiaozhuanlan.com/topic/9405168327)

---

Binder设计基础

ioctl：内核/用户空间调用。
它是设备驱动中对设备 I/O 通道进行管理的函数。
每次调用需要三个参数：fd，command 和 需要传入到内核的数据。

mmap()：内核/用户空间内存映射
内存映射的作用：将用户空间的一段内存区域映射到内核空间，映射之后，用户再对这段内存机型读写操作，都会和内核空间的对应内存区域保持一致。
使用mmap进行普通用户空间与内核空间的映射，是实现进程间通信的物理基础。
重要方法：copy\_to\_user 和 copy\_from\_user

[2]:	http://weishu.me/2016/01/12/binder-index-for-newer/
[3]:	http://weishu.me/2016/02/16/understand-plugin-framework-binder-hook/
[4]:	https://www.cnblogs.com/0616--ataozhijia/p/8572959.html
[5]:	https://blog.csdn.net/freekiteyu/article/details/70082302
