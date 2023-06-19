package net.donething.pc_phone.shortcuts

import android.util.Log
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.donething.pc_phone.MyApp
import net.donething.pc_phone.R
import java.lang.Exception

/**
 * 操作在 Activity 中的执行时期
 */
enum class RunAt {
    OnCreate, OnWindowFocusChanged
}

/**
 * 通过快捷方式执行的任务，需要实现 doOperation
 */
abstract class IOperation : LifecycleOwner {
    // 任务的标题（shortcuts.xml 中的 shortcutShortLabel）
    abstract val label: String

    // 操作在 Activity 中的执行时期（默认 onCreate ，可按需设置）
    open val runAt: RunAt = RunAt.OnCreate

    // 执行的任务的id（shortcuts.xml 中的 shortcutId）
    lateinit var id: String

    // 需执行任务的实现，返回执行结果
    abstract fun doOperation(): String

    // 在子线程执行任务，在主线程（UI线程）显示结果
    fun start(tv: TextView?) {
        lifecycleScope.launch {
            // 执行耗时任务
            val result = withContext(Dispatchers.IO) {
                try {
                    doOperation()
                } catch (e: Exception) {
                    e.printStackTrace()
                    MyApp.ctx.getString(R.string.shortcut_tip_do_operation_err, id, e.toString())
                }
            }

            if (tv != null) {
                tv.text = result
            } else {
                Log.i(this::class.java.simpleName, result)
            }
        }
    }

    companion object {
        /**
         * 根据 Intent 的 extra 中 id 值，判断执行的任务
         */
        fun parseOperation(id: String): IOperation {
            val operation = when (id) {
                MyApp.ctx.getString(R.string.shortcut_id_wakeup_pc) -> WakeUpPC
                MyApp.ctx.getString(R.string.shortcut_id_clipboard_send) -> ClipboardSend
                MyApp.ctx.getString(R.string.shortcut_id_clipboard_load) -> ClipboardLoad

                else -> {
                    Log.i(IOperation::class.java.name, "未知的操作 id：'$id'")
                    UnknownlOperation
                }
            }

            operation.id = id

            return operation
        }
    }
}
