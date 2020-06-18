## 关键字
Annotation
DI（Dependency Injection）
IOC
AOP
apt
目标类

## 日常目标类组合方式

1. 构造方法 new（硬初始化）
2. get 方法传入（工厂模式）
3. set 方法传入
4. 接口调用
5. 注解

场景：
1. MVP 模式下，Activity 需要持有 Presenter 的引用，Presenter 又需要依赖 View 接口，耦合在一起

## 作用

1. 解耦，方便测试
2. 更好的管理类实例。1. 管理全局类实例；2. 管理页面类实例；
3. 省去重复的工作。1. 实例；2.单例，不用关心饿汉还是懒汉；

## 使用

核心：@Inject、@Component、@Module/@Providers
辅助：@Qualifier、@Scope、@Singleton

方式一：@Inject - @Component - @Inject（注解标注）优先级低
> @Inject 使用时不能用 private 修饰成员属性
标记属性 和 标记构造方法

方式二：@Inject - @Component - @Module/@Providers（工厂模式）优先级高
@Module 提供给无构造方法的类，不能用 Inject，如第三方库/系统类/**接口**
@Providers

划分 Component：
1. 全局 ApplicationComponent
2. 页面 Component
注意粒度不能太小

@Qualifier 限定符，解决依赖注入迷失问题（构造方法重载情况）使用方式？
@SubComponent
@Scope
@Singleton 是 @Scope 的一种实现
1. 没有创建单例的能力
2. 作用：更好的管理 ApplicationComponent 和 Module 关系，保证匹配，否则编译报错；代码可读性；

组织 Component：类实例共享
1. 依赖方式，dependencies 属性
2. 包含方式，SubComponent
3. 继承方式，抽象到父类中

@Scope 的真正用处在于 Component 的组织，Component 之间和 Component 和 Module 之间，不一样就会报错


注意点：
1. @Scope 非必须
2. Component 可以没有 Module
3. 多个页面可以共享一个 Component
4. 一个页面必须要有一个 Component 管理整个全局类实例


生成代码分析：
1. Provider 类型


## 参考资料

[Android：dagger2让你爱不释手 系列](https://www.jianshu.com/p/cd2c1c9f68d4)
[Dagger2 从入门到放弃再到恍然大悟](https://www.jianshu.com/p/39d1df6c877d)