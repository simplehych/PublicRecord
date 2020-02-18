### 2020-1-1 January Wednesday

* Shell 十三问
    * 别人 echo 你也 echo；试问echo知多少
    * 双引号和单引号的区别
    * var=value与export前后的差在哪
    * exec跟source差在哪
    * () 与 {} 的差别在哪
    * 特殊符号差异
* 英语
    * distraction
    * form feed
    * trim
    * substitution  代替
    * export


### 2020-1–2 January Thursday

* 国新发布
    * 应用升级
        * 存储文件地址 - 内部地址不用读写权限，外部地址需要申请读写权限
        * setDataAndType 需要 Uri 权限 -  addFlag(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
    * 拍照 裁剪
        * “com.android.camera.action.CROP" 裁剪代码不同 
    * Uri适配方式
        * 使用FileProvider extens ContentProvider，file:///Uri不给用，那么换个Uri为 content:// 来替代
        *  Uri 权限有两种方式，原因 安全问题
            * 1. context.grantUriPermission授权/revokeUriPermission撤销授权
            * 2. addFlags(FLAG_GRANT_WRITE_URI_PERMISSION|FLAG_GRANT_READ_URI_PERMISSION)，主要用于setData，setDataAndType以及setClipData
* 英语
    * revoke
    * invoke


### 2020-1-3 January Friday

* Shell 十三问
    * 大于号和小于号的区别
        * 文件描述符
            * fd，File Description
                * stdin 0 <
                * stdout 1 >
                * stderr 2 >
        * I/O Redirection
            * 输入重定向 n<   <<
            * 输出重定向 n>  eg: 1>&2（>&2）
            * 空，/dev/null
* 国新发布，留言板接口调试
* 英语
    * File handle 文件句柄
    * File Description 文件描述符
    * I/O Redirector I/O重定向
    * exceed 超过


### 2020-1-4 January Saturday

* play basketball

### 2020-1-5 January Sunday

* English / Summary 转移至 github


---

### 2020-1-6 January Monday

* Shell 入门指导文档编写
* company
    * 留言板需求评审
    * 国新发布版本升级
    * 年终总结汇报


### 2020-1-7 January Tuesday

* company
    * 留言板隐私政策上线
    * 留言板分享图片

* Git
    * 变基
        * 三方合并 merge / 变基 rebase
        * git rebase master; git checkout master; git merge feature_one;
        * git rebase --onto master feature_one feature_two;
        * git pull --rebase
        * 合并和变基的区别
     
### 2020-1-8 January Wednesday

* company
    * 领导留言板的设计评审，接口梳理
    * 话题征集页面设计

### 2020-1-9 January Thursday    

* company
    * 领导留言板
        * 留言话题征集页面
        * 测量View Measure.makeSpec();

* other
    * 应急救护培训
        * 黄金4分钟：18秒缺氧；30秒昏迷；4-6分钟脑细胞不可逆死亡；10分钟后脑细胞全部死亡。
        * 心肺复苏：
            * C-胸外按压：1.按压30次吹气2次；2.频率100-120次/分钟；3.深度5-6厘米；
            * A-打开气道
            * B-人工呼吸
        * 海姆立克：
            * 自救：肚脐二指上一拳，弯腰，有规律上推；借助板凳或栏杆
            * 他救：从后抱住，挤压
            * 小孩：拍背，注意头朝下
        * AED 除颤
            * 左锁骨下 右胸下
            * 飞利浦 测试是否需要除颤

### 2020-1-13 January Monday

* 阴影的效果
    * Paint.setMaskFilter(new BlurMaskFilter(20,BlurMaskFilter.Blur.SOLID));
    * EmbossMaskFilter 凸起，类似浮雕，可用性相对较低
    * shape 一层一层画
    * https://www.cnblogs.com/tianzhijiexian/p/4297734.html

* IoC，Inversion of Control，控制反转，最常见的方式是依赖注入 DI（Dependency Injection），还有一种方式是依赖查找（Dependency Lookup）
    
* company
    * 留言板提测

### 2020-1-14 January Tuesday
* n**ew出来的实例**跟**执行时的对象**没有必然的联系
* Hook点：**静态变量和单例**；在一个进程之内，静态变量和单例变量是相对不容易发生变化的，因此非常容易定位，而普通的对象则要么无法标志，要么容易改变。（我们**没有办法拦截一个静态方法**，也没有办法获取到这个静态方法里面的局部变量）




