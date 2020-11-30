package com.ooftf.docking.api;

import android.app.Application;

import org.jetbrains.annotations.NotNull;

/**
 * @author ooftf
 * @email 994749769@qq.com
 * @date 2018/11/17 0017
 */
public interface IApplication {
    /**
     * Application onCreate
     *
     * @param application
     */
    void onCreate(@NotNull Application application);

    /**
     * 优先级  值越大优先级越高
     *
     * @return
     */
    int getPriority();
}
