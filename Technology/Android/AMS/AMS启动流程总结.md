# AMS启动流程

本文在 API 27 上开展分析

## 摘要


## 关键词

1. AMS：ActivityManagerService 
2. 应用app；系统system
3. 

### step1 构建SystemServer的Android运行环境
`Zygote` 创建出的第一个进程是 `SystemServer`

`Zygote#main() -> Zygote#forkSystemServer()`

`SystemServer#main() -> SystemServer#run()` 

```
#SystemServer#run()
private void run() {
    // Initialize the system context.
    // step 1
    createSystemContext();
    ...
    // Create the system service manager.
    // SystemServiceManager 负责启动所有的系统服务
    mSystemServiceManager = new SystemServiceManager(mSystemContext);
    ...
    LocalServices.addService(SystemServiceManager.class, mSystemServiceManager);
    ...
    // step 2
    startBootstrapServices();
    ...
    // step 3
    startOtherServices();
    ...
}
```

当 SystemServer 调用 createSystemContext 完毕后：

1. 得到一个 ActivityThread 对象，它代表当前进程（系统进程）的主线程
2. 得到一个 Context，对于 SystemServer 而言，它包含 Application 运行环境 和 framework-res.apk（名为"android"的LoadedAPK） 有关。

Q：为什么启动所有的服务前，SystemServer先要调用createSystemContext？
A：众所周知，Android是以Java为基础的，Java运行在进程中。
但是，Android努力构筑一个自己的运行环境，组件的运行和它们之间的交互均在该环境中实现。

createSystemContext函数就是为**SystemServer进程**搭建一个和**应用进程**一样的**Android
环境**

**Android运行环境**，是构建在**进程**之上的，但是**进程的概念被模糊化**，应用程序一般只和Android运行环境交互。

基于同样的道理，Android 希望 SystemServer进程内部运行的应用，也通过Android运行环境交互，因此才调用了createSystemContext函数。

创建Android运行环境时，
由于SystemServer的特殊性，SystemServer.main() 调用了 ActivityThread.systemMain() 函数
对于普通的应用程序，将在自己的主线程汇中调用 ActivityThread.main() 函数

### step2 AMS初始化
创建完Android运行环境和创建系统服务管理后，SystemServer就调用了 **startBootstrapServices**，其中**创建并启动了AMS**

```
private void startBootstrapServices() {
    Installer installer = mSystemServiceManager.startService(Installer.class);
    ...
    // Activity manager runs the show.
    //启动AMS，然后获取AMS保存到变量中
    // step 2.1
    mActivityManagerService = mSystemServiceManager.startService(
            ActivityManagerService.Lifecycle.class).getService();
    //以下均是将变量存储到AMS中
    mActivityManagerService.setSystemServiceManager(mSystemServiceManager);
    mActivityManagerService.setInstaller(installer); 
    ...
    mPackageManagerService = PackageManagerService.main(mSystemContext, installer,
                mFactoryTestMode != FactoryTest.FACTORY_TEST_OFF, mOnlyCore);
    mPackageManager = mSystemContext.getPackageManager();
    ...
    // Set up the Application instance for the system process and get started.
    // step 2.2
   mActivityManagerService.setSystemProcess();
   ...
   mPackageManagerService.systemReady();
   ...
   mActivityManagerService.systemReady(()->{ ... });
   ...

}
```

注意上面代码并没有**直接**启动AMS，而是启动AMS的内部类`Lifecycle`.
由于AMS并没有继承`SystemService`，因此不能通过`SystemServiceManager.startService()`直接启动（内部实现采用反射的方式），例如可以直接启动`Installer`。

AMS的内部类继承SystemService，像一个适配器来**间接**操作AMS。

```
public static final class Lifecycle extends SystemService {
   private final ActivityManagerService mService;

   public Lifecycle(Context context) {
        super(context);
        // step 2.1.1 调用AMS的构造函数
        mService = new ActivityManagerService(context);
   }

   @Override
   public void onStart() {
        // step 2.1.2 调用AMS的start函数
        mService.start();
   }

   @Override
   public void onCleanupUser(int userId) {
        mService.mBatteryStatsService.onCleanupUser(userId);
   }

   public ActivityManagerService getService() {
        return mService;
   }
}
```

#### step 2.1.1 AMS 构造函数
1. 初始化一些变量

#### step 2.1.2 AMS 的 start 函数
1. 启动CPU监控**线程**，该线程将会开始统计不同**进程**使用CPU的情况
2. 发布一些服务，如BatteryStatsService、AppOpsService（权限管理相关）和LocalService继承ActivityManagerInternal的服务

### 将SystemServer纳入AMS的管理体系
#### step 2.2 setSystemProcess

```
public void setSystemProcess() {
   try {
        // step 2.2.1 以下几个向ServiceManager注册几个服务
        // AMS自己，在这里注册，以后通过ServiceManager获取，最常见的获取key为"activity"的服务
       ServiceManager.addService(Context.ACTIVITY_SERVICE, this, true);
       ServiceManager.addService(ProcessStats.SERVICE_NAME, mProcessStats);
       ServiceManager.addService("meminfo", new MemBinder(this));
       ServiceManager.addService("gfxinfo", new GraphicsBinder(this));
       ServiceManager.addService("dbinfo", new DbBinder(this));
       if (MONITOR_CPU_USAGE) {
           ServiceManager.addService("cpuinfo", new CpuBinder(this));
       }
       ServiceManager.addService("permission", new PermissionController(this));
       ServiceManager.addService("processinfo", new ProcessInfoService(this));

        // step 2.2.2 通过 PMS 查询 package 名为 "android" 的应用的ApplicationInfo，即系统应用
       ApplicationInfo info = mContext.getPackageManager().getApplicationInfo(
               "android", STOCK_PM_FLAGS | MATCH_SYSTEM_ONLY);
               
        // step 2.2.3 通过查询的结果进行安装
       mSystemThread.installSystemApplicationInfo(info, getClass().getClassLoader());

        // step 2.2.4 AMS进程的管理
       synchronized (this) {
           ProcessRecord app = newProcessRecordLocked(info, info.processName, false, 0);
           app.persistent = true;
           app.pid = MY_PID;
           app.maxAdj = ProcessList.SYSTEM_ADJ;
           app.makeActive(mSystemThread.getApplicationThread(), mProcessStats);
           synchronized (mPidsSelfLocked) {
               mPidsSelfLocked.put(app.pid, app);
           }
           updateLruProcessLocked(app, false, null);
           updateOomAdjLocked();
       }
   } catch (PackageManager.NameNotFoundException e) {
       throw new RuntimeException(
               "Unable to find android system package", e);
   }
}
```

AMS的setSystemProcess有四个主要功能：

1. 注册一个服务，如 AMS 本身
2. 获取package名为"android"的应用的ApplicationInfo，即系统应用
3. 调用 ActivityThread 的 installSystemApplication
4. AMS 进程管理相关的操作


##### step 2.2.1 注册服务 ServiceManager.addService()

```
# ActivityManagerService#setSystemProcess()

ServiceManager.addService(Context.ACTIVITY_SERVICE, this, true);
```

注意平时常用的 AMS **获取**，是在这里进行 **添加** 的


##### step 2.2.2 获取系统应用信息 getApplicationInfo

```
# ActivityManagerService#setSystemProcess()

ApplicationInfo info = mContext.getPackageManager().getApplicationInfo(
                    "android", STOCK_PM_FLAGS | MATCH_SYSTEM_ONLY);
```

###### step 2.2.2.1 mContext.getPackageManager()

Context 的实现类是 ContextImpl

```
# ContextImpl#getPackageManager
@Override
public PackageManager getPackageManager() {
   if (mPackageManager != null) {
       return mPackageManager;
   }
    // 依赖于ActivityThread，此时为系统进程的主线程，调用ActivityThread的getPackageManager()函数
   IPackageManager pm = ActivityThread.getPackageManager();
   if (pm != null) {
       // Doesn't matter if we make more than one instance.
       // 利用PMS的代理对象，构建ApplicationPackageManager对象（继承PackageManager）
       // Note：PackageManager不继承IPackageManager。IPackageManager继承IInterface，属于Binder接口
       return (mPackageManager = new ApplicationPackageManager(this, pm));
   }

   return null;
}
```

跟进一下ActivityThread的getPackageManager：

```
# ActivityThread#getPackageManager

public static IPackageManager getPackageManager() {
   if (sPackageManager != null) {
       return sPackageManager;
   }
   // 依赖于Binder通信，并通过ServiceManager获取到PMS对应的BpBinder
   IBinder b = ServiceManager.getService("package");
   //得到PMS对应的Binder服务代理
   sPackageManager = IPackageManager.Stub.asInterface(b);
   return sPackageManager;
}
```

从上面我们可以看到，AMS获取PMS用到了Binder通信。

实际上，PMS由SystemServer创建，与AMS运行在用一个进程，且初始化比AMS早。
AMS完全可以不经过Context、ActivityThread、Binder来获取PMS，直接在 SystemServer 中取mPackageManagerService 属性即可

但是代码通过 ActivityThread.getPackageManager 等之前之后一系列操作 获取PMS的原因：
SystemServer 进程汇总的服务，也使用Android运行环境来交互，保留了组件之间交互接口的统一，为未来的系统保留了可扩展性


###### step 2.2.2.2 通过PMS获取 getApplicationInfo()

得到PMS的代理对象后，AMS调用PMS的getApplicationInfo接口，获取package名为"android"的ApplicationInfo。

在AMS的setSystemProcess被调用前，PMS已经启动了
在PMS的构造函数中，它将解析手机中所有的AndroidManifest.xml，然后形成各种数据结构以维护应用的信息。

getApplicationInfo就是通过package名，从对应的数据结构中国呢，取出对应的应用信息。

####  2.2.3 installSystemApplicationInfo

在上一步得到 **framework-res.apk 的 ApplicationInfo** 后，需要将这部分ApplicationInfo保存到SystemServer对应的ActivityThread中。

```
# ActivityMagerService#

mSystemThread.installSystemApplicationInfo(info, getClass().getClassLoader());
```

AMS中的mSystemThread就是SystemServer中创建出的ActivityThread。

跟进一下 ActivityThread的installSystemApplicationInfo函数

```
#ActivityThread

public void installSystemApplicationInfo(ApplicationInfo info, ClassLoader classLoader) {
   synchronized (this) {
        //调用SystemServer中创建出的ContextImpl的installSystemApplicationInfo函数        
       getSystemContext().installSystemApplicationInfo(info, classLoader);
       getSystemUiContext().installSystemApplicationInfo(info, classLoader);

       // give ourselves a default profiler
       // 创建一个Profiler对象，用于性能统计
       mProfiler = new Profiler();
   }
}
```

继续跟进 ContextImpl 的 installSystemApplicationInfo 函数

```
# ContextImpl

void installSystemApplicationInfo(ApplicationInfo info, ClassLoader classLoader) {
    // mPackageInfo 的类型为 LoadedApk
   mPackageInfo.installSystemApplicationInfo(info, classLoader);
}
```

继续跟进 LoadedApk 的 installSystemApplicationInfo 函数

```
/**
* Sets application info about the system package.
*/
void installSystemApplicationInfo(ApplicationInfo info, ClassLoader classLoader) {
    // 这个接口仅供系统进程调用，故这里断言了一下
   assert info.packageName.equals("android");
   mApplicationInfo = info;
   mClassLoader = classLoader;
}
```

至此，我们知道了 installSystemApplicationInfo 的真想就是：
将包名为"android"对应的ApplicationInfo(framework-res.apk)，加入到之前创建的系统LoadedApk中（SystemServer#run#createSystemContext）

原因是：在此之前创建SystemContext时，PMS还没有执行对手机中的文件进行解析的操作，因此初始化的LoadApk并没有持有有效的ApplicationInfo（仅实例化了一个ApplicationInfo对象，设置了packageName属性为"android"，其他并没有设置）。

-

在此基础上，AMS下一步的工作就呼之欲出了

因为AMS是专门用于进程管理和调度的，但是framework.apk运行在SystemServer进程中，所以SystemServer进程也应该在AMS有对应的管理结构。（SystemServer创建了AMS，但是AMS还想管理SystemServer  ^ - ^ ）

于是，AMS的下一步工作就是将SystemServer的运行环境和一个进程管理结构对应起来，并进行统一的管理

#### 2.2.4 AMS进程的管理

```
# AMS#setSystemProcess

...
synchronized (this) {
    // 创建进程管理对应的结构 ProcessRecord
    ProcessRecord app = newProcessRecordLocked(info, info.processName, false, 0);
    
    // 由于此时创建的是SystemServer进程对应的ProcessRecord，因此设置了一些特殊值
    app.persistent = true;
    app.pid = MY_PID;
    app.maxAdj = ProcessList.SYSTEM_ADJ;
    // 将SystemServer对应的ApplicationThread保存到ProcessRecord中
    app.makeActive(mSystemThread.getApplicationThread(), mProcessStats);
    synchronized (mPidsSelfLocked) {
        // 按pid将ProcessRecord保存到mPidsSelfLocked中
        mPidsSelfLocked.put(app.pid, app);
    }
    // TODO 调整 mLruProcess 列表的位置，最近活动过的进程总是位于前列，同时拥有Activity的进程总是前于只有Service的进程
    updateLruProcessLocked(app, false, null);
    // TODO 更新进程对应的oom_adj值，oom_adj将决定进程是否被kill掉
    updateOomAdjLocked();
}
...
```

这里创建了进程管理对应的结构`ProcessRecord`，并设置了系统进程对应的参数，然后添加到AMS中。

-
**总结**
`AMS#setSystemProcess` 函数主要完成任务：

1. 向 ServiceManager 添加注册了一些服务，包括 AMS 自身。
2. 通过  PMS 获取 "android" 系统服务的 ApplicationInfo 信息，并安装到系统服务对应的 LoadedApk 中。
3. 构建 SystemServer 进程对应的ProcessRecord，将SystemServer纳入到AMS的管理中。

### step 3 SystemServer#startOtherServices

接下来AMS启动相关的操作，在SystemServer的startOtherServices函数中

```
# SystemServer

private void startOtherServices() {
    ...
    mActivityManagerService.installSystemProviders();
    ...
}
```

继续跟进AMS的installSystemProviders函数

```
public final void installSystemProviders() {
    List<ProviderInfo> providers;
    synchronized (this) {
        // AMS 根据进程名取出对应的ProcessRecord
      ProcessRecord app = mProcessNames.get("system", SYSTEM_UID);
      // 得到该ProcessRecord对应的ProviderInfo
      providers = generateApplicationProvidersLocked(app);
      // 仅处理系统级的Provider，取出非系统
      if (providers != null) {
          for (int i=providers.size()-1; i>=0; i--) {
              ProviderInfo pi = (ProviderInfo)providers.get(i);
              if ((pi.applicationInfo.flags&ApplicationInfo.FLAG_SYSTEM) == 0) {
                  Slog.w(TAG, "Not installing system proc provider " + pi.name
                          + ": not system .apk");
                  providers.remove(i);
              }
          }
      }
    }
    if (providers != null) {
      // 安装Provider
      mSystemThread.installSystemProviders(providers);
    }
    
    mConstants.start(mContext.getContentResolver());
    // 监控Settings数据库中的secure、System和Global表的变化
    mCoreSettingsObserver = new CoreSettingsObserver(this);
    // 创建监控数据库中字体大小的变化
    mFontScaleSettingObserver = new FontScaleSettingObserver();
    
    // Now that the settings provider is published we can consider sending
    // in a rescue party.
    RescueParty.onSettingsProviderPublished(mContext);
    
    //mUsageStatsService.monitorPackages();
}
```

从上面的代码可以看出，installSystemProviders主要是加载运行在SystemServer进程中的ContentProvider，即SettingProvider.apk（frameworks/base/packages/SettingsProvider）

上面有两个比较重要的函数：

1. generateApplicationProvidersLocked 返回一个进程对应的ProviderInfoList
2. ActivityThread可以看做进程Android运行环境，因此它的installSystemProviders表示为对应进程安装ContentProvider
3. 当SettingProvider被加载到SystemServer进程中运行后，AMS就注册了两个ContentObserver监控SettingsProvider中的字段变化

> AMS监控的字段影响范围比较广，例如字体发生变化时，很多应用的显示界面需要作出调整，这也许就是让AMS来负责监控这些字段的原因

接下来，我们看上面比较重要的两个函数

```
private final List<ProviderInfo> generateApplicationProvidersLocked(ProcessRecord app) {
   List<ProviderInfo> providers = null;
   try {
        // 利用PMS根据进程名及权限，从数据结构中得到进程对应的ProviderInfo
       providers = AppGlobals.getPackageManager()
               .queryContentProviders(app.processName, app.uid,
                       STOCK_PM_FLAGS | PackageManager.GET_URI_PERMISSION_PATTERNS
                               | MATCH_DEBUG_TRIAGED_MISSING, /*metadastaKey=*/ null)
               .getList();
   } catch (RemoteException ex) {
   }
   if (DEBUG_MU) Slog.v(TAG_MU,
           "generateApplicationProvidersLocked, app.info.uid = " + app.uid);
   int userId = app.userId;
   if (providers != null) {
   
        // 通常而言，我们逐渐向容器加入数据时，容器只有在数据超出当前存储空间时
        // 才会运行内存的重新分配（一般乘2）和数据的拷贝
        // 因此若待加入数据总量很大，在逐步向容器加入数据的过程，容器将会有多次重新分配和拷贝的过程
        // 或许整体的开销并不是很惊人，但事先将内存一次分配到位，体现了对极致的追求
       int N = providers.size();
       app.pubProviders.ensureCapacity(N + app.pubProviders.size());
       
       for (int i=0; i<N; i++) {
           // TODO: keep logic in sync with installEncryptionUnawareProviders
           ProviderInfo cpi =
               (ProviderInfo)providers.get(i);
               // 判断是否为单例的
           boolean singleton = isSingleton(cpi.processName, cpi.applicationInfo,
                   cpi.name, cpi.flags);
                   // 针对多用户的处理
                   // 若一个Provider是单例的，但当前进程不属于默认用户，那么这个Provider将不被处理
                   // 简单来说，就是两个用户都启动一个进程时（有了两个进程）
                   // 定义于进程Package中单例的Provider仅运行在默认用户启动的进程中
           if (singleton && UserHandle.getUserId(app.uid) != UserHandle.USER_SYSTEM) {
               // This is a singleton provider, but a user besides the
               // default user is asking to initialize a process it runs
               // in...  well, no, it doesn't actually run in this process,
               // it runs in the process of the default user.  Get rid of it.
               providers.remove(i);
               N--;
               i--;
               continue;
           }

            // 包名和类名组成ComponentName
           ComponentName comp = new ComponentName(cpi.packageName, cpi.name);
           // 创建ContentProvider对应的ContentProviderRecord
           // 加入到AMS的MProviderMap中
           ContentProviderRecord cpr = mProviderMap.getProviderByClass(comp, userId);
           if (cpr == null) {
               cpr = new ContentProviderRecord(this, cpi, app.info, comp, singleton);
               mProviderMap.putProviderByClass(comp, cpr);
           }
           // 将ContentProviderRecord保存在ProcessRecord中
           app.pubProviders.put(cpi.name, cpr);
           if (!cpi.multiprocess || !"android".equals(cpi.packageName)) {
               // Don't add this if it is a platform component that is marked
               // to run in multiple processes, because this is actually
               // part of the framework so doesn't make sense to track as a
               // separate apk in the process.
               // 当ContentProvider仅属于当前进程时，还需要统计该Provider的运行信息
               app.addPackage(cpi.applicationInfo.packageName, cpi.applicationInfo.versionCode,
                       mProcessStats);
           }
           //通知PMS记录该Provider对应包被使用的时间
           notifyPackageUse(cpi.applicationInfo.packageName,
                            PackageManager.NOTIFY_PACKAGE_USE_CONTENT_PROVIDER);
       }
   }
   return providers;
}
```

generateApplicationProvidersLocked 函数实现的主要功能：

1. 从PMS中得到应用对应的ContentProvider
2. 利用应用信息和对应的ContentProvider组成ContentProviderRecord
3. 将ContentProviderRecord按包名保存到AMS的mProviderMap中。原因：AMS需要管理ContentProvider，当一个进程退出时，AMS需要将其中运行的ContentProvider信息从系统中移除
4. 将ContentProviderRecord按包名保存到ProcessRecord中。原因：最终需要落实到一个进程中。


```
public final void installSystemProviders(List<ProviderInfo> providers) {
if (providers != null) {
  installContentProviders(mInitialApplication, providers);
}
}

private void installContentProviders(
       Context context, List<ProviderInfo> providers) {
   final ArrayList<ContentProviderHolder> results = new ArrayList<>();

   for (ProviderInfo cpi : providers) {
       ...
       // 初始化并保存ContentProvider
       ContentProviderHolder cph = installProvider(context, null, cpi,
               false /*noisy*/, true /*noReleaseNeeded*/, true /*stable*/);
       if (cph != null) {
           cph.noReleaseNeeded = true;
           results.add(cph);
       }
   }

   try {
        // 向AMS注册ContentProvider
       ActivityManager.getService().publishContentProviders(
           getApplicationThread(), results);
   } catch (RemoteException ex) {
       throw ex.rethrowFromSystemServer();
   }
}
```

installContentProviders 是安装ContentProvider的通用程序，主要包括两方面的工作：

1. 调用installProvider得到ContentProviderHolder对象，其间完成了对应ContentProvider的初始化等工作
2. 向AMS发布ContentProviderHolder

installProvider

```
private ContentProviderHolder installProvider(Context context,
        ContentProviderHolder holder, ProviderInfo info,
        boolean noisy, boolean noReleaseNeeded, boolean stable) {
    ContentProvider localProvider = null;
    IContentProvider provider;
    if (holder == null || holder.provider == null) {
        // 此时holder==null，进入这个分支
        ...
        Context c = null;
        ApplicationInfo ai = info.applicationInfo;
        
        // 下面判断的作用是：为待安装的ContentProvider找到对应的Application
        // 在 AndroidManifest中，ContentProvider是Application的子标签，因此ContentPro与Application有一种对应关系
        // 本次流程中，传入的context是mInitApplication，代表的是framework-res.apk
        // 而 ai 是从ProviderInfo中获取的，代表的是SettingProvider，SettingProvider.apk所对应的Application还未创建
        // 所有进入最后的else判断中
        if (context.getPackageName().equals(ai.packageName)) {
            c = context;
        } else if (mInitialApplication != null &&
                mInitialApplication.getPackageName().equals(ai.packageName)) {
            c = mInitialApplication;
        } else {
            try {
            	// 以下将重新创建一个Context，指向SettingProvider.apk
            	// ai.packageName = "com.android.provider.settings"

            	// createPackageContext方法中利用package对应的LoadedApk信息，创建新的ContextImpl
            	// 其内部通过 mMainThread.getPackageInfo 取出LoadedApk，在这个过程中，如果已经加载过就直接从存储变量中取，否则同PMS重新构建
                c = context.createPackageContext(ai.packageName,
                        Context.CONTEXT_INCLUDE_CODE);
            } catch (PackageManager.NameNotFoundException e) {
                // Ignore
            }
        }
        
        ...

        try {
        	// 上面必须找到ContentProvider对应的Context的原因：
        	// 1. ContentProvider和Application有对应关系，而Application继承Context
        	// 2. 只有正确的Context才能加载对应APK的Java字节码，从而通过反射创建出ContentProvider实例

        	// 得到对应的ClassLoader
            final java.lang.ClassLoader cl = c.getClassLoader();
            // 反射创建实例
            localProvider = (ContentProvider)cl.
                loadClass(info.name).newInstance();

            // ContentProvider的mTransport对象
            // 变现类型为IContentProvider，实际为ContentProviderNative，即ContentProvider的Binder通信服务端BbBinder
            // Tansport 继承 ContentProviderNative
            provider = localProvider.getIContentProvider();

            ...

            // 初始化ContentProvider，内部会调用ContentProvider的onCreate函数
            localProvider.attachInfo(c, info);

        } catch (java.lang.Exception e) {
            ...
            return null;
        }
    } else {
        provider = holder.provider;
       	...
    }

    ContentProviderHolder retHolder;

    synchronized (mProviderMap) {
        ...
        // 调用ContentProviderNative的asBinder，得到服务端的Binder对象
        IBinder jBinder = provider.asBinder();
        if (localProvider != null) {
            ComponentName cname = new ComponentName(info.packageName, info.name);
            ProviderClientRecord pr = mLocalProvidersByName.get(cname);
            if (pr != null) {
                ...
                provider = pr.mProvider;
            } else {
            	// 根据ProviderInfo创建ContenProviderHolder
                holder = new ContentProviderHolder(info);
                // 使持有provider
                holder.provider = provider;
                holder.noReleaseNeeded = true;

                // 构造ProviderClientRecord，并按authority将其保存在mProviderMap中
                pr = installProviderAuthoritiesLocked(provider, localProvider, holder);
                // 将 pr 按 jBinder 保存在 mLocalProviders 中
                mLocalProviders.put(jBinder, pr);
                // 将 pr 按 cname 保存在 mLocalProvidersByName 中
                mLocalProvidersByName.put(cname, pr);
            }
            retHolder = pr.mHolder;
        } else {
            ...
        }
    }
    return retHolder;
}
```
installProvider的代码较长，但实际思想很简单，就是环环相扣的三步：
1. 创建出ContentProvider对应的ContextImpl（代表对应的运行环境）
2. 利用ContextImpl得到对应的ClassLoader，完成ContentProvider的初始化和启动
3. 得到与ContentProvider通信的BBinder，然后按名称和BBinder，将ContentProvider保存到对应的存储结构中“


ActivityThread 与 ContenProvider 的关系大概如上图所示。
ContentProvider本身只是一个**容器**，其内部持有的 **Transport** 类才是能提供跨进程调用的支持。

Transport类继承自ContentProviderNative类，作为ContentProvider的Binder通信服务端BBinder。
ContentProviderNative中定义了ContentProviderProxy类，将作为Binder通信的服务端代理，为客户端提供交互

在上面的代码中，ActivityThread用mLocalProviders保存运行在本地的ContentProvider，使用的键值的key为ContentProvider的Binder通信服务端。


##### publishContentProviders
ContentProvider 初始化完成后，我们需要向AMS注册它。

```
# ActivityThread#installContentProviders

...
ActivityManager.getService().publishContentProviders(
    getApplicationThread(), results);
...
```

此处通过Binder通信进行调用 AMS的publishContentProviders函数

```
public final void publishContentProviders(IApplicationThread caller,
        List<ContentProviderHolder> providers) {
    if (providers == null) {
        return;
    }

    enforceNotIsolatedCaller("publishContentProviders");
    synchronized (this) {
    	//找到调用者对应的ProcessRecord对象
        final ProcessRecord r = getRecordForAppLocked(caller);
        ...

        final long origId = Binder.clearCallingIdentity();

        final int N = providers.size();
        for (int i = 0; i < N; i++) {
            // ProcessRecord的pubProviders中保存了ContentProviderRecord信息
            // 这是根据PMS解析出的Package信息生成的
            // 此处主要发布的ContentProvider，必须是该Packager已经声明的
            ContentProviderHolder src = providers.get(i);
            ContentProviderRecord dst = r.pubProviders.get(src.info.name);
            ...
            if (dst != null) {
                ComponentName comp = new ComponentName(dst.info.packageName, dst.info.name);
                // 按名称保存到MProviderMap
                mProviderMap.putProviderByClass(comp, dst);
                
                String names[] = dst.info.authority.split(";");
                for (int j = 0; j <  names.length; j++) {
                    // 按 authority保存到mProviderMap
                    mProviderMap.putProviderByName(names[j], dst);
                }
                // mLaunchingProviders 保存处于启动状态的Provider
                int launchingCount = mLaunchingProviders.size();
                int j;
                boolean wasInLaunchingProviders = false;
                for (j = 0; j < launchingCount; j++) {
                    if (mLaunchingProviders.get(j) == dst) {
                    	// 启动完成后，从列表中移除
                        mLaunchingProviders.remove(j);
                        wasInLaunchingProviders = true;
                        j--;
                        launchingCount--;
                    }
                }
                if (wasInLaunchingProviders) {
                	// 取消启动超时的消息
                    mHandler.removeMessages(CONTENT_PROVIDER_PUBLISH_TIMEOUT_MSG, r);
                }
                synchronized (dst) {
                    dst.provider = src.provider;
                    dst.proc = r;
                    // 通知
                    dst.notifyAll();
                }
              	// 没发布一个ContentProvider，均调整对应进程的oom_adj
                updateOomAdjLocked(r, true);
                // 判断，并在需要的时候更新ContentProvider相关的统计信息
                maybeUpdateProviderUsageStatsLocked(r, src.info.packageName,
                        src.info.authority);
            }
        }

        Binder.restoreCallingIdentity(origId);
    }
}
```

publishContentProvider 函数结束后，一个ContentProvider就算正式在系统中注册了。
在AMS的启动过程中，此处注册的是 **SettingProvider**
此后，Setting数据库相关的操作均由它来管理。

注意到上面的ContentProvider注册到AMS后，进行了notifyAll的操作。
举例来说：进程需要查询一个数据库，需要通过进程B中的某个ContentProvider来实施。如果B还未启动，那么AMS就需要先启动B。在这段时间内，A需要等待B注册启动对应的ContentProvider。B一旦完成ContentProvider的注册，就需要告知A退出等待继续后续的查询工作。

### AMS的systemReady

接下来，我们看看AMS启动的最后一部分：systemReady

在SystemServer中的startOtherServices的最后调用了AMS的systemReady函数
可以看到第一个参数为Runnable。

```
# SystemServer#startOtherServices

// We now tell the activity manager it is okay to run third party
// code.  It will call back into us once it has gotten to the state
// where third party code can really run (but before it has actually
// started launching the initial applications), for us to complete our
// initialization.
mActivityManagerService.systemReady(new Runnable(){
    ...
}, BOOT_TIMINGS_TRACE_LOG);
```

由于此处代码量很大，所以分段查看AMS中systemReady的处理流程

阶段一
```
public void systemReady(final Runnable goingCallback, TimingsTraceLog traceLog) {
    traceLog.traceBegin("PhaseActivityManagerReady");
    synchronized(this) {
    	// 第一次mSystemReady=false
        if (mSystemReady) {
            // If we're done calling all the receivers, run the next "boot phase" passed in
            // by the SystemServer
            if (goingCallback != null) {
                goingCallback.run();
            }
            return;
        }

        // 这一部分主要是调用一些关键服务systemReady相关的函数

        mLocalDeviceIdleController
                = LocalServices.getService(DeviceIdleController.LocalService.class);
        mAssistUtils = new AssistUtils(mContext);
        mVrController.onSystemReady();
        // Make sure we have the current profile info, since it is needed for security checks.
        mUserController.onSystemReady();
        mRecentTasks.onSystemReadyLocked();
        mAppOpsService.systemReady();

        // 系统准备完毕
        mSystemReady = true;
    }

    try {
        sTheRealBuildSerial = IDeviceIdentifiersPolicyService.Stub.asInterface(
                ServiceManager.getService(Context.DEVICE_IDENTIFIERS_SERVICE))
                .getSerial();
    } catch (RemoteException e) {}

    ArrayList<ProcessRecord> procsToKill = null;
    synchronized(mPidsSelfLocked) {
        for (int i=mPidsSelfLocked.size()-1; i>=0; i--) {
        	// mPidsSelfLocked中保存当前正在运行的所有进程的信息
            ProcessRecord proc = mPidsSelfLocked.valueAt(i);
            // isAllowedWhileBooting中判断FLAG_PERSISTENT标志
            // 在AMS启动完成前，如果没有 FLAG_PERSISTENT 标志的进程已经启动了，将其加入到procsToKill列表中
            if (!isAllowedWhileBooting(proc.info)){
                if (procsToKill == null) {
                    procsToKill = new ArrayList<ProcessRecord>();
                }
                procsToKill.add(proc);
            }
        }
    }

    synchronized(this) {
        if (procsToKill != null) {
            for (int i=procsToKill.size()-1; i>=0; i--) {
                ProcessRecord proc = procsToKill.get(i);
                Slog.i(TAG, "Removing system update proc: " + proc);
                // 关闭 procsToKill中的进程
                removeProcessLocked(proc, true, false, "system update done");
            }
        }

        // Now that we have cleaned up any update processes, we
        // are ready to start launching real processes and know that
        // we won't trample on them any more.
        
        // 进程准备完毕
        mProcessesReady = true;
    }

    ...

    // 根据数据库和资源文件，获取一些配置参数
    retrieveSettings();
    final int currentUserId;
    synchronized (this) {
    	// 得到当前的用户Id
        currentUserId = mUserController.getCurrentUserIdLocked();
        // 读取 urigrant.xml，为其中定义的 ContentProvider 配置对指定Uri数据的访问/修改权限
        // 原生的代码中，似乎没有 urigrants.xmls
        // 实际使用的 grant-uri-permission 是分布式定义的
        readGrantedUriPermissionsLocked();
    }


    if (goingCallback != null) goingCallback.run();
    traceLog.traceBegin("ActivityManagerStartApps");

    ...

}
```

这一部分的工作主要是调用一些关键服务的初始化函数，然后杀死那些没有 FLAG_PERSISTENT 却在AMS启动完成前已经存在的进程，同时获取一些配置参数。

> 只有Java进程才会向AMS注册，而一般的Native进程不会向AMS注册，因此这里杀死的进程时Java进程

阶段二

```
public void systemReady(final Runnable goingCallback, TimingsTraceLog traceLog) {
    
    ...

    // 调用参数传入的 Runnable对象，SystemServer中有具体的定义
    if (goingCallback != null) goingCallback.run();
    traceLog.traceBegin("ActivityManagerStartApps");
    
    ...
    
    // 内部循环遍历所有的系统服务的onStartUser接口
    mSystemServiceManager.startUser(currentUserId);

    synchronized (this) {
        // Only start up encryption-aware persistent apps; once user is
        // unlocked we'll come back around and start unaware apps
        // 启动persistent为1的Application所在的进程
        startPersistentApps(PackageManager.MATCH_DIRECT_BOOT_AWARE);

        // Start up initial activity.
        mBooting = true;
        // Enable home activity for system user, so that the system can always boot. We don't
        // do this when the system user is not setup since the setup wizard should be the one
        // to handle home activity in this case.
        // 当isSplitSystemUser返回true时，意味着system user和primary user是分离的
        // 这里应该是让system user也有启动 home activity 的权限吧
        if (UserManager.isSplitSystemUser() &&
                Settings.Secure.getInt(mContext.getContentResolver(),
                     Settings.Secure.USER_SETUP_COMPLETE, 0) != 0) {
            ComponentName cName = new ComponentName(mContext, SystemUserHomeActivity.class);
            try {
                AppGlobals.getPackageManager().setComponentEnabledSetting(cName,
                        PackageManager.COMPONENT_ENABLED_STATE_ENABLED, 0,
                        UserHandle.USER_SYSTEM);
            } catch (RemoteException e) {
                throw e.rethrowAsRuntimeException();
            }
        }

        // 启动 Home
        startHomeActivityLocked(currentUserId, "systemReady");

        try {
        	// 发送消息，触发处理Uid错误的Application
            if (AppGlobals.getPackageManager().hasSystemUidErrors()) {
            	...
                mUiHandler.obtainMessage(SHOW_UID_ERROR_UI_MSG).sendToTarget();
            }
        } catch (RemoteException e) {
        }

        // 发送一些广播信息
        ...

        // 这里暂不深入，属于Activity的启动过程，onResume
        mStackSupervisor.resumeFocusedStackTopActivityLocked();

        ...	
    }
}
```

这部分代码的主要工作：
1. 通知所有的系统服务执行onStartUser()
2. 启动persistent=1的Application所在的进程
3. 启动Home
4. 执行Activity的onResume


调用回调接口

回调函数定义在ServerServer#startOtherServices中，其中会调用大量服务的onBootPhase函数、一些对象的systemReady函数或systemRunning函数。

```
# SystemServer#startOtherServices

mActivityManagerService.systemReady(() -> {

	// 遍历系统服务startBootPhase启动阶段代码
    mSystemServiceManager.startBootPhase(
            SystemService.PHASE_ACTIVITY_MANAGER_READY);


    try {
    	// AMS 启动 NativeCrashListener，监听“/data/system/ndebugsocket”中的信息
    	// 实际上就是监听 debuggerd(调试工具) 传入的信息
        mActivityManagerService.startObservingNativeCrashes();
    } catch (Throwable e) {
        reportWtf("observing native crashes", e);
    }


    // No dependency on Webview preparation in system server. But this should
    // be completed before allowring 3rd party
    final String WEBVIEW_PREPARATION = "WebViewFactoryPreparation";
    Future<?> webviewPrep = null;
    if (!mOnlyCore) {
        webviewPrep = SystemServerInitThreadPool.get().submit(() -> {
            Slog.i(TAG, WEBVIEW_PREPARATION);
            TimingsTraceLog traceLog = new TimingsTraceLog(
                    SYSTEM_SERVER_TIMING_ASYNC_TAG, Trace.TRACE_TAG_SYSTEM_SERVER);
            traceLog.traceBegin(WEBVIEW_PREPARATION);
            ConcurrentUtils.waitForFutureNoInterrupt(mZygotePreload, "Zygote preload");
            mZygotePreload = null;
            mWebViewUpdateService.prepareWebViewInSystemServer();
            traceLog.traceEnd();
        }, WEBVIEW_PREPARATION);
    }

    if (mPackageManager.hasSystemFeature(PackageManager.FEATURE_AUTOMOTIVE)) {
        traceBeginAndSlog("StartCarServiceHelperService");
        mSystemServiceManager.startService(CarServiceHelperService.class);
        traceEnd();
    }

    // 启动 SystemUi
    try {
        startSystemUi(context, windowManagerF);
    } catch (Throwable e) {
        reportWtf("starting System UI", e);
    }
    
    // 一系列 systemReady
    try {
        if (networkScoreF != null) networkScoreF.systemReady();
    } catch (Throwable e) {
        reportWtf("making Network Score Service ready", e);
    }
    ...
    ...

   	// 启动Watchdog
    Watchdog.getInstance().start();

    // Wait for all packages to be prepared
    mPackageManagerService.waitForAppDataPrepared();

    // confirm webview completion before starting 3rd party
    if (webviewPrep != null) {
        ConcurrentUtils.waitForFutureNoInterrupt(webviewPrep, WEBVIEW_PREPARATION);
    }

    mSystemServiceManager.startBootPhase(
            SystemService.PHASE_THIRD_PARTY_APPS_CAN_START);


    // 一系列 systemRunning
    try {
        if (locationF != null) locationF.systemRunning();
    } catch (Throwable e) {
        reportWtf("Notifying Location Service running", e);
    }
    ...
    ...

}, BOOT_TIMINGS_TRACE_LOG);
```

startPersistentApps

```

private void startPersistentApps(int matchFlags) {
    ...

    synchronized (this) {
        try {
            //从PMS中得到persistent为1的ApplicationInfo
            final List<ApplicationInfo> apps = AppGlobals.getPackageManager()
                    .getPersistentApplications(STOCK_PM_FLAGS | matchFlags).getList();
            for (ApplicationInfo app : apps) {
                //由于framework-res.apk已经由系统启动，所以此处不再启动它
                if (!"android".equals(app.packageName)) {
                    //addAppLocked中将启动application所在进程
                    addAppLocked(app, false, null /* ABI override */);
                }
            }
        } catch (RemoteException ex) {
        }
    }
}
```

跟进一下 addAppLocked 函数

```
final ProcessRecord addAppLocked(ApplicationInfo info, boolean isolated,
        String abiOverride) {
    //以下是取出或构造出ApplicationInfo对应的ProcessRecord
    ProcessRecord app;
    if (!isolated) {
        app = getProcessRecordLocked(info.processName, info.uid, true);
    } else {
        app = null;
    }

    if (app == null) {
        app = newProcessRecordLocked(info, null, isolated, 0);
        updateLruProcessLocked(app, false, null);
        updateOomAdjLocked();
    }
    ...
    // This package really, really can not be stopped.
    try {
        //通过PKMS将package对应数据结构的StoppedState置为fasle
        AppGlobals.getPackageManager().setPackageStoppedState(
                info.packageName, false, UserHandle.getUserId(app.uid));
    } catch (RemoteException e) {
    } catch (IllegalArgumentException e) {
    }

    if ((info.flags & PERSISTENT_MASK) == PERSISTENT_MASK) {
        app.persistent = true;
        app.maxAdj = ProcessList.PERSISTENT_PROC_ADJ;
    }

    if (app.thread == null && mPersistentStartingProcesses.indexOf(app) < 0) {
        mPersistentStartingProcesses.add(app);
        //启动应用所在进程，将发送消息给zygote，后者fork出进程
        startProcessLocked(app, "added application", app.processName, abiOverride,
                null /* entryPoint */, null /* entryPointArgs */);
    }

    return app;
}
```

这里最终通过startProcessLocked函数，启动实际的应用进程。
Zygote进程中的server socket将接受消息，然后为应用fork出进程

启动Home Activity

```
boolean startHomeActivityLocked(int userId, String reason) {
    ...
    Intent intent = getHomeIntent();
    //根据intent中携带的ComponentName，利用PMS得到ActivityInfo
    ActivityInfo aInfo = resolveActivityInfo(intent, STOCK_PM_FLAGS, userId);
    if (aInfo != null) {
        intent.setComponent(new ComponentName(aInfo.applicationInfo.packageName, aInfo.name));
        aInfo = new ActivityInfo(aInfo);
        aInfo.applicationInfo = getAppInfoForUser(aInfo.applicationInfo, userId);

        //此时home对应进程应该还没启动，app为null
        ProcessRecord app = getProcessRecordLocked(aInfo.processName,
                aInfo.applicationInfo.uid, true);
        if (app == null || app.instrumentationClass == null) {
            intent.setFlags(intent.getFlags() | Intent.FLAG_ACTIVITY_NEW_TASK);
            //启动home
            mActivityStarter.startHomeActivityLocked(intent, aInfo, reason);
        }
    } else {
        ...
    }
    return true;
}
```

这里暂时不深究Home Activity启动的具体过程。当启动成功后，会调用Activity#handleResumeActivity方法，最终调用到 ActivityStackSupervisor#activityIdleInternalLocked。

```
final ActivityRecord activityIdleInternalLocked(final IBinder token, boolean fromTimeout,
        Configuration config) {
    ...
    if (isFocusedStack(r.task.stack) || fromTimeout) {
        booting = checkFinishBootingLocked();
    }
    ...
}
```

checkFinishBootingLocked

```
private boolean checkFinishBootingLocked() {
    //mService为AMS，mBooting变量在AMS回调SystemServer中定义的Runnable时，置为了true
    final boolean booting = mService.mBooting;
    boolean enableScreen = false;
    mService.mBooting = false;
    if (!mService.mBooted) {
        mService.mBooted = true;
        enableScreen = true;
    }
    if (booting || enableScreen) {、
        //调用AMS的接口，发送消息
        mService.postFinishBooting(booting, enableScreen);
    }
    return booting;
}
```

最终，AMS的finishBooting函数将被调用：

```
final void finishBooting() {
    ...
    //以下是注册广播接收器，用于处理需要重启的package
    IntentFilter pkgFilter = new IntentFilter();
    pkgFilter.addAction(Intent.ACTION_QUERY_PACKAGE_RESTART);
    pkgFilter.addDataScheme("package");
    mContext.registerReceiver(new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String[] pkgs = intent.getStringArrayExtra(Intent.EXTRA_PACKAGES);
            if (pkgs != null) {
                for (String pkg : pkgs) {
                    synchronized (ActivityManagerService.this) {
                        if (forceStopPackageLocked(pkg, -1, false, false, false, false, false,
                                0, "query restart")) {
                            setResultCode(Activity.RESULT_OK);
                            return;
                        }
                    }
                }
            }
       }
    }, pkgFilter);
    ...
    // Let system services know.
    mSystemServiceManager.startBootPhase(SystemService.PHASE_BOOT_COMPLETED);

    //以下是启动那些等待启动的进程
    synchronized (this) {
        // Ensure that any processes we had put on hold are now started
        // up.
        final int NP = mProcessesOnHold.size();
            if (NP > 0) {
                ArrayList<ProcessRecord> procs =
                        new ArrayList<ProcessRecord>(mProcessesOnHold);
                for (int ip=0; ip<NP; ip++) {
                    ...
                    startProcessLocked(procs.get(ip), "on-hold", null);
                }
            }
        }
    }
    ...
    if (mFactoryTest != FactoryTest.FACTORY_TEST_LOW_LEVEL) {
        // Start looking for apps that are abusing wake locks.
        //每5min检查一次系统各应用进程使用电量的情况，如果某个进程使用WakeLock的时间过长
        //AMS将关闭该进程
        Message nmsg = mHandler.obtainMessage(CHECK_EXCESSIVE_WAKE_LOCKS_MSG);
        mHandler.sendMessageDelayed(nmsg, POWER_CHECK_DELAY);

        // Tell anyone interested that we are done booting!
        SystemProperties.set("sys.boot_completed", "1");
        ...
        //此处从代码来看发送的是ACTION_LOCKED_BOOT_COMPLETED广播
        //在进行unlock相关的工作后，mUserController将调用finishUserUnlocking，发送SYSTEM_USER_UNLOCK_MSG消息给AMS
        //AMS收到消息后，调用mUserController的finishUserUnlocked函数，经过相应的处理后，
        //在mUserController的finishUserUnlockedCompleted中，最终将会发送ACTION_BOOT_COMPLETED广播
        mUserController.sendBootCompletedLocked(...);
        ...
    }
}
```

最终AMS启动 HomeActivity 结束，并发送 ACTION_BOOT_COMPLETED 广播时，AMS的启动过程告一段落。

-

总结
对于整个AMS
启动过程来说，本文涉及的内容只是其中极小一部分。
整个过程，可以将AMS的启动过程分为四步

1. 创建出 SystemServer 进程的 Android 运行环境
    这个流程，创建SystemServer进程对应的ActivityThread和ContextImpl，构成Android运行环境
    AMS后续工作依赖SystemServer在此创建出的运行环境
    
2. 完成AMS的初始化和启动
    这个流程，调用了AMS的构造函数和start函数，完成AMS一些初始化工作

3. 将SystemServer进程纳入到AMS的管理体系
    这个流程，AMS加载了SystemServer中framework-res.apk的信息，并启动和注册了SettingsProvider.apk。涉及了ContentProvider的安装。

4. AMS启动完毕后才能进行的工作
    这个流程，AMS调用 systemReady函数，通知系统的其他服务和进程，可以进行对应的工作。
    并且，Home Activity被启动了，当Home Activity被加载完成后，最终会触发ACTION_BOOT_COMPLETED广播。
    



1. 创建 `system` 进程的运行环境，一个 `ActivityThread` 主线程，一个和系统进程相关的 `Context` 对象
2. 调用 `AMS` 的构造方法，对必要的内容进行初始化
3. 将 AMS 注册到 `ServiceManager` 中，同时对 `system` 进程创建一个 `ProcessRecord` 对象，并设置 `Context` 的 `application` 为 `framework-res` 的 `application` 对象。由于 `AMS` 是 `Android` 世界的进程管理和调度中心，尽管 `system` 贵为系统进程，也要将其并入 `AMS` 的管理范围
4. 为 `system` 进程加载 `SettingProvider`
5. 调用 `systemReady` 方法做系统启动完毕前的最后一些扫尾工作。最 `HomeActivity` 将呈现在用户面前

