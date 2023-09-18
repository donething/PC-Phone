package net.donething.pc_phone.tasks

import android.util.Log
import net.donething.pc_phone.MyApp
import net.donething.pc_phone.R
import net.donething.pc_phone.ui.preferences.Pref
import net.donething.pc_phone.utils.Form
import net.donething.pc_phone.utils.Http
import org.jetbrains.annotations.Nullable

/**
 * 关机 PC
 */
class ShutdownPC : ITask<Nullable>() {
    private val itag = this::class.simpleName

    override val label: String = MyApp.ctx.getString(R.string.shortcut_label_shutdown_pc_short)

    override fun doTask(): String {
        Log.i(itag, "执行：$label")

        val pcAddr = MyApp.myDS.getString(Pref.PC_ADDR, "")
        if (pcAddr.isNullOrBlank()) {
            return MyApp.ctx.getString(R.string.tip_pc_addr_null)
        }

        // 发送关机命令
        val form = Form("shutdown", 60)
        val obj = Http.postJSON<String>("$pcAddr/api/shutdown", form)

        Log.i(itag, "响应：${obj.msg}")

        return obj.msg
    }
}