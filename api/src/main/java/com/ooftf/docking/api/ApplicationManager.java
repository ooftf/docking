package com.ooftf.docking.api;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author ooftf
 * @email 994749769@qq.com
 * @date 2018/11/19 0019
 */
class ApplicationManager {
    protected static List<IApplication> apps = new ArrayList<>();

    static {
        init();
        Collections.sort(apps, new Comparator<IApplication>() {
            @Override
            public int compare(IApplication o1, IApplication o2) {
                return o2.getPriority() - o1.getPriority();
            }
        });
        if (Docking.isDebug) {
            Log.e("Docking", "register-sort::" + apps.toString());
        }
    }

    public static void init() {

    }

    /**
     * 添加IApplication 模块
     */
    public static void register(IApplication proxy) {
        if (Docking.isDebug) {
            Log.e("Docking", "register::" + proxy.toString());
        }
        apps.add(proxy);

    }
}
