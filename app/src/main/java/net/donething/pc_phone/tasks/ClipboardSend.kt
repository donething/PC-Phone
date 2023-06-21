package net.donething.pc_phone.tasks

import android.util.Log
import net.donething.pc_phone.MyApp
import net.donething.pc_phone.R

/**
 * 发送剪贴板到 PC
 * Android 10 以后，Activity 必须拥有焦点（前台，而不只是 onResume），才能读取剪贴板
 */
class ClipboardSend(data:String?) : ITask<String>(data) {
    private val itag = this::class.simpleName

    override val label: String = MyApp.ctx.getString(R.string.shortcut_label_clipboard_send_short)

    override fun doTask(): String {
        val msg = SendTextToPC(data).doTask()

        Log.i(itag, msg)

        return msg
    }
}