package net.donething.pc_phone.tasks

abstract class ITask<T> {
    // 任务的标题
    abstract val label: String

    // 延时执行任务（毫秒）
    var delay: Long? = null

    var data: T? = null

    /**
     * 任务的视线
     */
    abstract fun doTask(): String
}