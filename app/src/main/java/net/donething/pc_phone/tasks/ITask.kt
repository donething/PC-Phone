package net.donething.pc_phone.tasks

/**
 * 任务的抽象类
 *
 * 泛型 T 表示要传递数据 data 的类型。不需要传递则为 Nullable
 * @param data 执行任务需要传递的数据
 * @param delay 需要延时执行任务。单位毫秒
 */
abstract class ITask<T>(val data: T? = null, val delay: Long? = null) {
    // 任务的标题
    abstract val label: String

    /**
     * 任务的实现
     */
    abstract fun doTask(): String
}