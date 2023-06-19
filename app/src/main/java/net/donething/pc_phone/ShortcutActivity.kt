package net.donething.pc_phone

import android.os.Bundle
import android.window.OnBackInvokedDispatcher
import androidx.activity.OnBackPressedCallback
import net.donething.pc_phone.shortcuts.IOperation
import net.donething.pc_phone.shortcuts.RunAt

/**
 * 点击快捷方式启动的对话框 Activity
 */
class ShortcutActivity : DialogActivity() {
    // 点击 shortcut 后，需要执行的任务
    private lateinit var operation: IOperation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 根据 shortcut id 解析得到操作
        val id = intent?.getStringExtra("id") ?: ""
        operation = IOperation.parseOperation(id)

        binding.tvActivityDialogTitle.text = operation.label

        finishOnTouchOutsides(true)

        // 执行任务
        if (operation.runAt == RunAt.OnCreate) {
            operation.start(binding.tvActivityDialogContent)
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

        if (operation.runAt == RunAt.OnWindowFocusChanged && hasFocus) {
            operation.start(binding.tvActivityDialogContent)
        }
    }
}