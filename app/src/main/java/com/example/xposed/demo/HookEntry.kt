package com.example.xposed.demo

import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

const val LOG_TAG = "XposedDemo"

class HookEntry : IXposedHookLoadPackage {

    // 加载一个应用的进程的时候，会调用这个方法
    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        // 如果你制定了多个scope，那么要通过lpparam判断，具体加载的是哪个应用
        if (lpparam.packageName == "android") {
            // 用XposedBridge打印的日志，可以在LSPosed管理器中看到日志记录
            XposedBridge.log("$LOG_TAG Loading Android")

            // 通过工具，找到要劫持的class
            val classToHook = XposedHelpers.findClass(
                "com.android.server.wm.ActivityStarter",
                // 这个classloader就是目标app的，用它才能定位到目标class
                lpparam.classLoader
            )
            XposedBridge.log("$LOG_TAG classToHook: $classToHook")

            // https://cs.android.com
            // 我们可以查阅AOSP代码，看下Activity启动流程
            // 此处我们hook以下ActivityStarter的启动方法
            // 原型如下：
            // private int executeRequest(Request request) {...}
            XposedBridge.hookAllMethods(
                // 类
                classToHook,
                // 要hook的方法名
                "executeRequest",
                object : XC_MethodHook() {
                    // AOP方法之一，在方法执行之前要做的事情，此处我们打印日志
                    // 提示：此处除了可以获取到method参数，返回值信息之外，还能修改返回值
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        super.beforeHookedMethod(param)

                        val request = param.args[0]
                        // 工具很方便
                        val intent = XposedHelpers.getObjectField(request, "intent")

                        // 打印日志，目的达到了
                        XposedBridge.log("$LOG_TAG Starting Activity: $intent")
                    }

                })

        }
    }

}