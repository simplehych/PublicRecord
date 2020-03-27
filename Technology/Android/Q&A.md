[TOC]
# Android开发常见问题
## JNI NDK问题
https://developer.android.com/ndk/guides/

1. Q：java.lang.UnsatisfiedLinkError: dlopen failed: "libxxx.so" is too small to be an ELF executable 

   A：重新覆盖(测试可以)；https://blog.csdn.net/shengyingpo/article/details/51538444 该链接中说查看Bmob文档貌似不是一个东西；
2. Q：Could not find method ndk() for arguments
    A：AndroidStudio2.2以上推荐使用Cmake方式，重新生成，将ndk放到defaultConfig下
    
    ```
    defaultConfig {
        ···        
        ndk {
            moduleName "libhello_jni"
            stl "stlport_static"
            ldLibs "log"
        }
    }
    ```
    
3. Q：Error: Your project contains C++ files but it is not using a supported native build system.
Consider using CMake or ndk-build integration with the stable Android Gradle plugin:
 https://developer.android.com/studio/projects/add-native-code.html
or use the experimental plugin:
 https://developer.android.com/studio/build/experimental-plugin.html.
 
    A：添加 `jni.srcDir=[]`
    ```
    sourceSets {
        main {
            jni.srcDirs = []
        }
    }
    ```

## transformNativeLibsWithStripDebugSymbolForGeneralDebug

Q：

```
Error:Execution failed for task ':app:transformNative_libsWithStripDebugSymbolForDebug'.
> java.lang.NullPointerException (no error message)
```

A：
1. 在build:gradle中将 classpath中版本改低 gradle 2.2.0以上可以对于NDK有些问题,故改成2.1.0，对应版本 https://developer.android.com/studio/releases/gradle-plugin
2. gradle.properties添加一句话: android.useDeprecatedNdk=true
3. 或将ndk改成低版本



