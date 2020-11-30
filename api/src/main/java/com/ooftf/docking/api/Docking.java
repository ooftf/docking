package com.ooftf.docking.api;

import android.app.Application;

import com.blankj.utilcode.util.ProcessUtils;

/**
 * @author ooftf
 * @email 994749769@qq.com
 * @date 2018/11/17 0017
 */
public class Docking {

    protected static boolean isDebug = false;

    public static void notifyOnCreate(Application application) {
        for (IApplication app : ApplicationManager.apps) {
            MainProcess  mainProgress = null;
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
}
