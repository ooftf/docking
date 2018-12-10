package com.ooftf.docking.sample;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.ooftf.docking.api.IApplication;

/**
 * @author ooftf
 * @email 994749769@qq.com
 * @date 2018/12/11 0011
 */
@com.ooftf.docking.annotation.Application
public class ModuleApp implements IApplication {
    @Override
    public void onCreate(Application application) {
        Log.e("ModuleApp","onCreate");
    }

    @Override
    public void onLowMemory() {
        Log.e("ModuleApp","onLowMemory");
    }

    @Override
    public void onTerminate() {
        Log.e("ModuleApp","onTerminate");
    }

    @Override
    public void attachBaseContext(Context context) {
        Log.e("ModuleApp","attachBaseContext");
    }
}
