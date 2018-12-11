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
[ ![Download](https://api.bintray.com/packages/ooftf/maven/docking-api/images/download.svg) ](https://bintray.com/ooftf/maven/docking-api/_latestVersion)
[ ![Download](https://api.bintray.com/packages/ooftf/maven/docking-compiler/images/download.svg) ](https://bintray.com/ooftf/maven/docking-compiler/_latestVersion)
``` gradle
 implementation 'com.ooftf:docking-api:1.0.0'
 annotationProcessor 'com.ooftf:docking-compiler:1.0.0'
```
## 添加组件Application
生成一个Java类实现IApplication接口添加注解 @com.ooftf.docking.annotation.Application
``` java
@com.ooftf.docking.annotation.Application
public class ModuleApp implements IApplication {
    @Override
    public void onCreate(Application application) {
        //TODO
    }

    @Override
    public void onLowMemory() {
        //TODO
    }

    @Override
    public void onTerminate() {
        //TODO
    }

    @Override
    public void attachBaseContext(Context context) {
       //TODO
    }
}
```
## 在项目的Applicaton的onCreate方法中调用
``` java
    public class App extends Application {
        @Override
        public void onCreate() {
            super.onCreate();
            Docking.init(this, true, ThreadUtil.getDefaultThreadPool());
        }

        @Override
        public void onTerminate() {
            Docking.notifyOnTerminate();
            super.onTerminate();
        }

        @Override
        public void onLowMemory() {
            Docking.notifyOnLowMemory();
            super.onLowMemory();
        }

        @Override
        protected void attachBaseContext(Context base) {
            Docking.notifyAttachBaseContext(base);
            super.attachBaseContext(base);

        }
    }
```