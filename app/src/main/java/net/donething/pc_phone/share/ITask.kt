package net.donething.pc_phone.share

abstract class ITask {
    // 任务的标题（点击的分享菜单的标题）
    abstract val label: String

    // 执行的任务的id（点击的分享菜单的 id）
    lateinit var id: String

    abstract fun doTask(): String
}