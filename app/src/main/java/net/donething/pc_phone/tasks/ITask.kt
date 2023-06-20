package net.donething.pc_phone.tasks

abstract class ITask<T> {
    // 任务的标题
    abstract val label: String

    var data: T? = null

    /**
     * 任务的视线
     */
    abstract fun doTask(): String
}