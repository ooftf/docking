package com.ooftf.docking.api;

import android.app.Application;
import android.content.Context;

/**
 * @author ooftf
 * @email 994749769@qq.com
 * @date 2018/11/17 0017
 */
public class Docking {

    protected static Application mApplication;
    protected static boolean isDebug;

    /**
     * 在Application 的 非静态代码块 中调用
     *
     * @param application
     * @param isDebug
     */
    public static void init(Application application, boolean isDebug) {
        mApplication = application;
        Docking.isDebug = isDebug;
        for (IApplication app : ApplicationManager.apps) {
            app.init(mApplication);
        }
    }

    public static void notifyOnCreate() {
        for (IApplication app : ApplicationManager.apps) {
            app.onCreate();
        }

    }

    public static void notifyOnLowMemory() {
        for (IApplication app : ApplicationManager.apps) {
            app.onLowMemory();
        }

    }

    public static void notifyOnTerminate() {
        for (IApplication app : ApplicationManager.apps) {
            app.onTerminate();
        }

    }

    public static void notifyAttachBaseContext(Context context) {
        for (IApplication app : ApplicationManager.apps) {
            app.attachBaseContext(context);
        }

    }
}
