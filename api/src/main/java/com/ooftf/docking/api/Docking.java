package com.ooftf.docking.api;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author ooftf
 * @email 994749769@qq.com
 * @date 2018/11/17 0017
 */
public class Docking {

    protected static Application mApplication;
    protected static boolean isDebug;
    protected static ThreadPoolExecutor mExecutor;
    protected static List<IApplication> applications = new ArrayList<>();

    /**
     *
     * 一定要在 onCreate 中调用
     *
     * @param application
     * @param isDebug
     * @param executor
     */
    public static void init(Application application, boolean isDebug, ThreadPoolExecutor executor) {
        mApplication = application;
        Docking.isDebug = isDebug;
        mExecutor = executor;


        try {
            Set<String> appSet;
            /**
             * 找到指定包下的所有类
             */
            appSet = ClassUtil.getFileNameByPackageName(application, Consts.REGISTER_PACKAGE_NAME);
            /**
             * 过滤出固定格式的注册器
             */
            for (String className : appSet) {
                if (className.startsWith(Consts.REGISTER_PACKAGE_NAME + Consts.DOT + Consts.SDK_NAME + Consts.SEPARATOR + Consts.SUFFIX_APP_SHIP)) {
                    // This one of root elements, load root.
                    ((IAppShipRegister) (Class.forName(className).getConstructor().newInstance())).register(applications);
                }
            }
            notifyOnCreate();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    private static void notifyOnCreate() {
        for (IApplication app : applications) {
            app.onCreate(mApplication);
        }

    }

    public static void notifyOnLowMemory() {
        for (IApplication app : applications) {
            app.onLowMemory();
        }

    }

    public static void notifyOnTerminate() {
        for (IApplication app : applications) {
            app.onTerminate();
        }

    }

    public static void notifyAttachBaseContext(Context context) {
        for (IApplication app : applications) {
            app.attachBaseContext(context);
        }

    }
}
