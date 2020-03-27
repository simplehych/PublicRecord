[TOC]

# Kotlin
 
Kotlin语言特性包括空安全，Elvis表达式，简洁字符串等等

## 1 更安全的指针操作

在Kotlin中，一切皆对象，不存在int，double等关键字，只存在Int，Double等类。

所有的对象都通过一个指针所持有，而指针只有俩种类型：var表示指针可变，val表示指针不可变。为了获得更好的空安全，Kotlin中所有的对象都明确指明可空或者非空属性，即这个对象是否可能为null

```
// 类型后面加?表示可为空
var age: String? = "23"
// 不做处理返回null
val age1 = age?.toInt()
// age为空返回-1
val age2 = age?.toInt() ?: -1;
// 抛出空指针异常
val age3 = age!!.toInt()
```

对于可空类型的对象，直接调用其方法，在编译阶段就会出错。这样就杜绝了空指针异常NullPointerException的可能性

## 2 ?表达式和Elvis表达式

Kotlin特有的?表达式和Elvis表达式可以在确保安全的情况下，写出更加简洁的代码。
比如我们在Android页面开发中常见的删除子控件操作，用Java写是这样的：

```
if(view != null) {
    if(view.getParent() != null) {
        if(view.getParent() instanceof ViewGroup){
            ((ViewGroup)view.getParent()).removeView(view);
        }
    }
}
```

为了获得更加安全的代码，我们不得不加上很多if else判断语句，来确保不会产生空指针异常。但Kotlin呢？操作符可以非常简洁地实现上述逻辑：

```
(view?.parent as? ViewGroup)?.removeView(view)
```

那么这个 `?` 表达式的内在逻辑是什么呢？以上代码为例，若 `view == null`，则后续调用均不会走到，整个表达式直接返回null，也不会抛出异常。也就是说，`?` 表达式中，只要某个操作符对象为null，则整个表达式直接返回null

除了`?`表达式，Kotlin还有个大杀器叫Elvis表达式，即`?: ` 表达式，这俩个表达式加在一起可以超简洁的形式表述一个复杂逻辑

```
val v = a?.b ?: c
```

以上面表达式为例，若前面部分为null，则整个表达式返回值等c的值，否则等于前面部分的值。把它翻译成java代码，是这样的

```
var temp = if(a!=null) a.b else null
val v = if(temp!=null) temp else c
```

同样等同于这样

```
val v = if(a == null || a.b == null) c else a.b
```

即Elvis表达式的含义在于为整个？表达式托底，即若整个表达式已经为null的情况下，Elvis表达式能够让这个表达式有个自定义的默认值。这样进一步保证了空安全，同样代码也不失简洁性。

## 3 更简洁的字符串

同Java一样，Kotlin也可以用字面量对字符串对象进行初始化，但Kotlin有个特别的地方是使用三引号来方便长篇字符串的书写。而且这种方法还不需要使用转义符。做到了字符串的所见即所得。

```
val text = """
    for(c in "foo")
        print(c)
"""
```

同时，Kotlin还引入了字符串模板，可以在字符串汇总直接访问变量和使用表达式：

```
/**
* Kotlin字符串模板，可以用￥符号拼接变量和表达式
*/
fun testString() {
   val strings = arrayListOf("abc", "efd", "gfg")
   println("First content is $strings")
   println("First content is ${strings[0]}")
   println("First content is ${if (strings.size > 0) strings[0] else "null"}")
}
```

## 4 强大的when语句

Kotlin中没有switch操作符，而是使用when语句替代，同样的，when将它的参数和所有的分支条件顺序比较，直到某个分支满足条件。如果其他分支都不满足条件将会进入else分支

```
when (x) {
  1 -> print("x==1")
  2 -> print("x==2")
  else -> {
      print("x is neither 1 nor 2")
  }
}
```

但功能上when语句要强大的多，首先第一点是，我们可以用任意表达式，而不仅仅使用常量作为分支条件，这点switch是做不到的，如下述代码：

```
when (x) {
  in 1..10 -> print("x is in the range")
  !in 10..20 -> print("x is outside the range")
  is String -> print("x is a string")
  else -> print("none of the above")
}
```

## 5 对象比较

Java的 `==` 操作符是比较引用值，但Kotlin的 `==` 操作符是比较内容，`===` 才是比较引用值。基于这点，我们可以写出逻辑更简洁合理的代码

```
when(day){
    today -> viewHolder.mDay.setText("今天")
    tomorrow -> viewHolder.mDay.setText("明天")
    else -> viewHolder.mDay.setText(day)
}
```

## 6 Nullable Receiver

NullableReceiver，可空接收者，要理解这个概念，我们先了解一下Kotlin中一个重要特性：扩展。Kotlin能够扩展一个类的新功能，这个扩展是无痕的，即我们无需继承该类或使用像装饰者的设计模式，同事这个扩展对使用者来说也是透明的，即使用者在使用该类扩展功能时，就像使用这个类自身的功能一样的

声明一个扩展函数，我们需要用一个接受者类型，也就是被扩展的类型来作为他的前缀，以下述代码为例：

```
fun <T> MutableList<T>.swap(index1: Int, index2: Int) {
   val tmp = this[index1]
   this[index1] = this[index2]
   this[index2] = tmp
}
```

上述代码为 MutableList<Int> 添加一个swap函数，我们可以对任意 MutableList<Int>调用该函数了：

```
val l = mutableListOf(1, 2, 3)
l.swap(0, 2)
```

其中MutableList<Int>就是这个扩展函数的接收者。值得注意的是，Kotlin允许这个接收者为null，这样我们可以写出一些在Java里面看似不可思议的代码。比如我们要把一个对象转换成字符串，在Kotlin中可以直接这么写：

```
var number = null
var str = number.toString()
```

上述代码先定义了一个空指针对象，然后调用toString方法，会不会Crash？其实不会发生Crash，答案就再可空接收者，也就是Nullable Receiver，我们看下这个扩展函数的定义：

```
public fun Any?.toString(): String
```

扩展函数是可以拿到接收者对象的指针的，即this指针。从这个方法的定义我们可以看到，这个方法是对Any类进行扩展，而接受者类型后面加了个 `？`，所以真确来说，是对Any?类进行扩展。我们看到，扩展函数一开始就对接收者判空，若为null，则直接返回“null”字符串。所以无论对于什么对象，调用toString方法不会发生Crash

## 7 关键字object

前面说过，Kotlin中一切皆对象，obje在Kotlin中是一个关键字，笼统来说是代表“对象”，在不同场景中有不同用法。

第一个是对象表达式，可以直接创建一个继承自某个(或某些)类型的匿名类的对象，而无须先创建这个对象的类。这一点跟Java是类似的：

```
window.addMouseListener(object : MouseAdapter() {
    override fun mouseClicked(e: MouseEvent){}
    override fun mouseEntered(e: MouseEvent){}
})
```

第二，对象字面量。这个特殊性将数字字面量，字符串字面量扩展到一般性对象中了。对应的场景是如果我们只需要“一个对象而已”，并不需要特殊类型。典型的场景是在某些地方，比如函数内部，我们需要零碎地使用一些一次性的对象时，非常有用。

```
fun foo() {
   val adHoc = object {
       var x: Int = 0
       var y: Int = 0
##    }
   print(adHoc.x + adHoc.y)
}
```

第三，对象声明，这个特性类似于Java中的单例模式，但我们不需要写单例模式的样板代码即可实现。

```
object DefaultListener : MouseAdapter(){
    override fun mouseClicked(e: MouseEvent){}
    override fun mouseClicked(e: MouseEvent){}
}
```

请注意上述代码是声明了一个对象，而不是类，而我们想要使用这个对象，直接引用其名称即可

```
DataProviderManager.registerProvider(...)
```

## 8 有趣的冒号

从语法上来看Kotlin大量使用了冒号，考虑下面四种场景：

* 在变量定义中，代表变量的类型
* 在类定义中，代表基类的类型
* 在函数定义中，代表函数返回值的类型
* 在匿名对象中，代表对象的类型

笼统来说，Kotlin的设计者应该就是想用冒号来笼统表示类型这一概念


## 9 可观察性

可观察性，本质就是观察者模式，在java中也可以实现这个模式，但Kotlin实现观察者模式不需要样板代码。在谈Kotlin的可观察属性前，先看下Kotlin里面的委托。同样的，委托也是一种设计模式。

Kotlin在语言级别支持它，不需要任何样板代码。Kotlin可以使用by关键字把子类的所有公有成员都委托给指定对象来实现基类的接口：

```
interface Base {
    fun print()
}

class BaseImpl(val x: Int) : Base {
    override fun print() {
        print(x)
    }
}

class Derived(base: Base) : Base by base
```

上述代码中，Base是一个接口，BaseImpl是它的一个实现类，通过by base语句就可以把Derive的类中所有公有成员全部委托给base对象来实现。我们在创建Derived类时，在构造器中直接传入一个BaseImpl的实例，那么调用Derived的方法等同于调用BaseImpl的实例的方法，访问Derived的属性也同访问BaseImpl的实例的属性。

回到可观察属性这个概念，Kotlin通过 Delegates.observable()实现可观察属性：

```
var name: String by Delegates.observable("wang", { property: KProperty<*>, oldValue: String, newValue: String ->
   println("kProperty: ${property.name} | oldname:${oldValue} | newName:$newValue")
})

fun main(args: Array<String>) {
   println("name: $name")

   name = "zhang"
   name = "li"

}
```

上述代码中，那么是一个属性，改变它的值都会自动回调{property，oldValue，newValue ->}这个lambda表达式。简单来说，我们可以监听name这个属性的变化。

可观察属性有什么用处呢？ListView中有一个经典的Crash：在数据长度与Adapter中的Cell的长度不一致时，会报IllegalStateException异常。这个异常的根本原因是修改了数据之后，没有调用notifyDataSetChanged，导致ListView没有及时刷新，如果我们把数据做成可观察属性，在观察回调方法中直接刷新ListView，可以杜绝这个问题。

## 10 函数类型

Kotlin中一切皆为对象，函数也不例外。在Kotlin中，函数本身也是对象，可以拥有类型并实例化。Kotlin使用类似（Int）-> String 的一系列函数类型来处理函数的声明，比如我们常见的点击回调函数

```
val onClick: (View) -> Unit = {}
```

箭头表示法是右结合的，`(Int) -> (Int) -> Unit` 等价于 `(Int) -> ((Int) -> Unit)`，但不等于 `((Int) -> (Int)) -> Unit`。 可以通过使用类型别名给函数类型起一个别称：

```
typealias ClickHandler = (View) -> Unit
```

函数对象最大的作用是可以轻易地实现回调，而不需要想Java那样通过代理类才可以做到。

Kotlin实现回调就是完全不一样的方式了，因为Kotlin的函数也是对象，所以我们直接把函数对象传递给主调方即可，如下：

```
class MyScrollView {
    var onScrollListener: (Int, Int) -> Unit = { _, _ -> }
}

scrollView.onScrollListener = { x, y ->
}
```

再介绍如何将函数类型实例化，有几种常见方式：

* 使用函数字面值的代码块，比如lambda表达式 `{a, b -> a + b}`，或者匿名函数 `fun(s: String): Int{ return s.toIntOrNull() ?: 0}`
* 使用已有声明的可调用引用，包括顶层、局部、成员、扩展函数 ::isOdd String::toInt，或者顶层、成员、扩展属性List<Int>::size，或者是构造函数::Regex
* 使用实现函数类型接口的自定义类的实例

    ```
    class IntTransformer: (Int) ->{
        override operator fun invoke(p1: Int): Int = TODO()
    }
    
    val intFuntion:(Int) -> Int = IntTransformer()
    ```

* 编译器推断

    ```
    val a = { i: Int -> i + 1} //推断出类型是(Int) -> Int
    ```


