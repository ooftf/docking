package com.ooftf.docking.api;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.blankj.utilcode.util.ProcessUtils;

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
            MainProcess mainProgress = null;
            try {
                mainProgress = app.getClass().getMethod("init", Application.class).getAnnotation(MainProcess.class);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
            if (mainProgress != null) {
                if (ProcessUtils.isMainProcess()) {
                    app.init(application);
                }
            } else {
                app.init(application);
            }
        }

    }

    public static void notifyOnCreate(Application application) {
        for (IApplication app : ApplicationManager.apps) {
            MainProcess mainProgress = null;
            try {
                mainProgress = app.getClass().getMethod("onCreate", Application.class).getAnnotation(MainProcess.class);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
            if (mainProgress != null) {
                if (ProcessUtils.isMainProcess()) {
                    app.onCreate(application);
                }
            } else {
                app.onCreate(application);
            }

        }

    }


    public static void notifyAttachBaseContext(Context context) {
        for (IApplication app : ApplicationManager.apps) {
            MainProcess mainProgress = null;
            try {
                mainProgress = app.getClass().getMethod("attachBaseContext", Context.class).getAnnotation(MainProcess.class);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
            if (mainProgress != null) {
                if (ProcessUtils.isMainProcess()) {
                    app.attachBaseContext(context);
                }
            } else {
                app.attachBaseContext(context);
            }
        }

    }
}
