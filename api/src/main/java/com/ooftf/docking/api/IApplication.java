package com.ooftf.docking.api;

import android.app.Application;
import android.content.Context;

/**
 * @author ooftf
 * @email 994749769@qq.com
 * @date 2018/11/17 0017
 */
public interface IApplication {
    void onCreate(Application application);

    void onLowMemory();

    void onTerminate();

    void attachBaseContext(Context context);
}
