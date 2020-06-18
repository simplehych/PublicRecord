OkHttp 是一个网络请求框架
Retrofit 是采用注解的形式对 OkHttp 的封装

## 关键字

*OkHttp、Retrofit、Okio
同步、异步、阻塞、非阻塞、BIO、NIO、AIO
拦截器
SPDY、连接池、Gzip、Http*

---

目录

1. 基本网络框架
2. OkHttp
3. Retrofit
4. BIO / NIO / AIO / Okio

---

## 1. 基本网络框架

一个完整的网络请求流程

![](/Users/hych/Desktop/截屏2020-04-27 15.33.45.png)

1. 构建 request 请求
2. 切换子线程请求 Http，Executor或线程
3. enqueue 入队列请求
4. 请求服务器数据完成之后进行解析 json -> 对象
5. 切回主线程，回调上层

## 2. OkHttp

**KEY：** *execute、enqueue、RealCall、Interceptor*

### 2.1 总述

OkHttp 有两种请求方式**同步**和**异步**
真正的执行者是**RealCall**
通过 **拦截链的设计** （拦截及回传），优先 **自定义** 的拦截器（eg：Log、Header），之后是 **默认** 有 **5** 个拦截器。

5个默认拦截器依次为：

1. **RetryAndFollowUpInterceptor** 重试/重定向拦截器
2. **BridgeInterceptor** 桥拦截器：添加移除Header
3. **CacheInterceptor** 缓存拦截器
4. **ConnectInterceptor** 连接池拦截器，连接池复用
5. **CallServerInterceptor** 真正请求网络拦截器


### 2.2 同步 / 异步请求

同步方法：execute
异步方法：enqueue

```
OkHttpClient okHttpClient = new OkHttpClient.Builder().build();

Request request = new Request.Builder().url("http://").build();

// 同步请求方式
Response response1 = okHttpClient.newCall(request).execute();

// 异步请求方式
okHttpClient.newCall(request).enqueue(new Callback() {
    @Override
    public void onFailure(@NotNull Call call, @NotNull IOException e) {
    }

    @Override
    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
    }
});
```

### 2.3 拦截链模式

RealCall 请求真正的执行者

```
override fun newCall(request: Request): Call = RealCall(this, request, forWebSocket = false)
```

execute() / enqueue() 最终都会调用 `getResponseWithInterceptorChain()` 方法

```
@Throws(IOException::class)
internal fun getResponseWithInterceptorChain(): Response {
    // Build a full stack of interceptors.
    val interceptors = mutableListOf<Interceptor>()
    interceptors += client.interceptors
    interceptors += RetryAndFollowUpInterceptor(client)
    interceptors += BridgeInterceptor(client.cookieJar)
    interceptors += CacheInterceptor(client.cache)
    interceptors += ConnectInterceptor
    if (!forWebSocket) {
      interceptors += client.networkInterceptors
    }
    interceptors += CallServerInterceptor(forWebSocket)

    val chain = RealInterceptorChain(
        call = this,
        interceptors = interceptors,
        ...
    )
    ...
    val response = chain.proceed(originalRequest)
    ...
    return response
    ...
}
```

1. **RealInterceptorChain** 负责管理拦截链的类
2. 每个拦截器调用 ` chain.process(request)`，就会走到下一个拦截器的 `intercept` 方法
3. 最后一个拦截器是 `CallServerInterceptor`，用Okio 请求网络返回 Response
4. 然后 response 进行回传，`response = chain.proceed(request);`

通过拦截链的设计，每个拦截器各司其职，我们可以自定义拦截器。

### 2.4 基本使用

#### 2.4.1 GET
    
1. 同步 **execute**
2. 异步 **enqueue**

#### 2.4.2 POST

1. 提交 **String** RequestBody
2. 提交 **流** RequestBody
3. 提交 **文件** RequestBody
4. 提交 **表单** FormBody
5. 提交 **分块请求** MultipartBody（ 复杂的请求体，与HTML文件上传形式兼容）

#### 2.4.3 Header
    
1. 自定义拦截器添加
2. 每个请求单独添加（区分 header / addHeader）
    
   ```
   Request request = new Request.Builder()
       .url("https://api.github.com/repos/square/okhttp/issues")
       .header("User-Agent", "OkHttp Headers.java")
       .addHeader("Accept", "application/json; q=0.5")
       .addHeader("Accept", "application/vnd.github.v3+json")
       .build();
   ```
    
   

#### 2.4.4 响应缓存

配置请求缓存策略：
    
```
CacheControl cacheControl = new CacheControl.Builder()
       .maxAge(10, TimeUnit.MILLISECONDS)
       .maxStale(10, TimeUnit.MILLISECONDS)
       .onlyIfCached()
       .noCache()
       .build();
new Request.Builder()
       .cacheControl(cacheControl)
       .build();
```
    
    
配置单个请求缓存设置：私有缓存目录 和 缓存大小的限制
    
```
newClient = okHttpClient.newBuilder() // 单个 OkHttp 配置
       .cache(new Cache(new File(""), 100 * 1024 * 1024L))
       .connectTimeout(10, TimeUnit.SECONDS)
       .readTimeout(30, TimeUnit.SECONDS)
       .writeTimeout(10, TimeUnit.SECONDS)
       .addInterceptor(new Interceptor() {
           @NotNull
           @Override
           public Response intercept(@NotNull Chain chain) throws IOException {
               return null;
           }
       })
       .build();
```


#### 2.4.5 取消一个Call

1. 取消单个：

    ```
    Call call = okHttpClient.newCall(request);
    call.cancel();
    ```

2. 取消全部：`dispatcher.cancelAll();`（待验证）

#### 2.4.6 处理验证

1. HTTP AUTH
    
    1. 配置了 http auth
    2. 检查 header 有没有 "Authorization"
    3. 有则检验，没有则401
    
2. OkHttp 认证
    
    自动重试未验证的请求 `401 Not Authorized`
        
    ```
    Authenticator authenticator = new Authenticator() {
       @Nullable
       @Override
       public Request authenticate(@Nullable Route route, @NotNull Response response) throws IOException {
        
           String credential = Credentials.basic("user", "pwd");
        
           if (credential.equals(response.request().header("Authorization"))) {
               return null; // If we already failed with these credentials, don't retry.
           }
        
           return response.request().newBuilder()
                   .header("Authorization", credential)
                   .build();
       }
    };
        
    new OkHttpClient.Builder()
           .authenticator(authenticator)
           .build();
    ```

### 2.5 优缺点

优点：

1. 异步
2. 进度条回调
3. 支持 session 保持
4. 支持取消请求
5. 支持SPDY（多个连接共享一个socket，Http协议的增强）、连接池（复用）、Gzip（减少传输内容大小）、缓存（避免重复请求）

缺点：
    缓存失效问题，解决：过滤可变参数；手动保存；


参考：
[（面试必备-源码系列）OkHttp3](https://www.jianshu.com/p/7c2bb28ccac4)
[OkHttp使用完全教程](https://www.jianshu.com/p/ca8a982a116b)

## 3. Retrofit


### 3.1 总述

Retrofit 底层网络框架默认是 OkHttp
Retrofit 适合 RESTful 格式的请求
与其他网络框架的不同：更多使用运行时注解的方式提供功能

### 3.2 使用步骤

1. 创建Retrofit实例

    ```
    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("http://localhost:4567/")
            .build();
    ```

    > 注意：baseUrl 必须以 "/" 结束
    
2. 定义Api接口

    ```
    public interface BlogService {
        @GET("blog/{id}")
        Call<ResponseBody> getBlog(@Path("id") int id);
    }
    ```

3. 请求调用

    ```
    BlogService service = retrofit.create(BlogService.class);
    // 和 OkHttp 一致
    service.getBlog(2).execute();
    ```

### 3.3 Retrofit 22 个注解

22个注解分三类：请求方式、标记类、参数类

#### 3.3.1 HTTP请求方式 8个
   
**7个：**
   GET、POST、PUT、DELETE、PATCH、OPTION；
   
**1个：**
   HTTP(method=;path=;hasBody=)；  可以替换上面7个方法

#### 3.3.2 标记类 3个

**表单请求：**
FormUrlEncoded、Multipart
    
**标记：**
Streaming

#### 3.3.3 参数类 11个

**作用于方法：**
Headers
    
**作用于方法参数：**
Header
Body
Filed / FiledMap
Part / PartMap
Path
Query / QueryMap
Url

参考：
[你真的会用Retrofit2吗?Retrofit2完全教程](https://www.jianshu.com/p/308f3c54abdd)

### 3.4 优缺点

优点：

1. 使用注解超级解偶
2. 配置不同的网络框架，OkHttp/HttpClient
3. 配置不同 CallAdapter
4. 配置不同的转换器解析工具

缺点：

1. 不能接触序列化实体和响应数据
2. 使用转换器低效
3. 只能支持自定义参数类型

参考：
[OkHttp和Retrofit](https://www.jianshu.com/p/4d67fe493ebf)

### 3.5 Retrofit 设计模式

1. 外观模式（门面模式）
    
    外部方便使用，内部闭门造车
    
    eg: retrofit.create() / Glide.with()
  
2. 装饰模式  
    
    Decorator，实际上叫 Wrapper/Source 更直观些
    
    和静态代理很像，同 Proxy/Delegate，proxy不提供delegate没有的public方法，以免被认出来
    
    使用的目的：希望在 Source 操作时执行一些额外的操作，这里是线程切换到主线程

    ```
    static final class ExecutorCallbackCall<T> implements Call<T> {
        final Executor callbackExecutor;
        final Call<T> delegate;
    
        ExecutorCallbackCall(Executor callbackExecutor, Call<T> delegate) {
          this.callbackExecutor = callbackExecutor;
          this.delegate = delegate;
        }
    
        @Override public void enqueue(final Callback<T> callback) {
          delegate.enqueue(new Callback<T>() {...}
        }
        ...
    ```

3. 动态代理

    API：Proxy / InvocationHandler，依赖接口实现
    
    和上面的静态代理使用场景类似，都做额外的操作。
    但，静态代理的不足是需要额外写很多代码
    所以，引入动态代理设置 delegate

    ```
public <T> T create(final Class<T> service) {
    validateServiceInterface(service);
    return (T) Proxy.newProxyInstance(service.getClassLoader(), new Class<?>[] { service },
        new InvocationHandler() {
          ...
          @Override public @Nullable Object invoke(Object proxy, Method method,
            ...
            return loadServiceMethod(method).invoke(args != null ? args : emptyArgs);
          }
        });
  }
    ```
    
    Android AOP：Aspect Oriented Programming，通过预编译方式和运行期动态代理实现程序功能的统一维护的一种技术，实现业务解偶
    

4. 适配器模式
  
  Retrofit 底层使用 OkHttp 进行网络请求最终返回 `OkHttpCall` implement `Call`，要被不同的标准平台调用，比如 rxjava、java8、guava等。
  
  于是为了适配兼容支持各平台，设计了接口  `CallAdapter<R, T>`
    
    ```
    Retrofit retrofit = new Retrofit.Builder()
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        ...
        .build()
    ```
    
    ```
    public interface CallAdapter<R, T> {
      Type responseType();
      T adapt(Call<R> call);
    
      abstract class Factory {
    
        public abstract @Nullable CallAdapter<?, ?> get(Type returnType, Annotation[] annotations,
            Retrofit retrofit);
    
        protected static Type getParameterUpperBound(int index, ParameterizedType type) {
          return Utils.getParameterUpperBound(index, type);
        }
    
        protected static Class<?> getRawType(Type type) {
          return Utils.getRawType(type);
        }
      }
    }
    ```

    其他：RecyclerView.Adapter


5. Builder模式

    ```
     Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://api.github.com")
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
    ```

6. 工厂模式

    ```
    public interface CallAdapter<R, T> {
    
      Type responseType();
    
      T adapt(Call<R> call);
    
      abstract class Factory {
    
        public abstract @Nullable CallAdapter<?, ?> get(Type returnType, Annotation[] annotations,
            Retrofit retrofit);
    
        protected static Type getParameterUpperBound(int index, ParameterizedType type) {
          return Utils.getParameterUpperBound(index, type);
        }
    
        protected static Class<?> getRawType(Type type) {
          return Utils.getRawType(type);
        }
      }
    }
    ```

参考：
[Retrofit分析-漂亮的解耦套路](https://www.jianshu.com/p/45cb536be2f4)

## 3. BIO / NIO / AIO / Okio

**KEY：** *同步/异步、阻塞/非阻塞、多路复用、BIO、NIO、AIO*


### 对比

名称 | 模式 | 接口 | 备注
--- |  --- | --- | ---
BIO <br/> Blocking I/O <br/> 传统IO  | 同步阻塞I/O模式 | InputStream <br/> OutputStream <br/> Reader <br/> Writer | 装饰器模式
NIO <br/> New I/O <br/> No-Blocking I/O | 同步非阻塞I/O模式 | Buffer <br/> Channel <br/> Selector | jdk1.4引入 实现多路复用模型
AIO <br/> Asynchronous I/O | 异步非阻塞I/O模式 | AsynchronousChannel <br/> CompletionHandler
Okio | 对Java原生流（而不是Channel）做了封装<br/>设计了一套非阻塞调用的机制（看门狗） | Sink <br/> Source <br/> Segment <br/> SegmentPool | 享元模式-池技术

参考：
[用轻和快定义优雅，Okio框架解析](https://www.jianshu.com/p/ea3ef6d7f01b)
[Java NIO Tutorial](http://tutorials.jenkov.com/java-nio/index.html)
[Java核心（五）深入理解BIO、NIO、AIO](https://zhuanlan.zhihu.com/p/51453522)
[10个最高频的Java NIO面试题剖析！](https://mp.weixin.qq.com/s/YIcXaH7AWLJbPjnTUwnlyQ)
[漫画：一文学会面试中常问的 IO 问题！](https://mp.weixin.qq.com/s/Gq8ABlEVolLW3wJ7vsSSFw)

## 参考资料

如文中列出


