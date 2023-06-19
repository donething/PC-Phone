package net.donething.pc_phone.shortcuts

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import androidx.lifecycle.LifecycleRegistry
import net.donething.pc_phone.MyApp
import net.donething.pc_phone.R
import net.donething.pc_phone.utils.Http
import net.donething.pc_phone.share.pcHost

/**
 * 获取 PC 剪贴板
 */
object ClipboardLoad : IOperation() {
    private val itag = this::class.simpleName

    override val lifecycle = LifecycleRegistry(this)

    override val label: String = MyApp.ctx.getString(R.string.shortcut_label_clipboard_load_short)

    override fun doOperation(): String {
        // 获取 PC 文本
        val obj = Http.get<String>("$pcHost/api/clip/get")

        Log.i(itag, "响应：'${obj.msg}'")

        if (obj.code != 0) {
            return obj.msg
        }

        val clipboardManager = MyApp.ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("text", obj.data)
        clipboardManager.setPrimaryClip(clip)

        return "\"${obj.data}\""
    }
}