package net.donething.pc_phone.tasks

import android.net.Uri
import android.util.Log
import net.donething.pc_phone.MyApp
import net.donething.pc_phone.R
import net.donething.pc_phone.ui.preferences.PreferencesRepository
import net.donething.pc_phone.utils.Form
import net.donething.pc_phone.utils.Http

/**
 * 发送内容到 PC
 */
class SendFilesToPC(data: ArrayList<Uri>?) : ITask<ArrayList<Uri>>(data) {
    private val itag = this::class.simpleName

    override val label: String = MyApp.ctx.getString(R.string.label_pc_send_files)

    override fun doTask(): String {
        if (data == null) {
            val msg = MyApp.ctx.getString(R.string.tip_data_null)
            Log.i(itag, msg)
            return msg
        }

        val pcAddr = PreferencesRepository.taskMode().getServerAddr()
            ?: return MyApp.ctx.getString(R.string.tip_pc_addr_null)

        val obj = Http.postFiles<Map<String, String>>("$pcAddr/api/file/send", data, MyApp.ctx)

        if (obj.code != 0) {
            return obj.msg
        }

        val result = StringBuilder()

        obj.data?.forEach { (key, value) ->
            result.append("$key $value\n")
        }

        return result.toString()
    }
}

/**
 * 发送内容到 PC
 */
class SendTextToPC(data: String?) : ITask<String>(data) {
    private val itag = this::class.simpleName

    override val label: String = MyApp.ctx.getString(R.string.label_pc_send_text)

    override fun doTask(): String {
        if (data.isNullOrBlank()) {
            val msg = MyApp.ctx.getString(R.string.tip_data_null)
            Log.i(itag, msg)
            return msg
        }

        val pcAddr = PreferencesRepository.taskMode().getServerAddr()
            ?: return MyApp.ctx.getString(R.string.tip_pc_addr_null)

        // 发送文本
        val form = Form("", data)
        val obj = Http.postJSON<String>("$pcAddr/api/clip/send", form)

        Log.i(itag, "响应：${obj.msg}")

        return obj.msg
    }
}