package net.donething.pc_phone.entity

/**
 * 服务端返回的数据
 * @param code 不为 0 表示有错误
 */
data class Rest<T>(val code: Int, val msg: String, val data: T? = null)
