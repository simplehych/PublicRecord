获取帮助

```
$ git help <verb>
$ git <verb> --help
$ man git-<verb>
```

Index空间，也叫做stage空间，或者cache空间

## 0x01 版本控制 VCS
Version Control System

版本控制是一种记录一个或若干文件内容变化，以便将来查阅特定版本修订情况的系统。

可以对任何类型的文件进行版本控制。如代码、图片等等。

### 版本控制发展历程

#### 0. 复制
复制整个项目的方式保存不同的版本，改名加上备份时间以示区别。

**好处：**简单

**问题：**但容易犯错，混淆工作目录，写错文件或覆盖文件。

#### 1. 本地版本控制系统
采用某种简单的**本地数据库**来记录文件的历史更新差异。

**举例：**RCS，原理是在硬盘上保存补丁集（补丁是文件修订前后的变化），通过应用所有的补丁，重新计算出各个版本的文件内容。

**问题：**不同系统上的开发者如何协同工作？

#### 2. 集中化的版本控制系统 CVCS
CVCS，Centralized Version Control System

**单一的集中管理服务器**，保存所有文件的修订版本，而协同工作的人们都通过客户端连到这台服务器，取出最新的文件或者提交更新。

**举例：**Subversion、CVS、Perforce等

**好处：**相对于老式的本地VCS，可以看到项目其他人的进度；管理员可以掌握开发者权限，并且管理一个CVCS远比在各个客户端上维护本地数据库容易。

**问题：**中央服务器的单点故障。宕机导致无法提交更新，无法协同工作。如果中心数据库所在磁盘损坏，没有备份，将失去所有数据。

#### 3. 分布式版本控制 DVCS
DVCS，Distributed Version Control System

客户端并不只提取最新版本的文件快照，而是把代码仓库完整地镜像下来。

**举例：**Git、Mercurial、Bazaar、Darcs

**好处：**
1. 任何一处协同工作用的服务器发生故障，事后都可以用任何一个镜像出来的本地仓库恢复；
2. 和若干不同的远端代码仓库进行交互。在同一个项目中，分别和不同工作小组的人相互协作。比如**层次模型式**的工作流，而这在以前的集中式系统中是无法实现的。

#### 对比
![](https://upload-images.jianshu.io/upload_images/7867366-79fe48490d3e6199.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

## 0x02 Git 基础知识

### 2.1 Git 目标特点
* 速度
* 简单的设计
* **对非线性开发模式的强力支持**（允许成千上万个并行开发的分支）
* 完全分布式
* 有能力高效管理类似 Linux 内核一样的超大规模项目（速度和数据量）
---
### 2.2 基础知识

#### 直接记录快找，而非差异比较
对待数据的方法：
Git：保存每个版本的文件快照
其他：保存每个版本的文件差异

#### 近乎所有的操作都是本地执行
感受速度之神赐给 Git 的超凡能量

Git 绝大多数操作只需要访问本地文件和资源。
无网操作。

####  Git 保证完整性
Git 中所有数据在存储前都计算校验和，然后以校验和来引用。

Git 用以计算校验和的机制叫做 SHA-1 散列（hash，哈希）。 这是一个由 40 个十六进制字符（0-9 和 a-f）组成的字符串，基于 Git 中文件的内容或目录结构计算出来。 

SHA-1 哈希看起来是这样：
`24b9da6552252987aa493b52f8696cd6d3b00373`

Git 数据库中保存的信息都是以文件内容的哈希值来索引，而不是文件名。

[简单易懂理解Git的工作原理（一）Git数据模型](https://www.jianshu.com/p/ae3f7c954061)


####  Git 一般只添加数据
你执行的 Git 操作，几乎只往 Git 数据库中增加数据。 很难让 Git 执行任何不可逆操作，或者让它以任何方式清除数据。 同别的 VCS 一样，未提交更新时有可能丢失或弄乱修改的内容；但是一旦你提交快照到 Git 中，就难以再丢失数据，特别是如果你定期的推送数据库到其它仓库的话。

这使得我们使用 Git 成为一个安心愉悦的过程，因为我们深知可以尽情做各种尝试，而没有把事情弄糟的危险。

####  Git 三个区域 / 三种状态
三个区域：
1. 工作目录，Working Directory
2. 暂存区域，Staging Area
3. Git 仓库，.git directory  (Repository)

三种状态：
1. 已修改，modified
2. 已暂存，staged
3. 已提交，committed

工作目录，是对项目的某个版本独立提取出来的内容。 这些从 Git 仓库的压缩数据库中提取出来的文件，放在磁盘上供你使用或修改。

暂存区域，是一个文件，保存了下次将提交的文件列表信息，一般在 Git 仓库目录中。 有时候也被称作“索引”，不过一般说法还是叫暂存区域。

Git 仓库目录，是 Git 用来保存项目的元数据和对象数据库的地方。 这是 Git 中最重要的部分，从其它计算机克隆仓库时，拷贝的就是这里的数据。

已修改，表示修改了文件，但还没保存到数据库中。 

已暂存，表示对一个已修改文件的当前版本做了标记，使之包含在下次提交的快照中。

已提交，表示数据已经安全的保存在本地数据库中。

基本的 Git 工作流程如下：
1. 在工作目录中修改文件。
2. 暂存文件，将文件的快照放入暂存区域。
3. 提交更新，找到暂存区域的文件，将快照永久性存储到 Git 仓库目录。

## 0x03 Git 基础命令

### 3.1 获取 Git 仓库

###### 在现有目录中初始化仓库
```
git init
```
该命令将创建一个名为 .git 的子目录，这个子目录含有你初始化的 Git 仓库中所有的必须文件，这些文件是 Git 仓库的骨干。 但是，在这个时候，我们仅仅是做了一个初始化的操作，你的项目里的文件还没有被跟踪。

你可通过 git add 命令来实现对指定文件的跟踪，然后执行 git commit 提交：
```
$ git add *.c
$ git add LICENSE
$ git commit -m 'initial project version'
```
######  克隆现有仓库
当你执行 git clone 命令的时候，默认配置下远程 Git 仓库中的每一个文件的每一个版本都将被拉取下来。
如果你对其它的 VCS 系统（比如说 Subversion）很熟悉，请留心一下你所使用的命令是"clone"而不是"checkout"。 这是 Git 区别于其它版本控制系统的一个重要特性，Git 克隆的是该 Git 仓库服务器上的几乎所有数据，而不是仅仅复制完成你的工作所需要文件。

克隆仓库的命令格式是 `git clone [url] `。 

 比如，要克隆 Git 的可链接库 libgit2，可以用下面的命令：
```
$ git clone https://github.com/libgit2/libgit2
```
这会在当前目录下创建一个名为 “libgit2” 的目录，并在这个目录下初始化一个 .git 文件夹。

如果你想在克隆远程仓库的时候，自定义本地仓库的名字，你可以使用如下命令：
```
$ git clone https://github.com/libgit2/libgit2 mylibgit
```
这将执行与上一个命令相同的操作，不过在本地创建的仓库名字变为 mylibgit。
Git 支持多种数据传输协议。 上面的例子使用的是 https:// 协议，不过你也可以使用 git:// 协议或者使用 SSH 传输协议，比如 user@server:path/to/repo.git 。 

### 3.2 记录每次更新到仓库
工作目录下的每一个文件都不外乎这两种状态：**已跟踪** 或 **未跟踪**。
![文件的状态变化周期](https://upload-images.jianshu.io/upload_images/7867366-19efade277c5acc5.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

#### 检查当前文件状态  `git status`

#### 跟踪新文件  `git add`

#### 暂存已修改文件 `git add`

多功能命令`git add`:
1. 开始跟踪新文件
2. 把已跟踪的文件放到暂存区
3. 用于合并时把有冲突的文件标记为已解决状态
4. ...
这个命令理解为 **“添加内容到下一次提交中”** 而不是“将一个文件添加到项目中”要更加合适。

#### 状态简览 `git status -s`
`git status` 命令的输出十分详细，但其用语有些繁琐。 

 `git status -s` 或 `git status --short` 命令，将得到一种更为紧凑的格式输出。

运行 git status -s ，状态报告输出如下：

```
$ git status -s
 M README
MM Rakefile
A  lib/git.rb
M  lib/simplegit.rb
?? LICENSE.txt
```

??：新添加的未跟踪的文件
A：新添加到暂存区中的文件
M左右两个位置：
左M：文件被修改了并放入了暂存区
右M：文件被修改了但是还没放入暂存区

#### 忽略文件 `.gitignore` 的配置
一般我们总会有些文件**无需纳入 Git 的管理**，也不希望它们总出现在未跟踪文件列表。 通常都是些自动生成的文件，比如日志文件，或者编译过程中创建的临时文件等。

在这种情况下，我们可以创建一个名为 .gitignore 的文件，列出要忽略的文件模式。 

来看一个实际的例子：
```
$ cat .gitignore
*.[oa]
*~
```

文件 `.gitignore` 的格式规范如下：
* 所有空行或者以 `＃` 开头的行都会被 Git 忽略。
* 可以使用标准的 glob 模式匹配。
* 匹配模式可以以 `/` 开头防止递归。
* 匹配模式可以以 `/`  结尾指定目录。
* 要忽略指定模式以外的文件或目录，可以在模式前加上惊叹号 `!` 取反。

所谓的 glob 模式是指 shell 所使用的简化了的正则表达式：

表达式 | 说明
--- | ---
`*`  星号 | 匹配零个或多个任意字符
[abc] | 匹配任何一个列在方括号中的字符（这个例子要么匹配一个 a，要么匹配一个 b，要么匹配一个 c）
[0-9] | 如果在方括号中使用短划线分隔两个字符，表示所有在这两个字符范围内的都可以匹配，表示匹配所有 0 到 9 的数字
`?`问号  | 只匹配一个任意字符
`**` 两个星号 | 表示匹配任意中间目录，比如 `a/**/z` 可以匹配 `a/z` , `a/b/z` 或 `a/b/c/z` 等

> GitHub 有一个十分详细的针对数十种项目及语言的 `.gitignore` 文件列表，你可以在 [https://github.com/github/gitignore](https://github.com/github/gitignore) 找到它.

#### 查看已暂存和未暂存的修改 `git diff`
如果 `git status` 命令的输出对于你来说过于模糊，你想知道具体修改了什么地方，可以用 `git diff `命令。

两个问题：
当前做的哪些更新还没有暂存？ 
有哪些更新已经暂存起来准备好了下次提交？

 `git status` 已经通过在相应栏下列出文件名的方式回答了这个问题
`git diff` 将通过文件补丁的格式显示具体哪些行发生了改变。

命令 | 说明
--- | ---
`git diff` 不加参数 | 比较的是  **工作目录** 和 **暂存区域** 之间的差异
`git diff --cached` | 比较的是 **暂存区域** 和 **Git仓库** 之间的差异
`git diff --staged` | 同 --cached，Git 1.6.1 新增，效果相同，但更好记
`git diff HEAD` | 比较的是 **工作目录** 和 **Git仓库** 之间的差异

> `git diff` 不是比较 **工作目录** 和 **Git仓库** 之间的差异

输出差异的概要：`git diff --stat`
比较某一个文件：`git diff test.md` 或 `git diff -- test.md` 

如果你喜欢通过图形化的方式或其它格式输出方式的话，可以使用 `git difftool `命令来用 Araxis ，emerge 或 vimdiff 等软件输出 diff 分析结果。 使用 `git difftool --tool-help` 命令来看你的系统支持哪些 Git Diff 插件。

#### 提交更新 `git  commit`
每次准备提交前，先用 `git status` 看下，是不是都已暂存起来了， 然后再运行提交命令.

```
$ git commit
```

这种方式会启动文本编辑器以便输入本次提交的说明。(一般都是 vim 或 emacs,使用 git config --global core.editor 命令设定你喜欢的编辑软件)
编辑器会显示类似下面的文本信息

```

# Please enter the commit message for your changes. Lines starting
# with '#' will be ignored, and an empty message aborts the commit.
# On branch master
# Changes to be committed:
#	new file:   README
#	modified:   CONTRIBUTING.md
#
~
~
~
".git/COMMIT_EDITMSG" 9L, 283C
```

1. 默认的提交消息包含最后一次运行 git status 的输出，放在注释行里
2. 另外开头还有一空行，供你输入提交说明。
3. 如果想要更详细的对修改了哪些内容的提示，可以用 `git  commit -v` ，这会将你所做的改变的 diff 输出放到编辑器中从而使你知道本次提交具体做了哪些修改。
4. 退出编辑器时，Git 会丢掉注释行，用你输入提交附带信息生成一次提交。

另外：你也可以在 `commit` 命令后添加 `-m` 选项，将提交信息与命令放在同一行，如下所示：

```
$ git commit -m "Story 182: Fix benchmarks for speed"

[master 463dc4f] Story 182: Fix benchmarks for speed
 2 files changed, 2 insertions(+)
 create mode 100644 README
```

提交后它会告诉你：
1. 当前是在哪个分支（master）提交的
2. 本次提交的完整 SHA-1 校验和是什么（463dc4f）
3. 在本次提交中，有多少文件修订过，多少行添加和删改过。

注意：提交时记录的是放在暂存区域的快照； 任何还未暂存的仍然保持已修改状态，可以在下次提交时纳入版本管理；每一次运行提交操作，都是对你项目做一次快照，以后可以回到这个状态，或者经习惯比较。

#### 跳过添加暂存区域步骤直接提交
尽管使用暂存区域的方式可以精心准备要提交的细节，但有时候这么做略显繁琐。

Git 提供了一个跳过使用暂存区域的方式， 只要在提交的时候，给 `git commit `加上 `-a` 选项，Git 就会自动把所有已经跟踪过的文件暂存起来一并提交，从而跳过 `git add` 步骤：

```
$ git commit -a -m 'added new benchmarks'
>经验证，当有未跟踪的文件时，使用失败

```
#### 移除文件 `git rm`
要从 Git 中移除某个文件，就必须要从已跟踪文件清单中移除（确切地说，是从暂存区域移除），然后提交。

如果只是简单地从工作目录中手工删除文件，运行 `git status` 时就会在 “Changes not staged for commit” 部分（也就是 未暂存清单）
然后再运行 `git rm` 记录此次移除文件的操作，下一次提交时，该文件就不再纳入版本管理了。

直接运行 `git rm` 可以同时删除本地文件，并纳入版本管理。

`git rm -f`：如果删除之前修改过并且已经放到暂存区域的话，则必须要用强制删除选项 `-f`（译注：即 force 的首字母）。 这是一种安全特性，用于防止误删还没有添加到快照的数据，这样的数据不能被 Git 恢复。

`git rm --cached`：如果我们想把文件从 Git 仓库中删除（亦即从暂存区域移除），但仍然希望保留在当前工作目录中。 换句话说，你想让文件保留在磁盘，但是并不想让 Git 继续跟踪。 当你忘记添加 .gitignore 文件，不小心把一个很大的日志文件或一堆 .a 这样的编译生成文件添加到暂存区时，这一做法尤其有用。 为达到这一目的，使用 --cached 选项。
```
$ git rm --cached README
```
`git rm`命令后面可以列出文件或者目录的名字，也可以使用 `glob` 模式。 比方说：
```
$ git rm log/\*.log
```
注意到星号` * `之前的反斜杠` \`， 因为 Git 有它自己的文件模式扩展匹配方式，所以我们不用 shell 来帮忙展开。 此命令删除 `log/` 目录下扩展名为 `.log` 的所有文件。 

#### 移动文件 / 修改名称 `git mv`
不像其它的 VCS 系统，Git 并不显式跟踪文件移动操作。

如果在 Git 中重命名了某个文件，仓库中存储的元数据并不会体现出这是一次改名操作。 不过 Git 非常聪明，它会推断出究竟发生了什么，至于具体是如何做到的，我们稍后再谈。

既然如此，当你看到 Git 的 mv 命令时一定会困惑不已。 要在 Git 中对文件改名，可以这么做：
```
$ git mv file_from file_to
```
它会恰如预期般正常工作。 实际上，即便此时查看状态信息，也会明白无误地看到关于重命名操作的说明
```
$ git mv README.md README
$ git status
On branch master
Changes to be committed:
  (use "git reset HEAD <file>..." to unstage)

    renamed:    README.md -> README
```
其实，运行 `git mv` 就相当于运行了下面三条命令：
```
$ mv README.md README
$ git rm README.md
$ git add README
```
如此分开操作，Git 也会意识到这是一次改名，所以不管何种方式结果都一样。 两者唯一的区别是，`mv` 是一条命令而另一种方式`git rm`需要三条命令，直接用 git mv 轻便得多。 

不过有时候用其他工具批处理改名的话，要记得在提交前删除老的文件名，再添加新的文件名。

### 3.3 查看提交历史
#### 查看提交历史 `git log`
```
$ git log --pretty=format:"%h %s" --graph
* 2d3acf9 ignore errors from SIGCHLD on trap
*  5e3ee11 Merge branch 'master' of git://github.com/dustin/grit
|\
| * 420eac9 Added a method for getting the current branch.
* | 30e367c timeout code and tests
* | 5a09431 add timeout protection to grit
* | e1193f8 support for heads with slashes in them
|/
* d6016bc require time for xmlschema
*  11d191e Merge branch 'defunkt' into local
```
#### 选择性输出

放在最后位置上的选项，所以用两个短划线（--）隔开之前的选项和后面限定的路径名。

一个常用的选项是 `-p` / `--patch` 用来显示每次提交的内容差异。 你也可以加上 `-2` 来仅显示最近两次提交。

`git log -p` 输出日志说明：[更改 git log 显示信息的方式](https://www.jianshu.com/p/00842c43d28e)



### 2.4 Git 基础 - 撤销操作

#### 2.4.1 撤销操作 `git commit --amend`

注意，有些撤消操作是不可逆的。

第一次提交完之后，发现 1. 提交信息写错了；2. 漏掉几个文件没有添加。此时可以运行带有 `--amend` 选项的提交命令尝试重新提交：
```
$ git commit --amend
```
这个命令会将暂存区中的文件提交。

1. 如果自上次提交以来你还未做任何修改，，那么快照会保持不变，而你所修改的只是提交信息。

2. 你提交后发现忘记了暂存某些需要的修改，可以像下面这样操作，最终你只会有一个提交——第二次提交将代替第一次提交的结果。
```
$ git commit -m 'initial commit'
$ git add forgotten_file
$ git commit --amend
```
#### 2.4.2 已添加到暂存区的文件 取消暂存 `git reset HEAD <file> `


> 虽然在调用时加上 `--hard` 选项可以令 `git reset` 成为一个危险的命令（译注：可能导致工作目录中所有当前进度丢失！），但本例中工作目录内的文件并不会被修改。 不加选项地调用 `git reset` 并不危险 — 它只会修改暂存区域。

#### 2.4.3 已修改工作目录的文件 还原到上次提交 `git checkout -- <file>`

> 你需要知道 `git checkout -- [file]` 是一个危险的命令，这很重要。 你对那个文件做的任何修改都会消失——你只是拷贝了另一个文件来覆盖它。 除非你确实清楚不想要那个文件了，否则不要使用这个命令。
 
记住，在 Git 中任何 *已提交的* 东西几乎总是可以恢复的。 甚至那些被删除的分支中的提交或使用 `--amend` 选项覆盖的提交也可以恢复（阅读 [数据恢复](https://git-scm.com/book/zh/v2/ch00/r_data_recovery) 了解数据恢复）。 然而，任何你未提交的东西丢失后很可能再也找不到了。

### 2.5 Git 基础 - 远程仓库的使用

#### 2.5.1 查看远程仓库 `git remote`
```
$ git remote
origin

$ git remote -v
origin	https://github.com/schacon/ticgit (fetch)
origin	https://github.com/schacon/ticgit (push)

// 如果你的远程仓库不止一个，该命令会将它们全部列出
$ git remote -v
bakkdoor  https://github.com/bakkdoor/grit (fetch)
bakkdoor  https://github.com/bakkdoor/grit (push)
cho45     https://github.com/cho45/grit (fetch)
cho45     https://github.com/cho45/grit (push)
defunkt   https://github.com/defunkt/grit (fetch)
defunkt   https://github.com/defunkt/grit (push)
koke      git://github.com/koke/grit.git (fetch)
koke      git://github.com/koke/grit.git (push)
origin    git@github.com:mojombo/grit.git (fetch)
origin    git@github.com:mojombo/grit.git (push)
```
#### 2.5.2 添加远程仓库 `git remote add <shortname> <url>`
添加一个新的远程 Git 仓库，同时指定一个你可以轻松引用的简写：
```
$ git remote
origin
$ git remote add pb https://github.com/paulboone/ticgit
$ git remote -v
origin	https://github.com/schacon/ticgit (fetch)
origin	https://github.com/schacon/ticgit (push)
pb	https://github.com/paulboone/ticgit (fetch)
pb	https://github.com/paulboone/ticgit (push)
```
现在你可以在命令行中使用字符串 `pb` 来代替整个 URL，`git fetch pb`

#### 2.5.3 从远程仓库中拉取与拉取 `git fetch / pull`
`git fetch [remote-name]` 命令会将数据拉取到你的本地仓库 — 它并不会自动合并或修改你当前的工作。 当准备好时你必须手动将其合并入你的工作。

如果你有一个分支设置为跟踪一个远程分支（阅读下一节与 [Git 分支](https://git-scm.com/book/zh/v2/ch00/ch03-git-branching) 了解更多信息），可以使用 `git pull` 命令来**自动的抓取然后合并远程分支**到当前分支。

默认情况下，git clone 命令会自动设置本地 master 分支跟踪克隆的远程仓库的 master 分支（或不管是什么名字的默认分支）。

#### 2.5.4 推送到远程仓库 `git push [remote-name] [branch-name]`
当你想分享你的项目时，必须将其推送到上游网络。

```
git push [remote-name] [branch-name]
```

当你想要将 `master` 分支推送到 `origin` 服务器时（再次说明，克隆时通常会自动帮你设置好那两个名字），那么运行这个命令就可以将你所做的备份到服务器：

```
$ git push origin master
$ git push origin develop

```
1. 只有当你有所克隆服务器的写入权限，并且之前没有人推送过时，这条命令才能生效。 
2. 当你和其他人在同一时间克隆，他们先推送到上游然后你再推送到上游，你的推送就会毫无疑问地被拒绝。 
3. 你必须先将他们的工作拉取下来并将其合并进你的工作后才能推送。 阅读 [Git 分支](https://git-scm.com/book/zh/v2/ch00/ch03-git-branching) 了解如何推送到远程仓库服务器的详细信息。

#### 2.5.5 查看某个远程仓库 `git remote show [remote-name]`

```
$ git remote show origin
* remote origin
  URL: https://github.com/my-org/complex-project
  Fetch URL: https://github.com/my-org/complex-project
  Push  URL: https://github.com/my-org/complex-project
  HEAD branch: master
  Remote branches:
    master                           tracked
    dev-branch                       tracked
    markdown-strip                   tracked
    issue-43                         new (next fetch will store in remotes/origin)
    issue-45                         new (next fetch will store in remotes/origin)
    refs/remotes/origin/issue-11     stale (use 'git remote prune' to remove)
  Local branches configured for 'git pull':
    dev-branch merges with remote dev-branch
    master     merges with remote master
  Local refs configured for 'git push':
    dev-branch                     pushes to dev-branch                     (up to date)
    markdown-strip                 pushes to markdown-strip                 (up to date)
    master                         pushes to master                         (up to date)
```
#### 2.5.6 远程仓库的移除与重命名 `git remote rm / rename`

```
$ git remote rename pb paul
$ git remote
origin
paul

$ git remote rm paul
$ git remote
origin
```

### 2.6 Git 基础 - 打标签
#### 2.6.1 列出标签 `git tag`
```
$ git tag
v0.1
v1.3
```
使用特定的模式查找标签：
```
$ git tag -l 'v1.8.5*'
v1.8.5
v1.8.5-rc0
v1.8.5-rc1
v1.8.5.1
v1.8.5.2
```
#### 2.6.2 创建标签
Git 使用两种主要类型的标签：
**轻量标签**（lightweight）:`git tag tag_des`
**附注标签**（annotated）:`git tag -a v1.0 -m 'tag_des'`


轻量标签：很像一个不会改变的分支——它只是一个特定提交的引用。

附注标签：是存储在 Git 数据库中的一个完整对象。 它们是可以被校验的；其中包含打标签者的名字、电子邮件地址、日期时间；还有一个标签信息；并且可以使用 GNU Privacy Guard （GPG）签名与验证。

通常建议创建附注标签，这样你可以拥有以上所有信息；但是如果你只是想用一个临时的标签，或者因为某些原因不想要保存那些信息，轻量标签也是可用的。

#### 2.6.3 附注标签
```
$ git tag -a v1.4 -m "my version 1.4"
$ git tag
v0.1
v1.3
v1.4
```
`-m` 选项指定了一条将会存储在标签中的信息。 如果没有为附注标签指定一条信息，Git 会运行编辑器要求你输入信息。

通过使用 git show 命令可以看到标签信息与对应的提交信息：
```
$ git show v1.4
tag v1.4
Tagger: Ben Straub <ben@straub.cc>
Date:   Sat May 3 20:19:12 2014 -0700

my version 1.4

commit ca82a6dff817ec66f44342007202690a93763949
Author: Scott Chacon <schacon@gee-mail.com>
Date:   Mon Mar 17 21:52:11 2008 -0700

    changed the version number
```
输出显示了打标签者的信息、打标签的日期时间、附注信息，然后显示具体的提交信息。

#### 2.6.4 轻量标签

轻量标签本质上是将提交校验和存储到一个文件中——没有保存任何其他信息。 创建轻量标签，不需要使用 -a、-s 或 -m 选项，只需要提供标签名字：
```
$ git tag v1.4-lw
$ git tag
v0.1
v1.3
v1.4
v1.4-lw
```
这时，如果在标签上运行 git show，你不会看到额外的标签信息。 命令只会显示出提交信息：
```
$ git show v1.4-lw
commit ca82a6dff817ec66f44342007202690a93763949
Author: Scott Chacon <schacon@gee-mail.com>
Date:   Mon Mar 17 21:52:11 2008 -0700

    changed the version number
```

#### 2.6.5 后期打标签

你也可以对过去的提交打标签。 假设提交历史是这样的：
```
$ git log --pretty=oneline
15027957951b64cf874c3557a0f3547bd83b3ff6 Merge branch 'experiment'
a6b4c97498bd301d84096da251c98a07c7723e65 beginning write support
0d52aaab4479697da7686c15f77a3d64d9165190 one more thing
6d52a271eda8725415634dd79daabbc4d9b6008e Merge branch 'experiment'
0b7434d86859cc7b8c3d5e1dddfed66ff742fcbc added a commit function
4682c3261057305bdd616e23b64b0857d832627b added a todo file
166ae0c4d3f420721acbb115cc33848dfcc2121a started write support
9fceb02d0ae598e95dc970b74767f19372d61af8 updated rakefile
964f16d36dfccde844893cac5b347e7b3d44abbc commit the todo
8a5cbc430f1a9c3d00faaeffd07798508422908a updated readme
```
现在，假设在 v1.2 时你忘记给项目打标签，也就是在 “updated rakefile” 提交。 你可以在之后补上标签。 要在那个提交上打标签，你需要在命令的末尾指定提交的校验和（或部分校验和）：
```
$ git tag -a v1.2 9fceb02
```
可以看到你已经在那次提交上打上标签了：
```
$ git tag
v0.1
v1.2
v1.3
v1.4
v1.4-lw
v1.5

$ git show v1.2
tag v1.2
Tagger: Scott Chacon <schacon@gee-mail.com>
Date:   Mon Feb 9 15:32:16 2009 -0800

version 1.2
commit 9fceb02d0ae598e95dc970b74767f19372d61af8
Author: Magnus Chacon <mchacon@gee-mail.com>
Date:   Sun Apr 27 20:43:35 2008 -0700

    updated rakefile
...
```

#### 2.6.6 共享标签

> 默认情况下，`git push` 命令并不会传送标签到远程仓库服务器上。

推送单个标签：`git push origin [tag_name]`
推送多个标签：`git push --tags`，把所有不在远程仓库服务器上的标签全部传送到那里。

#### 2.6.7 删除标签

删除本地仓库的标签：`git tag -d <tag_name>`
删除远程仓库的标签：`git push <remote> :refs/tags/<tag_name>`

#### 2.6.8 检出标签 `git checkout`
如果你想查看某个标签所指向的文件版本，可以使用 git checkout 命令.

虽然说这会使你的仓库处于“分离头指针（detacthed HEAD）”状态——这个状态有些不好的副作用。

在“分离头指针”状态下，如果你做了某些更改然后提交它们，标签不会发生变化，但你的新提交将不属于任何分支，并且将无法访问，除非确切的提交哈希。因此，如果你需要进行更改——比如说你正在修复旧版本的错误——这通常需要创建一个新分支：
```
$ git checkout -b version2 v2.0.0
Switched to a new branch 'version2'
```
当然，如果在这之后又进行了一次提交，`version2` 分支会因为这个改动向前移动，`version2` 分支就会和 `v2.0.0` 标签稍微有些不同，这时就应该当心了。

### 2.7 Git 基础 - Git 别名

#### 2.7.1 配置别名
通过 git config 文件来轻松地为每一个命令设置一个别名。 这里有一些例子你可以试试：
```
$ git config --global alias.co checkout
$ git config --global alias.br branch
$ git config --global alias.ci commit
$ git config --global alias.st status
```
为了解决取消暂存文件的易用性问题，可以向 Git 中添加你自己的取消暂存别名：
```
$ git config --global alias.unstage 'reset HEAD --'
```
这会使下面的两个命令等价：
```
$ git unstage fileA
$ git reset HEAD -- fileA
```
再例如，打印日志别名：
```
git config --global alias.lg "log --color --graph --pretty=format:'%Cred%h%Creset -%C(yellow)%d%Creset %s %Cgreen(%cr) %C(bold blue)<%an>%Creset' --abbrev-commit"
```
#### 2.7.2 配置文件

配置Git的时候，加上 `--global` 是针对当前用户起作用的，如果不加，那只针对当前的仓库起作用。

每个仓库的Git配置文件都放在 `.git/config` 文件中。

而当前用户的Git配置文件放在用户主目录下的一个隐藏文件 `.gitconfig` 中。

可进行增删改查操作。

```
$ cat .gitconfig
[alias]
    co = checkout
    ci = commit
    br = branch
    st = status
[user]
    name = Your Name
    email = your@email.com
```

## 3 Git 分支

### 3.1 分支简介

**Git 保存的不是文件的变化或者差异，而是一系列不同时刻的文件快照。**

Git 仓库包含的对象：
1. 提交对象（包含着指向前述树对象的指针和所有提交信息）
2. 树对象（记录着目录结构和 blob 对象索引）
3. blob 对象（保存着文件快照）

 ![首次提交对象及其树结构](https://upload-images.jianshu.io/upload_images/7867366-da4a06083d454faa.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

做些修改后再次提交，那么这次产生的提交对象会包含一个指向上次提交对象（父对象）的指针。

![提交对象及其父对象](https://upload-images.jianshu.io/upload_images/7867366-41a15034fe7fbd26.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

**Git 的分支，其实本质上仅仅是指向提交对象的可变指针 HEAD。** 每次的提交操作中指针自动向前移动。

#### 3.1.1 分支创建 `git branch`

创建一个 testing 分支：
```
$ git branch testing
```
在 Git 中，`HEAD` 是一个指针，指向当前所在的本地分支。
`git branch` 命令仅仅创建 一个新分支，并不会自动切换到新分支中去。

#### 3.1.2 分支切换 `git checkout`

切换到一个已存在的分支：
```
$ git checkout testing
```
这样 `HEAD` 就指向 `testing` 分支了。

> **分支切换会改变你工作目录中的文件**
> 在切换分支时，一定要注意你工作目录里的文件会被改变。 如果是切换到一个较旧的分支，你的工作目录会恢复到该分支最后一次提交时的样子。 如果 Git 不能干净利落地完成这个任务，它将禁止切换分支。

由于 Git 的分支实质上仅是包含所指对象校验和（**长度为 40 的 SHA-1 值字符串**）的文件，所以它的创建和销毁都异常高效。 
创建一个新分支就相当于往一个文件中写入 **41 个字节**（40 个字符和 1 个换行符）。

快照和备份的区别

这与过去大多数版本控制系统形成了鲜明的对比。
它们在创建分支时，将所有的项目文件都复制一遍，并保存到一个特定的目录。 完成这样繁琐的过程通常需要好几秒钟，有时甚至需要好几分钟。所需时间的长短，完全取决于项目的规模。
而在 Git 中，任何规模的项目都能在瞬间创建新分支。 同时，由于每次提交都会记录父对象，所以寻找恰当的合并基础（译注：即共同祖先）也是同样的简单和高效。 这些高效的特性使得 Git 鼓励开发人员频繁地创建和使用分支。

### 3.2 Git 分支 - 分支的新建与合并

#### 3.2.1 新建分支
新建一个分支并同时切换到那个分支上，运行一个带有 `-b` 参数的 `git checkout` 命令：
```
$ git checkout -b iss53
```
它是下面两条命令的简写：
```
$ git branch iss53
$ git checkout iss53
```

#### 3.2.2 合并分支：
分为两种情况： 直接合并（直接上游） 和 三方合并（分叉）

直接合并
![直接合并](https://upload-images.jianshu.io/upload_images/7867366-609d06b7aeb0f6f3.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

![合并后](https://upload-images.jianshu.io/upload_images/7867366-75e3a0ca2c578d05.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

```
$ git checkout master
$ git merge hotfix
Updating f42c576..3a0874c
Fast-forward
 index.html | 2 ++
 1 file changed, 2 insertions(+)
```
> "快进（**fast-forward**）"这个词。 由于当前 master 分支所指向的提交是你当前提交（有关 hotfix 的提交）的 **直接上游**，所以 **Git 只是简单的将指针向前移动**。 

三方合并提交
![三方合并](https://upload-images.jianshu.io/upload_images/7867366-b5e88f35236d358b.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
 
![合并后](https://upload-images.jianshu.io/upload_images/7867366-dce734a3edfccb90.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

```
$ git checkout master
Switched to branch 'master'
$ git merge iss53
Merge made by the 'recursive' strategy.
index.html |    1 +
1 file changed, 1 insertion(+)
```

在这种情况下，你的开发历史从一个更早的地方开始分叉开来（diverged）。 因为，`master `分支所在提交并不是 `iss53` 分支所在提交的直接祖先，

Git 会使用两个分支的末端所指的快照（C4 和 C5）以及这两个分支的工作祖先（C2），做一个简单的三方合并。

需要指出的是，Git 会自行决定选取哪一个提交作为最优的共同祖先，并以此作为合并的基础。

#### 3.2.3 遇到冲突时的分支合并

如果你在两个不同的分支中，对**同一个文件**的**同一个部分**进行了**不同的修改**，Git 就没法干净的合并它们。

此时 Git 做了合并，但是没有自动地创建一个新的合并提交。

Git 会暂停下来，等待你去解决合并产生的冲突。

步骤：
1. `git status` 查看冲突文件
2. 手动合并，去除文件中 <<< === >>> 符号
3. `git add` 命令标记冲突已解决
4. `git commit` 完成合并提交

> `git mergetool` 使用图形化工具来解决冲突，Mac默认使用opendiff作为合并工具；如果你需要更加高级的工具来解决复杂的合并冲突，我们会在 [高级合并](https://git-scm.com/book/zh/v2/ch00/r_advanced_merging) 介绍更多关于分支合并的内容。

#### 3.2.4 删除分支：

```
$ git branch -d hotfix
Deleted branch hotfix (3a0874c).
```

### 3.3 Git 分支 - 分支管理

`git branch` 不添加任何参数运行，得到当前分支的一个列表：

```
$ git branch
  iss53
* master
  testing
```

注意 `master ` 分支前的 `*` 字符。（当前 HEAD 指针所指向的分支）

查看每一个分支的最后一次提交，可以运行 `git branch -v` 命令：

```
$ git branch -v
  iss53   93b412c fix javascript issue
* master  7a98805 Merge branch 'iss53'
  testing 782fd34 add scott to the author list in the readmes
```

查看全部分支，包括远程
```
git branch -a -v
```
查看哪些分支已经合并到当前分支：
```
git branch --merged
```
已经合并的通常可以删除，`git branch -d`。

查看所有包含未合并工作的分支：
```
$ git branch --no-merged
```
因为包含了未合并的工作，使用 `git branch -d` 会删除失败，可以使用 `-D` 选项强制删除它。

### 3.4 Git 分支 - 分支开发工作流

### 3.4.1 长期分支 / 短期分支

随着你的提交而不断右移的指针。 稳定分支的指针总是在提交历史中落后一大截，而前沿分支的指针往往比较靠前。

![渐进稳定分支的线性图](https://upload-images.jianshu.io/upload_images/7867366-f9420d7a7c86fbf7.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

![渐进稳定分支的流水线（“silo”）视图](https://upload-images.jianshu.io/upload_images/7867366-0f7854707b05bea9.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

用这种方法维护不同层次的稳定性。

一些大型项目还有一个 `proposed`（建议） 或 `pu`: `proposed updates`（建议更新）分支，它可能因包含一些不成熟的内容而不能进入 `next` 或者 `master` 分支。 

### 3.4.2 特性分支

特性分支对任何规模的项目都适用。 

特性分支是一种短期分支，它被用来实现单一特性或其相关工作。 

我们将会在 [分布式 Git](https://git-scm.com/book/zh/v2/ch00/ch05-distributed-git) 中向你揭示更多有关分支工作流的细节，因此，请确保你阅读完那个章节之后，再来决定你的下个项目要使用什么样的分支策略（branching scheme）。

### 3.5 Git 分支 - 远程分支

远程引用是对远程仓库的引用（指针），包括分支、标签等等。

`git ls-remote`：显式地获得远程引用的完整列表
`git remote show origin`：远程分支的更多信息

```
$ git ls-remote
From git@github.com:simplehych/HookApp.git
3f86145d9b4525ccde107fd758df064c4528f784	HEAD
3f86145d9b4525ccde107fd758df064c4528f784	refs/heads/master
3ce73ca9f59865f12773f80d179a62979e7ed57a	refs/heads/new
6befd4b557a6503e82a104528f24e4e65da51f1a	refs/heads/temp
7f1da708a731dac2dfa2f7815fa838ab944a450b	refs/tags/v0.1
dffb53d0f4f3a0c2ad25becc70beb8e792a28f5b	refs/tags/v0.1^{}

$ git remote show origin
* remote origin
  Fetch URL: git@github.com:simplehych/HookApp.git
  Push  URL: git@github.com:simplehych/HookApp.git
  HEAD branch: master
  Remote branches:
    master tracked
    new    tracked
    temp   tracked
  Local branches configured for 'git pull':
    master merges with remote master
    new    merges with remote new
    temp   merges with remote temp
  Local refs configured for 'git push':
    master pushes to master (fast-forwardable)
    new    pushes to new    (fast-forwardable)
    temp   pushes to temp   (up to date)
```

追踪远程分支是远程分支状态的引用。以 `(remote)/(branch)` 形式命名。

`git fetch origin`：抓取远程仓库**有**而本地**没有**的数据

#### 3.5.1 推送

` git push (remote) (branch)`

```
$ git push origin serverfix
Counting objects: 24, done.
Delta compression using up to 8 threads.
Compressing objects: 100% (15/15), done.
Writing objects: 100% (24/24), 1.91 KiB | 0 bytes/s, done.
Total 24 (delta 2), reused 0 (delta 0)
To https://github.com/schacon/simplegit
 * [new branch]      serverfix -> serverfix
```
 Git 自动将 `serverfix` 分支名字展开为 `refs/heads/serverfix:refs/heads/serverfix`，那意味着，“推送本地的 serverfix 分支来更新远程仓库上的 serverfix 分支。” 
我们将会详细学习 [Git 内部原理](https://git-scm.com/book/zh/v2/ch00/ch10-git-internals) 的 `refs/heads/` 部分，但是现在可以先把它放在儿。 
你也可以运行 `git push origin serverfix:serverfix`，它会做同样的事——也就是说“推送本地的 serverfix 分支，将其作为远程仓库的 serverfix 分支” 可以通过这种格式来推送本地分支到一个命名不相同的远程分支。 
如果并不想让远程仓库上的分支叫做 `serverfix`，可以运行 `git push origin serverfix:awesomebranch` 来将本地的 `serverfix` 分支推送到远程仓库上的 `awesomebranch` 分支。

> 如何避免每次输入密码
如果你正在使用 HTTPS URL 来推送，Git 服务器会询问用户名与密码。 默认情况下它会在终端中提示服务器是否允许你进行推送。如果不想在每一次推送时都输入用户名与密码，你可以设置一个 “credential cache”。 最简单的方式就是将其保存在内存中几分钟，可以简单地运行 git config --global credential.helper cache 来设置它。

要特别注意的一点是当抓取到新的远程跟踪分支时，本地不会自动生成一份可编辑的副本（拷贝）。 
换一句话说，这种情况下，不会有一个新的 serverfix 分支——只有一个不可以修改的 origin/serverfix 指针。

1. 可以运行 git merge origin/serverfix 将这些工作合并到当前所在的分支。 
2. 如果想要在自己的 serverfix 分支上工作，可以将其建立在远程跟踪分支之上：
```
$ git checkout -b serverfix origin/serverfix
Branch serverfix set up to track remote branch serverfix from origin.
Switched to a new branch 'serverfix'
```
这会给你一个用于工作的本地分支，并且起点位于 `origin/serverfix`。


新建远程分支
git push origin localbranch:localbranch

删除远程分支'

推送一个空分支到远程分支，其实就相当于删除远程分支：git push origin :localbranch
git push origin --delete localbranch

#### 3.5.2 跟踪分支


#### 3.5.3 拉取


#### 3.5.4 删除远程分支



















## 7 Git 工具

### 7.3 Git 工具 储藏与清理

有时，当你在项目的一部分上已经工作一段时间后，所有东西都进入了混乱的状态，而这时你想要切换到另一个分支做一点别的事情。 问题是，你不想仅仅因为过会儿回到这一点而为做了一半的工作创建一次提交。 针对这个问题的答案是 `git stash` 命令。

储藏会处理工作目录的脏的状态——即跟踪文件的修改与暂存的改动——然后将未完成的修改保存到一个栈上，而你可以在任何时候重新应用这些改动。

#### 7.3.1 储藏工作

现在想要切换分支，但是还不想要提交之前的工作；所以储藏修改。 将新的储藏推送到栈上
运行 `git stash` 或 `git stash save`

运行前后 `git status` 查看状态

```
// 有改动的状态
$ git status
Changes to be committed:
  (use "git reset HEAD <file>..." to unstage)
	modified:   index.html
Changes not staged for commit:
  (use "git add <file>..." to update what will be committed)
  (use "git checkout -- <file>..." to discard changes in working directory)
	modified:   lib/simplegit.rb

// 将新的储藏推送到栈上
$ git stash
Saved working directory and index state \
  "WIP on master: 049d078 added the index file"
HEAD is now at 049d078 added the index file
(To restore them type "git stash apply")

// 工作目录是干净的了
$ git status
# On branch master
nothing to commit, working directory clean
```

**在这时，你能够轻易地切换分支并在其他地方工作；你的修改被存储在栈上。**

要查看储藏的列表，可以使用 `git stash list`：

```
$ git stash list
stash@{0}: WIP on master: 049d078 added the index file
stash@{1}: WIP on master: c264051 Revert "added file_size"
stash@{2}: WIP on master: 21d80a5 added number to log
```

将最近储藏的工作重新应用：`git stash apply`
指定旧的储藏重新应用：`git stash apply stash@{2}`
如果不指定一个储藏，Git 认为指定的是最近的储藏。

`git stash apply --index` ???  index 同 stage 暂存

应用选项只会尝试应用暂存的工作——在堆栈上还有它。

移除储藏：`git stash drop`
移除的储藏的名字来移除它：`git stash drop stash@{0}`

应用储藏然后立即从栈上扔掉它：`git stash pop`

#### 7.3.2 创造性的储藏

1. `git stash save --keep-index`
不储藏任何你通过 `git add` 命令已经暂存的东西

2. `git stash save --include-untracked` 或 `-u`
储存任何创建的未跟踪文件

3. `git stash save --patch`
交互式的提示哪些储存，哪些改动需要保存在工作目录中

以上 save 均可以省略。

#### 7.3.3 从储藏创建一个分支

```
git stash branch testchanges
```

#### 7.3.3 清理工作目录

有一些通用的原因比如说为了移除由合并或外部工具生成的东西，或是为了运行一个干净的构建而移除之前构建的残留

`git clean` ,它被设计为从工作目录中**移除未被追踪的文件。如果你改变主意了，你也不一定能找回来那些文件的内容。

 `git stash --all` ，一个更安全的选项，来移除每一样东西并存放在栈中。

对于 `git clean -d` 有几个选项
`-n`：演习，告诉你将要移除什么
`-x`：包活移除` .gitiignore `忽略文件
`-i` / `interactive`：以交互模式运行 clean 命令

git stash 储藏-已添加暂存区的文件
git stash --all 储藏-已添加暂存区的文件 和 未跟踪的文件
git stash --keep-index 1.不储藏任何你通过git add已暂存的文件；经验证还包括以下两种情况 2. 不储藏从未提交的文件； 3. 仅包括曾经git commit 提交到Git仓库的 且 当前修改没有 git add 到暂存区的文件
git stash --include-untracked / git stash -u 储存-已经在暂存区的文件 + 未跟踪的文件
git stash show 查看储藏的内容

> 注意 `git stash apply` 应用之后，原先提交暂存的文件，会变成没有暂存的状态，需要重新暂存。可以用 `--index` 参数解决该问题。

具体描述如下：
注意 `dumbidea.txt` 文件，由开始的 `左M` 变成之后的 `右M`
```
$ git status -s
M  dumbidea.txt
A  dumbidea1.txt
 M dumbidea2.txt

$ git stash / git stash -u / git stash --keep-index / git stash --all

$ git stash apply

$ git status -s
 M dumbidea.txt
A  dumbidea1.txt
 M dumbidea2.txt
```

git stash --all 和 git stash -u 的区别？




















































## 参考资料
感谢以下文章作者
[git](https://git-scm.com/docs)


