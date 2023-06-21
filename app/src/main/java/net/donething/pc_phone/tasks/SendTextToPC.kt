package net.donething.pc_phone.tasks

import android.util.Log
import net.donething.pc_phone.MyApp
import net.donething.pc_phone.R
import net.donething.pc_phone.ui.preferences.Pref
import net.donething.pc_phone.utils.Form
import net.donething.pc_phone.utils.Http

/**
 * 发送内容到 PC
 */
object SendTextToPC : ITask<String>() {
    private val itag = this::class.simpleName

    override val label: String = MyApp.ctx.getString(R.string.label_pc_send_text)

    override fun doTask(): String {
        if (data == null) {
            val msg = MyApp.ctx.getString(R.string.tip_data_null)
            Log.i(itag, msg)
            return msg
        }

        val pcAddr = MyApp.myDS.getString(Pref.PC_ADDR, "")
        if (pcAddr.isNullOrBlank()) {
            return MyApp.ctx.getString(R.string.tip_pc_addr_null)
        }

        // 发送文本
        val form = Form("", data)
        val obj = Http.postJSON<String>("$pcAddr/api/clip/send", form)

        Log.i(itag, "响应：${obj.msg}")

        return obj.msg
    }
}