package net.donething.pc_phone.onfocus

import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import android.widget.TextView
import androidx.lifecycle.LifecycleRegistry
import net.donething.pc_phone.MyApp
import net.donething.pc_phone.R
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.donething.pc_phone.tasks.SendTextToPC
import java.lang.Exception

/**
 * 发送剪贴板到 PC
 * Android 10 以后，Activity 必须拥有焦点（前台，而不只是 onResume），才能读取剪贴板
 */
object ClipboardSend : LifecycleOwner {
    private val itag = this::class.simpleName

    override val lifecycle = LifecycleRegistry(this)

    val label: String = MyApp.ctx.getString(R.string.shortcut_label_clipboard_send_short)

    private fun doTask(): String {
        val clipManager = MyApp.ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        if (!clipManager.hasPrimaryClip()) {
            // Android 10 中 只有默认输入法(IME)或者是目前处于焦点的应用, 才能访问到剪贴板数据.
            val msg = MyApp.ctx.getString(R.string.shortcut_tip_clipbroad_send_data_null)
            Log.i(itag, msg)
            return msg
        }

        val item = clipManager.primaryClip?.getItemAt(0)
        val text = item?.text?.toString()
        if (text.isNullOrEmpty()) {
            val msg = MyApp.ctx.getString(R.string.shortcut_tip_clipbroad_send_read_text_fail)
            Log.i(itag, msg)
            return msg
        }

        SendTextToPC.data = text
        val msg = SendTextToPC.doTask()

        Log.i(itag, msg)

        return msg
    }

    // 在子线程执行任务，在主线程（UI线程）显示结果
    fun start(tv: TextView?) {
        lifecycleScope.launch {
            // 执行耗时任务
            val result = withContext(Dispatchers.IO) {
                try {
                    doTask()
                } catch (e: Exception) {
                    e.printStackTrace()
                    MyApp.ctx.getString(R.string.shortcut_tip_do_task_err, label) + "：$e"
                }
            }

            if (tv != null) {
                tv.text = result
            } else {
                Log.i(this::class.java.simpleName, result)
            }
        }
    }
}