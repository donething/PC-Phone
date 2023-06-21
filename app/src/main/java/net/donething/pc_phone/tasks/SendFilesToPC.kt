package net.donething.pc_phone.tasks

import android.net.Uri
import android.util.Log
import net.donething.pc_phone.MyApp
import net.donething.pc_phone.R
import net.donething.pc_phone.ui.preferences.Pref
import net.donething.pc_phone.utils.Http

/**
 * 发送内容到 PC
 */
object SendFilesToPC : ITask<ArrayList<Uri>>() {
    private val itag = this::class.simpleName

    override val label: String = MyApp.ctx.getString(R.string.label_pc_send_files)

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

        val obj = Http.postFiles<Map<String, String>>(
            "$pcAddr/api/file/send", data!!, MyApp.ctx
        )

        val result = StringBuilder()

        obj.data.forEach { (key, value) ->
            result.append("$key $value\n")
        }

        return result.toString()
    }
}