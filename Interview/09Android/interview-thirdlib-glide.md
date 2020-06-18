**Glide 是一款 Android 图片加载框架。**

本文基于 `Glide 4.11.0` 展开说明


## 关键字

线程池、LruCache、DiskLruCache、LinkedHashMap、OOM

## 1. Glide 的使用

### 1.1 常规使用

```
RequestOptions requestOptions = new RequestOptions()
        .transform(new CircleCrop()); // 图片转换，圆角/模糊等，可模仿自定义 transform， implementation 'jp.wasabeef:glide-transformations:3.0.1'
        .placeholder(R.drawable.ic_launcher_background)
        .error(R.drawable.ic_launcher_foreground)
        .override(Target.SIZE_ORIGINAL) // 设置显示大小
        .skipMemoryCache(false) // 设置内存缓存
        .diskCacheStrategy(DiskCacheStrategy.ALL); // 设置磁盘缓存

Glide.with(this)
        .asDrawable() // asBitmap() / asGif() / asFile()
        .load(url) // load(new GlideUrl(url)) / load(file) / load(bitmap) / load(drawable) 
        .apply(requestOptions)
        .into(imageView);
```

### 1.2 监听回调使用

```
// 1. 仅预加载不显示，目的是缓存
Glide.with(this)
        .load(url)
        .preload(); 

// 2. into 自定义Target，自己进行图片显示操作
Glide.with(this)
        .load(url)
        .into(new CustomTarget<Drawable>() {
            @Override
            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                imageView.setImageDrawable(resource);
            }
            ...
        });

// 3. listener 回调监听
Glide.with(this)
        .load(url)
        .listener(new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                return true; // true 表拦截
            }
        })
        .into(imageView);
        
// 4. submit 属于阻塞操作
Runnable runnable = new Runnable() {
    @Override
    public void run() {
        FutureTarget<File> submit = Glide.with(Util.getAppContext())
                .asFile()
                .load(url)
                .submit();
        try {
            File file = submit.get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }
};
Thread thread = new Thread(runnable);
thread.start();
```

### 1.3 配置使用

1. 对 Glide 进行配置
2. 注册组件，如切换网络请求模块为 OkHttp
3. 通过使用 Generated API 功能可恢复 Glide 3.0 使用方式，可自查。（@GlideExtension / @GlideOption）

```
@GlideModule
public class MyGlideModule extends AppGlideModule {
    public static final int DISK_CACHE_SIZE = 500 * 1024 * 1024;

    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
        builder
                .setMemoryCache(new LruResourceCache(1024)) // 设置内存缓存大小
                .setDiskCache(new ExternalPreferredCacheDiskCacheFactory(context, DISK_CACHE_SIZE)) // 设置硬盘缓存大小
                .setDiskCacheExecutor(GlideExecutor.newDiskCacheExecutor()) // 设置硬盘存储位置
                .setDefaultRequestOptions(new Glide.RequestOptionsFactory() {
                    @NonNull
                    @Override
                    public RequestOptions build() {
                        return new RequestOptions().format(DecodeFormat.PREFER_RGB_565); // 设置图片存储格式
                    }
                });
    }

    @Override
    public void registerComponents(Context context, Glide glide, Registry registry) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.addInterceptor(new ProgressInterceptor());
        OkHttpClient okHttpClient = builder.build();
        // 更换网络请求组件
        registry.replace(GlideUrl.class, InputStream.class,
                new OkHttpGlideUrlLoader.Factory(okHttpClient));
    }
}
```

## 2 图片库框架对比

**Glide特点：**

1. 拥有生命周期（采用提供隐藏Fragment方式）
    1. 注意：非主线程都采用 ApplicationContext
2. 高效处理Bitmap（Bitmap的复用和主动回收，减少系统回收压力）
3. 缓存策略
    1. 多种尺寸
    2. 多种格式（WebP、Gif、Video）
    3. 三级缓存（弱引用，内存，磁盘）

**Fresco特点：**
 
1. 最大优势在5.0以下系统，图片会存放在一个特别的内存区域（Ashmem区，共享内存区属于native堆）
2. Native层对OOM进行处理，不占用App内存
3. 适用于加载大量图片场景

Android 图片存储位置

## 2 手写图片加载框架涉

结合 Glide 实现分析

1. 异步加载：线程池
2. 线程切换：Handler
3. 缓存功能：LruCache、DiskLruCache
4. 防止OOM：软引用、LruCache、图片压缩、Bitmap像素存储位置（native/java）
5. 内存泄漏：ImageView正确引用、生命周期管理
6. 列表错位：加载错乱、队满任务过多问题

### 2.1 异步加载

线程池，多少个？

基本：读网络一个，读硬盘一个。

```
public final class GlideBuilder {
  ...
  private GlideExecutor sourceExecutor; //加载源文件的线程池，包括网络加载
  private GlideExecutor diskCacheExecutor; //加载硬盘缓存的线程池
  ...
  private GlideExecutor animationExecutor; //动画线程池
```

Glide 使用了三个线程池：网络、硬盘、动画。

网络请求会阻塞，所以单独采用一个线程池。可以替换OkHttp内置线程池。

硬盘加载采用一个线程池。

### 2.2 切换线程 Handler

异步加载成功，主线程更新 ImageView。

RxJava、EventBus、Glide等切换线程都采用 Handler 方式。

```
.into(
   glideContext.buildImageViewTarget(view, transcodeClass),
   /*targetListener=*/ null,
   requestOptions,
   Executors.mainThreadExecutor());

public final class Executors {
  ...
  private static final Executor MAIN_THREAD_EXECUTOR =
      new Executor() {
        private final Handler handler = new Handler(Looper.getMainLooper());

        @Override
        public void execute(@NonNull Runnable command) {
          handler.post(command);
        }
      };
  ...
  /** Posts executions to the main thread. */
  public static Executor mainThreadExecutor() {
    return MAIN_THREAD_EXECUTOR;
  }
```

### 2.3 缓存

图片的三级缓存：内存缓存、硬盘缓存、网络缓存。

#### 2.3.1 内存缓存 LruCache

一般采用 LruCache，Glide 还结合了一种弱引用机制，共同完成内存缓存功能。

```
public class Engine ... {
  ...
  private final ActiveResources activeResources;
  ...
  @Nullable
  private EngineResource<?> loadFromActiveResources(Key key) {
    EngineResource<?> active = activeResources.get(key);
    if (active != null) {
      active.acquire();
    }
    return active;
  }

  private EngineResource<?> loadFromCache(Key key) {
    EngineResource<?> cached = getEngineResourceFromCache(key);
    if (cached != null) {
      cached.acquire();
      activeResources.activate(key, cached);
    }
    return cached;
  }
  ...
```

Glide 自己实现了 LruCache，没有采用 `java.util.LruCache`

```
public class LruCache<T, Y> {
  // initialCapacity为100，java.util.LruCache为0，HashMap默认大小为16
  private final Map<T, Y> cache = new LinkedHashMap<>(100, 0.75f, true);
  ...
```

**补充：LruCache 源码**

LruCache 内部采用 LinkedHashMap，accessOrder 参数传true，表示按照访问顺序排序。

LinkedHashMap 继承 HashMap 数组+链表（红黑树）的结构

HashMap 的 `newNode` 返回 HashMap.Node
LinkedHashMap 重写了 `newNode()` 返回 LinkedHashMapEntry (继承 HashMap.Node，并添加了 before/after)

超过一定大小，则移除最老的数据 `trimToSize(maxSize)`

#### 2.3.2 磁盘缓存 DiskLruCache

DiskLruCache 跟 LruCache 实现思路是差不多的，一样是设置一个总大小，每次往硬盘写文件，总大小超过阈值，就会将旧的文件删除。

同样是利用LinkHashMap的特点，只不过数组里面存的 Entry 有点变化，**Editor** 用于操作文件。


### 2.4 防止OOM

Bitmap 之所以占用内存，不是因为对象大，而是因为 Bitmap 的像素数据。

> Bitmap 的像素数据大小 = 宽 * 高 * 1像素占用的内存

**宽 / 高：** 一般字缩放后的宽高。（依赖 BitmapFactory.Options# inJustDecodeBounds的值，若 true 为原图宽高 ）
**1像素占用内存：** RGB_565格式占用 2 byte；ARGB_8888格式占用 4 byte；

1. 软引用
    1. 强
    2. 软 OOM 才回收
    3. 弱 发生 GC 即回收
    4. 虚
2. onTrimMemory / onLowMemory

```
// Glide
  @Override
  public void onLowMemory() {
    clearMemory();
  }
  
  public void clearMemory() {
    // Engine asserts this anyway when removing resources, fail faster and consistently
    Util.assertMainThread();
    // memory cache needs to be cleared before bitmap pool to clear re-pooled Bitmaps too. See #687.
    memoryCache.clearMemory();
    bitmapPool.clearMemory();
    arrayPool.clearMemory();
  }
```

3. 像素存储位置
    1. 系统为每个进程，即每个虚拟机分配的内存是有限的
    2. 虚拟机内存主要划分 5 部分：虚拟机栈、本地方法栈、程序计数器、方法区、堆（是JVM最大的一块内存，OOM发生地）
    3. 像素数据 8.0 以后在 naitve 堆，8.0以下在 java 堆
    4. finalize 6.0以前 Bitmap 释放 native 层对象


### 2.5 内存泄漏

1. 采用弱引用，简单粗暴
2. 生命周期管理，ImageView回收 / 加载任务取消 / 未执行队列移除

```
// Glide的RequestManager
  @Override
  public synchronized void onDestroy() {
    targetTracker.onDestroy();
    for (Target<?> target : targetTracker.getAll()) {
      clear(target);
    }
    targetTracker.clear();
    requestTracker.clearRequests();
    lifecycle.removeListener(this);
    lifecycle.removeListener(connectivityMonitor);
    mainHandler.removeCallbacks(addSelfToLifecycle);
    glide.unregisterRequestManager(this);
  }
```

### 2.6 列表加载问题

1. 图片错乱
    1. ImageView 设置tag（一般是图片url），更新ImageView时判断tag和url是否一致
    2. item列表消失，取消对应图片加载任务（考虑在图片框架做 还是 app处理）
2. 线程池任务过多
    1. 列表滑动，第一次没有缓存会有大量请求，判断队列中是否有该任务，存在则不添加队列。

## 5 参考资料
[Glide 最全解析 ｜ 自：郭霖](https://blog.csdn.net/sinyu890807/category_9268670.html)
[面试官：简历上最好不要写Glide，不是问源码那么简单 | 自：蓝师傅_Android](https://www.jianshu.com/p/1ab5597af607)



