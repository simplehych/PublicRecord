[TOC]

## 0x01 前言

**为何 `shell` 命令都非常简短？**

eg：`echo`、`pwd`、`ls`、`mkdir`、`cat`、`less`、`curl`...

一开始设计 `Unix` 系统时，计算机与终端之间的连接速度很慢，因此采用非常简短的命令使用起来速度更快。不仅 `shell` 是这样，`Unix` 系统的其他部分（例如 `C` 编程语言）也是这样。

## 0x02 Shell 基础知识

本节内容学习自：[Shell 讲习班](https://classroom.udacity.com/courses/ud206)

**shell** ：是广泛用于计算机的命令行，它能调用文件和目录、运行程序、并与其他计算机进行交互。

 **Terminal**：如今，我们把用于与 `shell` 交互的程序，称为终端程序。类似的有 `iTerm`。

**bash**：迄今为止使用最多的 `UNIX` 风格的 `shell`。 类似的有 `zsh`。


* command line format 语法：

    ```
    command_name options arguments
    ```
    
    *eg:*
    
    ```
    ls -l ~
    ```

    系统可以接受的命令的名称(command-name)可以从如下途径获得：
    
    1. 确定的路径所指定的外部命令，eg: 当前目录下的可执行文件
    2. shell 内建命令(built-in)，eg:`pwd`
    3. $PATH 之下的外部命令，eg: `~.bash_profile` 配置的 PATH 环境变量
    4. 命令的别名(alias)，eg:`ll`
    5. 每一个命令行均必须包含命令的名称，这是不能缺少的。

* `man` - manual pages - 帮助命令，使用手册

    对你学习 shell 有很大帮助，包括**英文释义**，有助于记忆和理解。

    ```
    $ man ls
    NAME
         ls -- list directory contents
    
    SYNOPSIS
         ls [-ABCFGHLOPRSTUW@abcdefghiklmnopqrstuwx1%] [file ...]
    
    DESCRIPTION
        ...
    ```
    
    简单的使用操作请参考下文：`16.查看文件`

> 常用的 Tips：
> 1. 单引号和双引号作用不一样，不确定时请使用单引号
> 2. 命令用 `;` 分号隔开，使得同一行可以运行多个命令。eg：`$ cd ~; ls`
> 3. 很多 `shell` 命令默认与当前目录交互。eg：`ls .` == `ls` 不加参数
> 4. 多个选项可以合并使用。eg：`ls -a -l` == `ls -al`

假如我们需要在 command line 中将这些**保留元字符 的功能关闭的话**，就需要quoting处理了。

在bash中，quoting 有以下三种方法：
`hard quote`：单引号`'`，凡是在hard quote 中**所有**元字符meta均被关闭；
`soft quote`：双引号`"`，凡是在soft quote中**大部分**元字符会被关闭，但某些会被保留，eg：`$`；
`escape`：反斜杠`\`，只有在紧接着在 **跳脱字符 escape** 之后的单一元字符meta才会被关闭。

`meta` 又分为 `shell meta` 与 `command meta`；

从技术的细节来看，shell 会依据 IFS internal Filed Separator 将command line 所输入的文字**拆解**为“字段”（word/filed）。然后再针对特殊字符meta先做处理，最后**重组整行**command line。

### 1. echo 

回显，回声；单纯将其argument送至"标准输出"(stdout, 通常是我们的屏幕)。

打印输出，同其他语言 `print`，`console` 等。

*eg:*

```
$ echo 'Hello Wolrd!'
Hello Wolrd!

$ echo 'A'!
A!
$ echo 'A'!!
$ echo 'A'echo 'A'!
Aecho A!

$ echo $COLUMNS × $LINES
80 × 25
```

1. 打印的单引号非必须。
2. 注意结尾的 `!`，打印history记录，当有多个时会产生重复的效果，去除单引号有同样的效果。
3. 有些字符（*eg:* `'` 、`$` 、`?`、`*`、`!`等）对 `shell` 有特别的意义，如果你在 `shell` 中输入的内容被以奇怪的方式进行了处理，通常你需要做的是给它加上 单引号 `'` 进行quoting，特殊含义字符在单引号内无效。**用shell quoting 关闭掉shell meta**。

4. 单词前面的美元符号 `$`，表示它是一个 `shell` 变量。
5. 这些特殊的字符如果没有进行转义 `escape`，会被shell辨识为 **元字符**，下文会总结。

### 2. 浏览目录
**ls**  - `list` - 查看目录列表
**lsof** - `list open file` - 显示 linux 系统当前已经打开的所有文件列表，用于查看打开的进程

**cd**  - `Change Directory` - 切换目录

**open** - `open files and directories` - 打开文件或目录

注意几个特殊目录标识：
1. `~` 主目录
2. `.` 当前目录
3. `..` 父目录，上一级目录

*eg:*

```
$ ls ~
$ cd ..
$ open .
```

*eg: 查看某个端口号是否被占用 `lsof -i tcp:port`，tcp可省略不写*

```
$ lsof -i :31415
COMMAND PID    USER   FD    TYPE  DEVICE                SIZE/OFF  NODE  NAME
adb     33386  user   13u   IPv4  0x58131bae63fc74b5    0t0       TCP   localhost:31415
...
```

杀死该端口对应的进程PID：`kill -9 33386 `

### 3. 当前目录
**pwd** - `Print Working Directory` - 打印当前目录

*eg:*

```
$ pwd
/Users/username
```

注意：输出的字符串（由斜杠分隔的多个目录名称组成）称为 **路径** `Path`。
Unix 使用 **正斜杠 `/`** 分隔目录名称，同网络中的 `URL` 一样，而**非**反斜杠 `\`。

### 4. 参数和选项

*eg:*

```
$ ls -l /Users/user_name
total 0
drwx------@  5 user_name  staff   160 Jan 16  2019 Applications
drwx------@ 10 user_name  staff   320 Dec 22 16:30 Desktop
drwx------@ 12 user_name  staff   384 Nov  4 09:28 Documents
drwx------@ 26 user_name  staff   832 Dec 27 17:17 Downloads
drwx------+ 13 user_name  staff   416 Oct  8 20:12 Movies
drwx------+ 12 user_name  staff   384 Oct  8 20:12 Music
```

在上述命令 `ls -l /Users/user_name` 中：`ls` 是命令 command；`-l`是选项 option，表示long；`/Users/user_name` 是参数 argument。

**说明：**
`d rwx --- --- @/+`，一共11位。

第1位表示文件类型，后面的三个为一组，最后一个为额外属性。

d | rwx | --- | --- | @/+
--- | --- | --- | --- |---
文件类型 <br> -/d/i | owner 所有者权限 <br> rwx | group 所属组权限 <br>  rwx | other 其他人权限 <br> rwx | 额外权限 <br> eg:@/+

**文件类型：**`-`：二进制文件；`d`：目录；`i`：表示软连接文件。

**权限类型：**`r`：read 读权限；`w`：write 写权限；`x`：execute 执行权限；

`@` ：表示额外属性，Mac独有，可以用 `xattr -l test.file` 来看。mac上从网上下载个文件有时会提示“此文件来自网络下载不安全”。就是这个属性起的作用。而从mac下copy文件到linux系统上时会因为这个属性造成在linux下打不开的错误。需要手动去除掉 `xattr -c test.file`。

`+`：[ACL - Access Control List，除传统的rwx权限之外的权限配置](https://blog.csdn.net/u013733747/article/details/80875996)


后面的参数依次表示：
引用计数，文件被引用过多少次；
所有者，user_name / root，一般创建文件，所有者默认是创建者；
所属组，staff / root；
文件字节大小，不带单位表示字节；
最后修改时间；
文件名；

改变权限设置：
**chgrp**：改变所属群组
**chown**：改变作者
**chmod**：改变权限属性，eg：776

[rwx = 111 = 7
rw- = 110 = 6](https://blog.csdn.net/u013197629/article/details/73608613)


### 5. 常用参数说明

*eg:*

```
ls -l ~/Music/*.mp3
```

意为：查看 `～/Music` 目录下所有以 `.mp3` 结尾的文件，以单行长列表的样式打印。
其中 `*` 表示匹配0个或多个字符。


glob 模式（globbing）也称为 shell 通配符（wildcard）。
glob是一种特殊的模式匹配，最常见的是通配符拓展。


**shell 通配符 / glob 模式通常用来匹配目录以及文件，而不是文本！**

`Wildcard` 也是属于 `command line` 的处理工序，作用于 `arguments` 里的 `path` 之上。若argument不是path的话，那也与wildcard无关。

换句更为精确的定义来讲： **wildcard 是一种命令行的路径扩展(path expansion)功能。**

主要分为两种字符：**literal**（文字，没有特殊功能的符号）与 **meta**（有特殊功能）

通配符说明如下：

字符 | 解释
--- | ---
* | 匹配 **任意长度** 任意字符
? | 匹配 **任意单个** 字符（与 RE 的同 `.` 点 原理相同）
[list]| 匹配指定 **范围内** 的 **任意单个** 字符 或 字符集合，eg: `a*[0-9]*`、`init*[a-d]` == `[abcd]`
[! list]| 匹配指定 **范围外** 的 **任意单个** 字符 或 字符集合，（与 RE 的同 [^ list]原理相同）
{str1, str2, ...}| 匹配 str1 或 str2 或更过字符串，也可以是集合


专用字符集说明如下：

字符 | 意义
--- | ---
[:alnum:] | 任意数字或者字母
[:alpha:] | 任意字母
[:space:] | 空格
[:lower:] | 小写字母
[:digit:] | 任意数字
[:upper:] | 任意大写字母
[:cntrl:] | 控制符
[:graph:] | 图形
[:print:] | 可打印字符
[:punct:] | 标点符号
[:xdigit:] | 十六进制数
[:blank:] | 空白字符（未验证）


> 使用通配符过程中如果包含了特殊字符的情况，在不使用专用字符集的情况下，需要使用转义符来匹配，或使用单引号将其框选。

> 这些特殊的字符如果没有进行转义，会被 `shell` 辨识为 **元字符**。

元字符 `meta` 说明如下：

字符 | 作用
--- | ---
`IFS` | Internal Field Separator，分隔符，由 < space > 或 < tab > 或 < enter > 三者之一组成
`CR` | 由 < enter > 产生
`=` | 设定变量，注意左右无空格
`$` | 做变量或运算替换
`>` | 输出重定向 stdout
`<` | 输入重定向 stdin
`|` | 命令管线
`&` | 重定向文件描述符file descriptor，或将命令置于后台 bg 静默执行
`()` | 命令群组command group，将其内部的命令置于 nested subshell 执行，或用于运算或命令替换
`{}` | 命令群组command group，将其内部的命令置于 non-named function 中执行，或用在变量替换的界定范围
`;` | 在前一个命令结束时，忽略返回值，继续执行下一个命令
`&&` | 在前一个命令结束时，若返回值为 true，继续执行下一个命令
`||` | 在前一个命令结束时，若返回值为 false，继续执行下一个命令
`!` | 执行 history 中的命令

注意事项：

1. 在使用专属字符集的时候，字符集之外还需要用 [ ] 来包含住，否则专用字符集不会生效，例如 [[:space:]]
2. 想要转义的时候，单引号与双引号使用方法是不同的，单引号会转义所有字符，而且单引号中间不允许再出现单引号，双引号允许出现特定的 shell 元字符，具体字符可以自行查询
3. 在使用花括号 {} 的时候，里面的单个字符串需要使用单引号或者双引号括住，否则就会视为多个的单个字符

参考资料：
[Wildcard](http://wiki.jikexueyuan.com/project/13-questions-of-shell/wildcard.html)
[Regular Expression](http://wiki.jikexueyuan.com/project/13-questions-of-shell/regular-expression.html)
[Linux shell 通配符 / glob 模式](https://www.cnblogs.com/divent/archive/2016/08/11/5762154.html)

### 6. 常用选项说明
备注：为方便记忆理解，英文释义不一定正确 `explaining the joke`。

命令 commands | 选项 options 
--- | ---
ls | -a，all，全部文件，包括隐藏文件 <br/> -l，long，长列表 <br> -h，使用 -l 时文件大小添加单位描述

### 7. 整理文件 
**touch** / **vim** - 创建文件 file
**mkdir** - `make directory` - 创建文件夹 directory

**rm** - `remove` - 删除文件 file
**rmdir**: - 删除文件夹  directory

**mv** - `move` - 移动文件 file 或文件夹 directory，有重命名的作用

**cp** - `copy` - 复制文件

*eg:*

```
$ mv file1.txt file2.txt target_directory
```

shell 没有回收站找回功能，当删除重要的文件时，可以使用 `-i` interactive 进行交互，删除之前询问你是否删除。


### 8. 下载

**curl** - `see URL` - 查看网页

`-L` : `--location` 遵循 Web 重定向

*eg:*

```
$ curl -L 'http://google.com'
```

`-o`: `--output <file>` 写入目的文件，而不是直接在终端打印展示

*eg:*
 
```
$ curl -o google.html -L 'http://google.com'
```

注意：本例中，不加引号也可以实现，但是有些 `URL` 有 `&` 等特殊含义字符，为了养成良好的习惯，我们加上引号`'`。


### 9. 查看文件

**cat** - 显示文件内容（适合内容较少的文件）- `Catenate` / `Contatenate` 的缩写，（连接/连环 级联）指连接多个事物
读取文件并输出内容，可读取任意数量的文件。
**tac** - 反向显示文件内容（适合内容较少的文件），**Mac没有该命令。**

**less** ，分页显示文件内容命令（可以前后翻页），一次向我们展示文件更少的部分（占满一屏幕的文件内容）。
**more** ，分页显示文件内容命令（不能向前翻页）。

**head** - 头 - 显示文件内容命令（指定行数），`-n`：指定显示的行数，默认20，eg：`head -n 10 test.txt`。
**tail** - 尾：**反向**查看文件内容命令，更新也能动态显示，多用于**日志**显示。`-n`：指定显示的行数；`-f`：动态显示文件末尾内容（即文件实时变化，那么显示内容也会随之变化）；

`less` 使用的相关操作说明如下：
1. **方向键**：上下键翻页
2. **空格**：下翻；**b**：上翻
3. **/**: 搜索，**n**：下一个；**N**：上一个
4. **q**: 退出

`cat`：它快速的扫荡了我们终端文件，直接达到了末尾，如果想看开头，这对我们意义不大。
`less` ：一次向我们展示文件更少的部分（占满一屏幕的文件内容），许多其他程序调用了它，例如 `man` 程序查看的方式是 `less`。

[Linux常用命令之文件和目录处理命令](https://www.cnblogs.com/ysocean/p/7712412.html)

### 10. 搜索和管道

**grep** - `global search regular expression(RE) and print out the line`
**|** - `pipe`

regular expression 正则表达式也叫做 `regexps` 或 `regexes`，使用 grep 可以进行一整套的复杂语言搜索操作。

*eg: 对 `file.txt` 文件执行 `grep keyword` 命令，然后传递给 less 程序，如下：*

```
$ grep keyword file.txt | less  
```

其中 `keyword` 为你想查找的关键字。

同理 `grep` 命令也可以处理来自另一个程序的输入。

*eg:*

```
$ curl -L https://tinyul.com/zeyq9vc | grep keyword | wc -l
...
105
$ curl -L https://tinyul.com/zeyq9vc | grep -c keyword
...
105
```

**wc -l** - `word count --line` - 行数
**-c** - `count` - 行数

**运算符在 shell 中是通配符，但在 grep 中有点不同。（虽然 "grep" 和 "glob" 都很奇怪）**


### 11. Shell变量和环境变量

*eg: 创建一个 `numbers` 变量*

```
$ numbers='one two three'  // 注意=两边没有空格
$ echo $numbers
one two three
```

Shell有两种类型的变量：Shell变量，环境变量。

**Shell变量**，`Shell` 程序本身的内部变量，如 `COLUMNS`、`LINES`。
**环境变量**，环境变量是与在 `Shell` 中运行的程序 **共享的变量**，如 `PATH` 变量。
**本地变量**，在当前Shell中所定义的变量。

["遗传性" 就是区分本地变量与环境变量的决定性指标。exec 和 source 命令的区别](http://wiki.jikexueyuan.com/project/13-questions-of-shell/exec-source.html)

> **环境变量**，落脚点在变量，明确它只是一种变量的名称，例如常用的 `PATH`变量、`CLASSPATCH`变量、`JAVA_HOME`变量 等都叫环境变量。对于系统OS（内核Kernel）而言，分为 **系统环境变量** 和 **用户环境变量**，添加配置的文件路径分别为 `/etc/profile` 和 `~/.bash_profile`。
> 以 java 命令说明**查找可执行文件的过程**：先在当前目录查找 java程序，如果有就执行，否则会在 `PATH` 变量中指定的路径中依次寻找（每个路径冒号`;`分隔），如果有就执行，否则提示 **command not found**。

Java Tips：对于 `java -classpath dir helloworld.java`，使用了 `-classpath` 选项后，JRE将严格按-classpath指定的路径来搜索Java类，指定一个路径有局限性，容易报错 `java.lang.NoClassDefFoundError`。可以指定多个，使用 `java -classpath %CLASSPATH%;.;dir helloworld.java`。`CLASSPATH`的作用是JRE会去该变量指定的路径中搜索所需的类（.class）文件。

```
$ echo $PATH
/usr/local/bin:/usr/bin:/bin:/usr/sbin
```
一个非常重要的环境变量，如 `PATH` 变量，它会告诉系统你的程序文件的位置。

`PATH`中的变量目录使用冒号 `:` 分隔.

当使用 `ls` 程序时，`Shell` 会从 `PATH` 第一个开始搜索，不断向右，直到找到你输入的命令。

**Q: 你认为 PWD 变量是 shell 变量还是环境变量？**
**A:** `echo $PWD` 打印的值是当前工作目录，和运行 `pwd` 命令看到的一样。你在类 Unix 系统上运行的每个程序都具有某种工作目录。通常以你启动程序时所在的目录开始。**因此 `PWD` 变量是环境变量,而不仅仅是 `shell` 内部变量。**

将一个目录添加到 `PATH` 以便能够找到其中的程序，添加方式：
1. PATH=$PATH:后跟新目录；这种方式更改只会持续到关闭Shell前，每次 Shell 启动都会重置。
2. .bash_profile文件中配置，永久性的，每次启动时都会运行该文件。

环境变量和PATH变量的区别？还有什么别的环境变量


### 12. 起始文件 .bash_profile

`Shell` 不只是一个用户界面，它还是一种编程语言。包含 **shell 命令** 的文件叫**Shell脚本**，常见的有以`.sh`结尾的文件。

由于历史的原因，Bash Shell 能在启动时运行几个不同的文件来获取其配置。

在Mac系统或者带有Git Bash的Windows系统上，你打开的终端中的Shell都将加载名为 .bash_profile 的文件中的指令。
但在 Linux系统上 `.bash_profile` 仅对某些Shell会话加载，特别是**登陆Shell会话**，**非登陆Shell会话**则会加载一个名为 `.bashrc` 的文件。

如果你想在不同的操作系统上使用相同的Shell配置，这种不一致性可能会是个问题，一种常用的解决方法是：在你的 `.bash_profile` 中加入一个语句，如果有一个名为 `.bashrc` 的文件则运行它，代码如下：

```
# .bash_profile
...
if [-f ~/.bashrc]; then
    source ~/.bashrc
fi
...
```

在 Mac 和 Winodow 系统上则不用处理。

`.bash_profile` 另一个日常用处：

你放入此配置文件的任何命令都会在你每次启动Shell时运行，比如更改 $PATH，它也可以包含你希望在启动Shell会话时，看到的任何内容，比如你想看到日期和友好消息，直接在此文件加上命令即可，代码如下：

```
# .bash_profile
...
date
echo "Hey there!"
...
```

### 13. 控制 Shell 提示符 $PS1

当你打开一个终端时，通常你会看到一个 `Shell` 提示符。
但不同的系统，它的内容会有所不同。例如 Windows的Git Bash 和 Mac的Terminal不同。

提示符 `prompting` 操作说明位置：
1. `man bash` 
2. 查找 `/PROMPTING`

**修改 PS1**

```
~/Tmp
$ echo $PS1
$

$ PS1='(_user_host)${_current_dir} $(git_prompt_info) $(_ruby_version)
%{$fg[$CARETCOLOR]%}▶%{$resetcolor%}'

~/Tmp
▶ 
```

构建长而复杂的 `$PS1` 提示符的工具，网址为 http://bashrcgenerator.com 请试试。

### 14. 别名

`alias`，这是一种缩短 `shell` 命令的方法

使用 `alias` 列举你拥有的所有别名，无需任何参数：

*eg:*

```
$ alias
-='cd -'
...=../..
....=../../..
.....=../../../..
......=../../../../..
1='cd -'
2='cd -2'
3='cd -3'
...
```

**配置方式：**
1. 当前窗口执行，*eg:* `alias ll='ls -la'`，只在当前窗口关闭前有效。
2. `.bash_profile` 文件中配置，你的别名会在每次启动 Shell 时都可用。

*eg:*
 
```
$ echo $ll

$ alias ll='ls -la'
$ which ll
ll: aliased to ls -la
$ alias sl=ls //当输入sl时，可达到自动纠正效果
```


### 15. Shell 资源

[Shell脚本编程30分钟入门](https://github.com/qinjx/30min_guides/blob/master/shell.md)
[Bash 学院]()
[Bash 初学者指南(英)](http://www.tldp.org/LDP/Bash-Beginners-Guide/html/)
[Bash 编程指南(英)](http://tldp.org/HOWTO/Bash-Prog-Intro-HOWTO.html)
[Regexr — 学习正则表达式(英)](http://regexr.com/)


## 0x03 常用命令补充

### 编辑文件

* **vim**

* **pico**
PIne's message COmposition editor ，编辑文字文件


### 切换用户

* **su**
swith user，切换用户

* **sudo**
switch user do，用来以其他身份来执行命令，预设身份为 root


https://www.zhihu.com/question/49073893
https://i.linuxtoy.org/docs/guide/ch02s02.html
https://www.cnblogs.com/summersoft/p/7475483.html

### 树型查看目录结构

* 方法1：安装 tree

    ```
    $ brew install tree
    $ tree -L 2 -N  
    ```
   ` 2`：显示两级结构
    `-N`： 解决中文乱码

* 方法2：命令行输入如下

    ```
    $ find . -print | sed -e 's;[^/]*/;|____;g;s;____|; |;g'
    ```

    如果直接指定别名

    ```
    alias treee="find . -print | sed -e 's;[^/]*/;|____;g;s;____|; |;g'"
    ```

### readelf，参看ELF文件

在 `Windows` 系统上的 `.exe` 安装文件和使用共享(破解)软件时用来覆盖的 `.dll` 动态链接库文件，都是 **PE 格式文件**。

在 `Linux` 系统上的 `.o` 目标文件、`.a` 静态连接库和 `.so` 动态连接库，都属于 **ELF 文件**。

在 `Mac`系统上和 Linux 类似，使用 readelf 需要安装 binutils： `brew install binutils `

```
$ readelf -a libnative-lib.so
```


[加密混淆，应用就安全了嘛？](https://mp.weixin.qq.com/s/NVDeWbjIQsEB7iWbR9ZV4A)

## 0x04 Vim 快捷键
~/.vimrc 配置


[http://www.cnblogs.com/yangjig/p/6014198.html](http://www.cnblogs.com/yangjig/p/6014198.html)
[https://www.cnblogs.com/zourrou/archive/2011/04/16/2018493.html](https://www.cnblogs.com/zourrou/archive/2011/04/16/2018493.html)
[https://www.cnblogs.com/zourrou/archive/2011/04/16/2018493.html](https://www.cnblogs.com/zourrou/archive/2011/04/16/2018493.html)


快捷键 | 描述 
--- | ---
ESC | 命令模式
V | 视图模式 Visual
i / a | 输入模式Insert/Append
I | 起始输入
A | 末尾输入
o | 下一行输入
O | 上一行输入
d | 剪切
y | 复制
p | 粘贴
vfx | v （选择） f（找到） x （x位置 含x）
vtx | t的用法和f一样，区别在于，选择的是不含指定的字符本身的那段字符 
di{ / di} | 可删除 {}中的所有内容
diw / yiw | 删除单词 / 复制单词
h / j / k / l | 左 /  下 / 上 / 右
3h / 3j / 3k /3 l | 3左 /  3下 / 3上 / 3右
^ / $ | 行首 / 行尾
gg / G | 文件头 / 文件尾
u | 撤销 undo
r | 返回 redo
yw | 复制当前单词
yy | 复制当前行
3yy | 复制三行 
p | 粘贴
x | 删除当前光标下的字符
dw | 删除光标之后的单词剩余部分
db | 删除单词之前的单词
d$ | 删除光标之后的该行剩余部分
dd | 删除当前行
3dd | 删除当前向下三行
c | 功能和 d 相同，之后进入 INSERT MODE
cc | 删除当前行，之后进入 INSERT MODE
Alt + j/h | 移动当前行
ctrl + w + hjkl | 切换窗口
[range]copy{address} | [https://liuzhijun.iteye.com/blog/1845534](https://liuzhijun.iteye.com/blog/1845534)
多窗口 | [https://blog.csdn.net/Devil_2009/article/details/7006113](https://blog.csdn.net/Devil_2009/article/details/7006113)


## 0x05 命令行快捷键

### 补全命令 tab

### 移动光标快捷键

Ctrl + a | 光标回到命令行首 ahead
--- | ---
Ctrl + e  | 光标回到命令行末 end
Ctrl + f / 方向键-右  | 光标向**右**移动一个**字符** forward
Ctrl + b / 方向键-左  | 光标向**左**移动一个**字符** backward
**Esc + f** | 光标向**右**移动一个**单词**，或当前单词的开头 forward，bash中可用 `Alt + 方向右键`
**Esc + b** | 光标向**左**移动一个**单词**，或当前单词的末尾 backward，bash中可用 `Alt + 方向左键`

### 删除-剪切-粘贴命令

Ctrl + u | 剪切光标处到行首的字符。（有删除的作用）Zsh 测试为删除整行，Bash为删除到行首。
--- | ---
Ctrl + k  | 剪切光标处到行末的字符。（有删除的作用）
**Ctrl + w** | 剪切光标**前**的一个**单词**（有删除作用）
暂未发现 | 剪切光标**后**的一个**单词**（有删除作用）

Ctrl + y | 粘贴 上述剪切的内容
--- | ---

### 搜索历史命令记录

Ctrl + r | 搜索命令行中使用过的命令记录
--- | ---
Ctrl+g | 从正在执行Ctrl+r的搜索中退出。

