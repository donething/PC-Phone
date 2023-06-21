package net.donething.pc_phone

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import net.donething.pc_phone.tasks.TaskService

/**
 * 点击快捷方式启动的对话框 Activity
 */
class ShortcutActivity : DialogActivity() {
    private val itag = this::class.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 根据 shortcut id 解析得到操作
        val id = intent?.getStringExtra("id") ?: ""

        if (id != getString(R.string.shortcut_id_clipboard_send)) {
            val intent = Intent(this, TaskService::class.java)
            intent.action = id
            startForegroundService(intent)

            finishAndRemoveTask()
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

        if (intent?.getStringExtra("id") == TaskService.ACTION_CLIP_SEND && hasFocus) {
            binding.tvActivityDialogTitleText.text = getString(R.string.shortcut_label_clipboard_send_short)
            sendClip()

            finishAndRemoveTask()
        }
    }

    // 发送剪贴板
    private fun sendClip() {
        val clipManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        if (!clipManager.hasPrimaryClip()) {
            // Android 10 中 只有默认输入法(IME)或者是目前处于焦点的应用, 才能访问到剪贴板数据.
            val msg = getString(R.string.shortcut_tip_clipbroad_send_data_null)
            Log.i(itag, msg)
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
            return
        }

        val item = clipManager.primaryClip?.getItemAt(0)
        val text = item?.text?.toString()
        if (text.isNullOrEmpty()) {
            val msg = getString(R.string.shortcut_tip_clipbroad_send_read_text_fail)
            Log.i(itag, msg)
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
            return
        }

        val intent = Intent(this, TaskService::class.java)
        intent.action = TaskService.ACTION_CLIP_SEND
        intent.putExtra(TaskService.INTENT_DATA_KEY, text)
        startForegroundService(intent)
    }
}