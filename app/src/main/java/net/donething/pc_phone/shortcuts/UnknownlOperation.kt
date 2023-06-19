package net.donething.pc_phone.shortcuts

import androidx.lifecycle.LifecycleRegistry
import net.donething.pc_phone.MyApp
import net.donething.pc_phone.R

/**
 * 当 shortcut id 没有匹配到操作时，返回该对象
 */
object UnknownlOperation : IOperation() {
    override val label: String = MyApp.ctx.getString(R.string.shortcut_label_unknown_operation)

    override val lifecycle = LifecycleRegistry(this)

    override fun doOperation(): String {
        return "未知的操作：'${this.id}'"
    }
}