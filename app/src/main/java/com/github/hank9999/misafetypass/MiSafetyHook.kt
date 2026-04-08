package com.github.hank9999.misafetypass

import com.hchen.hooktool.BaseHC
import com.hchen.hooktool.hook.IHook
import com.hchen.hooktool.log.XposedLog.logE
import com.hchen.hooktool.log.XposedLog.logI
import org.luckypray.dexkit.DexKitBridge

class MiSafetyHook : BaseHC() {

    companion object {
        lateinit var mDexKit: DexKitBridge
    }

    override fun init() {
        val constructor = try {
            mDexKit.findMethod {
                matcher {
                    name("<init>")
                    paramCount(3)
                    paramTypes("int", null, "com.xiaomi.security.xsof.IMiSafetyDetectCallback")
                    declaredClass {
                        interfaces("android.os.IBinder\$DeathRecipient")
                    }
                }
            }.singleOrNull()?.getMethodInstance(classLoader)
        } catch (e: Exception) {
            logE(TAG, "findMethod failed: $e"); null
        }

        if (constructor == null) {
            logE(TAG, "constructor not found"); return
        }

        hook(constructor, object : IHook() {
            override fun before() {
                if (argsLength() == 3 && getArgs(0) == 10) {
                    setArgs(0, 11)
                }
            }
        })

        logI(TAG, "MiSafetyHook success")
    }
}