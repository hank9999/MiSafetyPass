package com.github.hank9999.misafetypass

import com.hchen.hooktool.BaseHC
import com.hchen.hooktool.hook.IHook
import com.hchen.hooktool.log.XposedLog.logE
import com.hchen.hooktool.log.XposedLog.logI
import org.luckypray.dexkit.DexKitBridge
import org.luckypray.dexkit.wrap.DexMethod

class MiSafetyHook : BaseHC() {

    companion object {
        lateinit var mDexKit: DexKitBridge
    }

    private var cResultCall: Class<*>? = null

    override fun init() {
        // 获取调用充电保护设置方法
        try {
            cResultCall = mDexKit.findClass {
                matcher {
                    usingStrings("ClientApiRequest", "binderDied: ")
                }
            }.singleOrNull()?.getInstance(classLoader)

            logI(TAG, "mResultCall $cResultCall")
        } catch (e: Exception) {
            logE(TAG, "mResultCall $e")
        }

        if (cResultCall == null) {
            logE(TAG, "mResultCall is null, method not found")
            return
        }

        if (cResultCall?.constructors?.size != 1) {
            logE(TAG, "mResultCall constructor not found")
            return
        }

        val method = cResultCall?.constructors?.get(0)
        if (method == null) {
            logE(TAG, "mResultCall constructor not found")
            return
        }

        hook(method, object : IHook() {
            override fun before() {
                logI(TAG, "before, arg length:${argsLength()}")
                for (i in 0 until argsLength()) {
                    logI(TAG, "arg $i: ${getArgs(i)}")
                }
                if (argsLength() != 3) {
                    return
                }
                if (getArgs(0) == 10) {
                    setArgs(0, 11)
                    logI(TAG, "arg 0: 10 -> 11")
                }
            }
        })

        logI(TAG, "MiSafetyHook success")
    }

    fun Thread.isFromMethod(method: DexMethod): Boolean {
        return this.stackTrace.any { it.methodName == method.name && it.className == method.className }
    }
}