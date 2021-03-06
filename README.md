# docking
组件化中，将Application生命周期通知到各个模块
# 目的
在组件化开发中，各个组件需要在Application生命周期中处理一些事情，通常我们会采用以下几种方式
1. 各个组件实现一个application的观察者接口，在主项目将他们实例化然后调用生命周期方法，缺点在于需要修改主项目的代码
2. 各个组件通过mate的方式将类名以字符串的形式传到主项目再使用反射的形式实例化，存在的问题：使用比较麻烦，如果类名改变需要手动修改类名字符串
# 解决方式
通过注解处理器，生成各个组件的application注册器，然后在application中扫描当前代码找到application注册器，通过反射实例化注册器，注册各个模块的applicaton

# 使用方式
## 引入
Api  [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.ooftf/docking-api/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.ooftf/docking-api)
Plugin  [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.ooftf/docking-plugin/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.ooftf/docking-plugin)

``` gradle
 在项目的build.gradle添加
 classpath 'com.github.ooftf:docking-plugin:2.1.3'
 在 app下build.gradle中添加
 apply plugin: 'docking-register'
 在组件中添加
 implementation 'com.github.ooftf:docking-api:3.0.0'
```
## 添加组件Application
``` java
public class ModuleApp implements IApplication {
 
    @Override
    public void onCreate() {
        //TODO
    }
    
    /**
    * 返回值越大优先级越高
    * @return 
    */
    @Override
    public int getPriority() {
        return 0;
    }
}
```
## 在项目的Applicaton的onCreate方法中调用
``` java
    public class App extends Application {
     
        @Override
        public void onCreate() {
            super.onCreate();
            Docking.notifyOnCreate();
        }
        
    }
```
## 混淆
    -keepclassmembers public class * implements com.ooftf.docking.api.IApplication{
        void onCreate(android.app.Application);
    }
