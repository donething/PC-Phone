package net.donething.pc_phone.tasks

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import net.donething.pc_phone.MyApp
import net.donething.pc_phone.R
import org.jetbrains.annotations.Nullable

/**
 * 获取 PC 剪贴板
 */
object ClipboardClear : ITask<Nullable>() {
    private val itag = this::class.simpleName

    override val label: String = MyApp.ctx.getString(R.string.shortcut_label_clipboard_clear_short)

    override fun doTask(): String {
        val clipManager = MyApp.ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("text", "")
        clipManager.setPrimaryClip(clip)

        return MyApp.ctx.getString(R.string.shortcut_tip_clipboard_clear_success)
    }
}