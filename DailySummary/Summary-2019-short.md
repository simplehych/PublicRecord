### 2019-short

* 2019-12-16 Monday
    * 全国人大：版本区分接口
    * 国新发布：Android版本适配 6.0-9.0

* 2019-12-17 Tuesday
    * 全国人大整改项目提测
    * 年终总结

* 2019-12-18 Wednesday
    *  X5内核替换、字体调整
    * 分享更新、验证
    * 增加资源混淆
    * git HEAD 游离问题 https://blog.csdn.net/u011240877/article/details/76273335
    * 内部存储/外部存储区别

* 2019-12-19 Thursday
    * 全国人大上线，渠道包，线上验证
    * 内部存储/外部存储及读写权限 总结

    未完成：
    
    * git HEAD detached 游离 及git用法总结


* 2019-12-20 Friday
    * git总结 https://git-scm.com/ 未完成


周总结

* 内部存储/外部存储
* git
        

2019-12-23 Monday

* Git 基础 - 撤销操作
    * git rm —cached 暂存区删除，添加到.gitignore
    * 覆盖提交 git commit —amend 配置别名 recommit
    * 取消暂存 git reset HEAD <file>  配置别名 unstage
    * 还原上次提交 git checkout -- <file> 配置别名 uncommit
        * 注意  上面的 -- 符号，文件位于末尾，用于和其他配置项分隔
* Git 基础 - 远程仓库的使用  git push <remote_name> ><branch_name>
    * 查看远程仓库 git remote
    * 添加远程仓库 git remote add <short_name> <url>
    * 远程仓库拉取 git fetch master / git pull
    * 推送到远程仓库 git push origin master
    * 查看某个远程仓库 git remote show origin
    * 远程仓库的移除 git remote rm master
    * 远程仓库的重命名 git remote rename master my-master
* Git 基础 - 打标签
    * 列出标签 git tag
    * 查找标签 git tag -l ‘v1.0'
    * 创建轻量标签 git tag ‘v1.0'
    * 创建附注标签 git tag -a v1.0 -m ‘version 1.0’ 建议使用
    * 查看标签 git show v1.0
    * 后期打标签 git tag -a ‘v0.1’  shar-1
    * 推送单个标签 git push origin v1.0
    * 推送多个标签 git push —tags
    * 删除本地标签 git tag -d v1.0
    *  删除远程 git push origin :refs/tags/v1.0
    * 检出标签 git checkout v1.0 
        * 问题：会导致HEAD Detach 游离问题，需要重新创建分支解决
* Git 基础 - Git别名
    * 配置别名 git config —global alias.ci commit
    * 配置文件
        * 全局配置放在～/.gitconfig
        * 本地项目的配置在 .git/config
* Git 分支 - 简介
    * 包含对象
        * 提交对象：父对象的指针，所提交信息
        * 树文件夹对象：记录目录结构，blob对象的索引
        * 每个文件 blob 对象：文件快照 
    * 分支创建 git branch develop
    * 分支切换 git checkout develop

Book 
人性的弱点
Chapter02 - six ways to make people like you
section 01 广受欢迎的秘密
原则1 建立对他人的兴趣，真心实意的关心他人
Sample：
1. 小狗
2. 如果想写出好的作品，必须对人感兴趣
Tips：
1. 每个都对欣赏自己的人感兴趣

2019-12-24 Tuesday
    
* Git 工具 - 储藏和清理
    * 储藏 git stash 或 git stash save
    * 查看储藏的列表 git stash list
    * 重新应用储藏的工作 git stash apply
    * 重新应用旧的储藏 git stash apply stash@{2}
    * 应用储藏 - 已经暂存的文件恢复暂存状态 git stash apply —index
    * 移除储藏 git stash drop
    * 移除储藏的名字来移除它 git stash drop stash@{2}
    * 应用储藏并从栈上扔掉 git stash pop
    * 不储藏已暂存的东西 git stash —keep-index
    * 储存未跟踪的文件 git stash —include-untracked / git stash -u
    * 交互式提示储存 git stash —patch
    * 从储藏中创建一个分支 git stash branch
* Git 分支
    * 未终结
* Git 数据模型
    * 未终结


* 上述命令未复习完

* 总结验证了以下命令的区别：
    * git stash
    * git stash —all
    * git stash —keep-index
    * git stash —include-untracked / git stash -u

    * git stash apply —index

* 未回顾英语
* 未读书

2019-12-25 Wednesday

* 总结昨日内容
    * Git 分支
    * Git 数据模型
    * 回顾英语 12-24

* 项目 - 领导留言板整改
    * 启动弹窗
    * 关于页面
    * 注销账号
    * 意见反馈 - ing

* 昨日总结
    * Git 数据模型
        * 储存数据的方式：存放着不同对象object的hash表
            * hash表的键为hash值，保证不重复
            * 对象有三种：blob、tree、commit()
                * blob: 文件（blob对象）
                * tree: 目录（tree对象）、多个文件（blob对象）
                * commit: 目录（tree对象）、父parent提交点（commit对象）、作者和提交者的信息
        * 查看方式  git cat-file
            * 查看项目所有对象： git cat-file --batch-check --batch-all-objects
            * 查看某一个对象：git cat-file -p 16f4c48319f2bb70f5289fc1ea325dabc9d14729
        * Branch、Head、Tag
            * branch 指针，指向某个commit文件的hash值，可移动
                * cat ./.git/refs/heads/master = 9a6390e648063c3b814849e33af2395c178f61c1 
            * HEAD 指向一个 branch
                * cat .git/HEAD =  ref: refs/heads/master
                * 当不指向一个Branch，如某一次提交，会HEAD游离，需要重新创建分支，否则切换分支会丢失
            * Tag 指针，静态的 branch
                * cat ./.git/refs/tags = 9a6390e648063c3b814849e33af2395c178f61c1
                * 不随着HEAD移动
        * Index空间 == stage空间 == cache空间
            * 停止跟踪某个文件 git rm —cache xxx
            * 重新提交到仓库 git reset —soft [commit 2],只是将HEAD移到commit 2,Index空间文件没有减少，重新提交说明即可
    * Git分支 - 未总结

* 英语
    * dumb 哑巴，愚蠢
    * stash 储藏
    * checkout 查看，结账
    * discard 抛弃
* 未读书

2019-12-26 Thursday

* 项目 - 领导留言板整改
    * 注销流程更改
    * 意见反馈更改


* GIt 日志
    * git log
    * git log —oneline -2
    * git log --stat
    * git log -p file_name  / git log —patch 补丁的形式
        * 补丁信息说明
diff --git a/RouterHelper.java b/RouterHelper.java  // 文件名称
index 6b1bb8e..bab8c03 100644 //版本hash值
--- a/common/CommonService/src/main/java/com/peopletech/router/RouterHelper.java
+++ b/common/CommonService/src/main/java/com/peopletech/router/RouterHelper.java
@@ -54,6 +54,7 @@  //本次开始行号，本次一共几行


+   public static 
-   private static 
    * git log -p commit_id
    * git show 相当于git log -p，显示差异 默认一条，可以添加 -2、--stat等参数

* GIt 分支未终结
* 未读书
* 英语暂无


2019-12-27 Friday

* [Shell 讲习班](https://classroom.udacity.com/courses/ud206)
    * command options arguments
    * man - manual pages
    * echo
    * ls cd
    * pwd
    * glob模式
        * shell 通配符 ，eg：*, ?, [list], [^list], [!list], {str1, str2, ...}
    * shell 专用字符集
        * eg：[:space:], [:print:], [:ctrl:]
    * 元字符
        * =, $, |, !
    * 整理文件
        * touch / vim   rm
        * mkdir rmdir
        * mv 移动，有重命名的效果
    * 下载 curl
        * -o  output
        * -L location
    * 查看文件
        * cat
        * less / more
    * 搜索 grep 和管道 |
        * curl -L http://xxx | grep keyword | wc -l | less
    * Shell 变量 和 环境变量

* 英语
    * peer
    * manual
    * synopsis - summary
    * identical 相同的

* GIt 分支未终结
* 未读书


2019-12-28 Saturday

未终结


2019-12-19 Sunday December
* 环境变量 和 PATH 关系
* Shell 十三问  http://wiki.jikexueyuan.com/list/linux/


2019 -12 - 19 Week Summary

知识回顾

* Git
    * Git 基础
        * 撤销操作
            * git rm —cached 暂存区删除，工作目录保留
            * git reset HEAD <file> 取消暂存
            * git checkout —<file> 还原到上次提交
            * git commit —amend 覆盖提交
        * 远程仓库
            * git remote 查看
            * git remote add <short_name> <url>
            * git remote rm master
            * git remote rename master my_master
            * git pull / git fetch
            * git push
            * git remote show origin
        * 打标签
            * git tag
            * git tag ’tag_name'
            * git tag -a ’tag_name'
            * git tag -a ’tag_name’ hash_value
            * git push —tags
            * git tag -d tag_name
            * git push origin :refs/tags/tag_name
            * git checkout tag_name
                * 会导致HEAD Detach游离，重新创建分支解决
        * 别名
            * git config —global alias.ci commit
            * 文件位置  1. 全局 ~/.gitconfig    2.本地项目 .git/config
    * Git 工具
        * 储藏和清理
            * git stash list
            * git stash / git stash save
            * git stash apply / git stash drop / git stash pop
            * git stash apply —index  说明：默认所有的储藏恢复后都是未暂存状态， --index可以保持之前的暂存状态
            * git stash —keep-index
            * git stash -u / git stash —include-untracked
            * git stash —patch
            * git stash branch
    * Git 数据模型
        * 储存数据方式
            * 存放着不同对象的hash表
                * blob、tree、commit
        * 如何查看
            * git cat-file —batch-check —batch-all
            * git cat-file -p 16f4c48319f2bb70f5289fc1ea325dabc9d14729  /patch
        * Branch / Head / Tag
            * Branch cat ./.git/refs/heads/master = 9a6390e648063c3b814849e33af2395c178f61c1 
            * Head cat .git/HEAD =  ref: refs/heads/master
            * Tag cat ./.git/refs/tags = 9a6390e648063c3b814849e33af2395c178f61c1
        * Index 空间 暂存空间
            * 同 stage 空间 = cache 空间
            * git rm —cache xxx
            * git reset —soft [commit2]
    * Git 日志
        * git log
        * git log - p file_name / git log —patch 
        * git log -p commit_id
        * git show  = git log -p
* Shell 基础
    * 常用命令
        * man / echo / ls / cd / pwd / touch / mkdir / rm / rmdir / mv / cp / curl /cat / less / grep | 搜索和管道
    * 环境变量和PATH关系
        * Shell 变量和环境变量
        * 环境变量，如PATH环境变量、CLASSPATCH环境变量
    * [^ ] 和 [! ] 的区别
        * 都是范围外的意思
        * ^ 是 regexp，! 是 wildcard

2019-12-30 December Monday

* 留言板意见反馈接口调试

2019-12-31 December Tuesday

* 留言板注销接口
* 国新发布注销、反馈


