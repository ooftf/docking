package com.ooftf.docking.sample;

import android.app.Application;
import android.content.Context;

import com.ooftf.docking.api.Docking;

/**
 * @author ooftf
 * @email 994749769@qq.com
 * @date 2018/12/11 0011
 */
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
