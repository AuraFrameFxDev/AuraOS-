package dev.aurakai.auraframefx.xposed

import com.highcapable.yukihookapi.YukiHookAPI
import com.highcapable.yukihookapi.hook.xposed.prefs.data.PrefsData
import com.highcapable.yukihookapi.hook.xposed.proxy.IYukiHookXposedInit

/**
 * Xposed module entry point for AuraFX framework.
 * This class is automatically loaded by the Xposed framework.
 */
class AuraXposedModule : IYukiHookXposedInit {

    companion object {
        // Shared preferences keys for module settings
        val ENABLED = PrefsData("enabled", true)
        val DEBUG_MODE = PrefsData("debug_mode", false)
    }

    override fun onInit() {
        // Initialize YukiHookAPI
        YukiHookAPI.config()
    }

    override fun onHook() {
        // This will be called when the module is loaded by Xposed
        // Actual hooks will be registered in their respective feature modules
    }
}
