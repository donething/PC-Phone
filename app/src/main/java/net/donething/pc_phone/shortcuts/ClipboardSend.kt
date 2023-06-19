package net.donething.pc_phone.shortcuts

import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import androidx.lifecycle.LifecycleRegistry
import net.donething.pc_phone.MyApp
import net.donething.pc_phone.R
import net.donething.pc_phone.utils.Form
import net.donething.pc_phone.utils.Http
import net.donething.pc_phone.share.pcHost

/**
 * 发送剪贴板到 PC
 * Android 10 以后，Activity 必须拥有焦点（前台，而不只是 onResume），才能读取剪贴板
 */
object ClipboardSend : IOperation() {
    private val itag = this::class.simpleName

    override val lifecycle = LifecycleRegistry(this)

    override val runAt = RunAt.OnWindowFocusChanged

    override val label: String = MyApp.ctx.getString(R.string.shortcut_label_clipboard_send_short)

    override fun doOperation(): String {
        val clipboardManager = MyApp.ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        if (!clipboardManager.hasPrimaryClip()) {
            // Android 10 中 只有默认输入法(IME)或者是目前处于焦点的应用, 才能访问到剪贴板数据.
            val msg = MyApp.ctx.getString(R.string.shortcut_tip_clipbroad_send_blank)
            Log.i(itag, msg)
            return msg
        }

        val item = clipboardManager.primaryClip?.getItemAt(0)
        val text = item?.text?.toString()
        if (text.isNullOrEmpty()) {
            val msg = MyApp.ctx.getString(R.string.shortcut_tip_clipbroad_send_read_text_fail)
            Log.i(itag, msg)
            return msg
        }

        // 发送文本
        val data = Form("", text)
        val obj = Http.postJSON<String>("$pcHost/api/clip/send", data)

        Log.i(itag, "响应：'${obj.msg}'")

        return obj.msg
    }
}