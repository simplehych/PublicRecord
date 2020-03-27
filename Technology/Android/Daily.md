# Android 日常记录

* Rxjava2使用 操作符 contact 导致不连续执行，需要调用onComplete
https://blog.csdn.net/carson_ho/article/details/78455349

    ```
    List<Observable<String>> observableList = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                String str = String.valueOf(i);
                Observable<String> observable = Observable.create(new ObservableOnSubscribe<String>() {
                    @Override
                    public void subscribe(ObservableEmitter<String> e) throws Exception {
                        Log.i("contact", "subscribe---");
                        e.onNext(str);
                        /**
                         * 需要调用onComplete()，否则不往下执行，推荐使用fromCallable或者fromIterable
                         **/
                        e.onComplete();
                    }
                });
                observableList.add(observable);
            }
            
    Observable.contact(observableList)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<String>() {
                        @Override
                        public void onSubscribe(Disposable d) {
                            Log.i("contact", " onSubscribe--- ");
                        }
    
                        @Override
                        public void onNext(String s) {
                            Log.i("contact", " onNext--- " + s);
                        }
    
                        @Override
                        public void onError(Throwable e) {
                            Log.i("contact", " onError--- ");
                        }
    
                        @Override
                        public void onComplete() {
                            Log.i("contact", " onComplete--- ");
                        }
                    });
    ```

* DownloadManager  https://www.cnblogs.com/zhaoyanjun/p/4591960.html
* epub http://readium.org/news/news/
* 依赖去重 https://blog.csdn.net/qq_24216407/article/details/72842614
  http://www.paincker.com/gradle-dependencies
  
* gradle学习 https://blog.csdn.net/u014608640/article/details/77991899
* 定时器的使用 timer
* VPN 分流：https://blog.csdn.net/littlewhite1989/article/details/39718235  https://blog.csdn.net/songques/article/details/44698401
* android 源码编译
https://mp.weixin.qq.com/s/FFCeraDXcoCP8eJtxS3KWA
https://github.com/anggrayudi/android-hidden-api
https://www.jianshu.com/p/9bf96f648884
https://www.jianshu.com/p/367f0886e62b




1 音视频无缝切换到详情页-待定
https://www.jianshu.com/p/4db96418f32b
https://blog.csdn.net/qq_22706515/article/details/54847822
https://github.com/danylovolokh/VideoPlayerManager

2 状态栏和返回搭配使用

3 拍照录像

4 异步断点续传
https://www.cnblogs.com/zhujiabin/p/7139712.html
https://blog.csdn.net/tongfj/article/details/53736046
https://mp.weixin.qq.com/s?__biz=MzA5MzI3NjE2MA==&mid=2650242663&idx=1&sn=6877c3db4dab01547454a30991565321&chksm=88638f08bf14061e775e23fe84c1641c97eff0952f6a92d34f62e428cea8ed534203e60a4b40&scene=38#wechat_redirect

5 组件化架构

6 使用Android-ObservableScrollView

7 即时消息机制
https://www.2cto.com/kf/201407/320682.html
http://comeonbabye.iteye.com/blog/1325357

8 图集
https://blog.csdn.net/shenshibaoma/article/details/78468810
https://www.jianshu.com/p/297e6af61ee6
https://github.com/maning0303/MNImageBrowser
https://github.com/githubwing/DragPhotoView

10 注解
http://www.cnblogs.com/gmq-sh/p/4798194.html
	反射  继承的方法发射不出来
11 抽象方法默认返回一个自己想要的值 而不是null

12 继承关系 后up先执行 后down后执行

13 键盘问题
https://blog.csdn.net/qq_32258857/article/details/77851464

https://www.jianshu.com/p/4127b7e091c0

14 sp null 问题
https://blog.csdn.net/tracyxia716/article/details/52967110

15 Glide 设计模式
https://www.jianshu.com/p/7ce7b02988a4

16 popwidow
https://blog.csdn.net/u014616515/article/details/52886606/

17 转义字符切割

不能定义flag，只能在split里面写"[|]"
https://bbs.csdn.net/topics/391054795
https://blog.csdn.net/qq_31008549/article/details/77414488
https://blog.csdn.net/yinianjian2017/article/details/70159327

18.收藏逻辑后续补充时序图
https://www.cnblogs.com/silent2012/archive/2011/09/07/2169946.html
http://www.uml.org.cn/oobject/201211231.asp


19. litepal 的布尔值和0的问题，统一使用String

20.
private boolean isCover(View view) {
    boolean cover = false;
    Rect rect = new Rect();
    cover = view.getGlobalVisibleRect(rect);
    if (cover) {
        if (rect.width() >= view.getMeasuredWidth() && rect.height() >= view.getMeasuredHeight()) {
            return !cover;
        }
    }
    return true;
}
销毁之后获取失败，不使用此方法

21 给Activity添加回调监听

22 键盘沉浸式有冲突

23 The specified child already has a parent
	1.	ImageView imageView = (ImageView) viewList.get(arg1%viewList.size());  
	2.	        if(imageView.getParent()!=null){  
	3.	            ((ViewPager)imageView.getParent()).removeView(imageView);  
	4.	        }  

24 glide 无法设置tag

25 dialog设置全屏 进行自定义 设置背景

26 at android.media.MediaMetadataRetriever._setDataSource(Native Method)

27 视频压缩
https://blog.csdn.net/qq_35373333/article/details/77765605
http://www.jb51.net/article/133902.htm
https://github.com/mabeijianxi/small-video-record/blob/master/document/README_CH.md


9 context.getCacheDir()和context.getFilesDir()的区别


