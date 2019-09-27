package com.ooftf.android.register

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
/**
 * 自动注册插件入口
 * @author billy.qi
 * @since 17/3/14 17:35
 */
public class RegisterPlugin implements Plugin<Project> {
    public static final String EXT_NAME = 'docking-register'

    @Override
    public void apply(Project project) {
        /**
         * 注册transform接口
         */
        def isApp = project.plugins.hasPlugin(AppPlugin)
        if (isApp) {
            println 'project(' + project.name + ') apply docking-register plugin'
            def android = project.extensions.getByType(AppExtension)
            def transformImpl = new RegisterTransform(project)
            android.registerTransform(transformImpl)
            project.afterEvaluate {
                init(project, transformImpl)//此处要先于transformImpl.transform方法执行
            }
        }
    }

    static void init(Project project, RegisterTransform transformImpl) {
        AutoRegisterConfig config = new AutoRegisterConfig()
        def item = new HashMap()
        item["scanInterface"] = 'com.ooftf.docking.api.IApplication'
        item["codeInsertToClassName"] = 'com.ooftf.docking.api.ApplicationManager'
        item["codeInsertToMethodName"] = 'init'
        item["registerMethodName"] = 'register'
        config.registerInfo.add(item)
        config.project = project
        config.convertConfig()
        transformImpl.config = config
    }

}
