package net.donething.pc_phone.tasks

import net.donething.pc_phone.MyApp
import net.donething.pc_phone.R
import org.jetbrains.annotations.Nullable

/**
 * 当 shortcut id 没有匹配到操作时，返回该对象
 */
object UnknownTask : ITask<Nullable>() {
    override val label: String = MyApp.ctx.getString(R.string.shortcut_label_unknown_task)

    override fun doTask(): String {
       return label
    }
}