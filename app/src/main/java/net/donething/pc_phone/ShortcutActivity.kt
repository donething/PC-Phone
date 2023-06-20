package net.donething.pc_phone

import android.content.Intent
import android.os.Bundle
import net.donething.pc_phone.tasks.TaskService
import net.donething.pc_phone.onfocus.ClipboardSend

/**
 * 点击快捷方式启动的对话框 Activity
 */
class ShortcutActivity : DialogActivity() {
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

        if (intent?.getStringExtra("id") == getString(R.string.shortcut_id_clipboard_send) && hasFocus) {
            binding.tvActivityDialogTitleText.text = ClipboardSend.label
            ClipboardSend.start(binding.tvActivityDialogContentText)
        }
    }
}