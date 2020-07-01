package com.ooftf.docking.api;

import android.app.Application;
import android.content.Context;

import org.jetbrains.annotations.NotNull;

/**
 * @author ooftf
 * @email 994749769@qq.com
 * @date 2018/11/17 0017
 */
public interface IApplication {
    void init(@NotNull Application application);

    void onCreate(@NotNull Application application);

    void attachBaseContext(@NotNull Context context);

    int getPriority();
}
