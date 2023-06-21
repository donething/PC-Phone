package net.donething.pc_phone.tasks

import android.util.Log
import net.donething.pc_phone.MyApp
import net.donething.pc_phone.R
import net.donething.pc_phone.ui.preferences.Pref
import org.jetbrains.annotations.Nullable
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

/**
 * 唤醒 PC
 */
object WakeUpPC : ITask<Nullable>() {
    private val itag = this::class.simpleName

    override val label: String = MyApp.ctx.getString(R.string.shortcut_label_wakeup_pc_short)

    override fun doTask(): String {
        val mac = MyApp.myDS.getString(Pref.PC_MAC, "")
        if (mac.isNullOrBlank()) {
            return MyApp.ctx.getString(R.string.tip_pc_mac_null)
        }

        // 执行耗时任务
        sendWakeOnLanPacket(mac)
        val msg = MyApp.ctx.getString(R.string.shortcut_tip_wakeup_pc_success)
        Log.i(itag, msg)

        return msg
    }

    /**
     * 唤醒指定设备
     * @param macAddress 设备的 MAC 地址，不区分大小写。如 "AA:BB:CC:DD:EE:FF"
     * @param port 发送目标的端口。默认 9
     */
    private fun sendWakeOnLanPacket(macAddress: String, port: Int = 9) {
        val magicPacket = createMagicPacket(macAddress)
        val broadcastAddress = InetAddress.getByName("255.255.255.255")

        DatagramSocket().use { socket ->
            socket.broadcast = true
            val packet = DatagramPacket(magicPacket, magicPacket.size, broadcastAddress, port)
            socket.send(packet)
        }
    }

    private fun createMagicPacket(macAddress: String): ByteArray {
        val macBytes = macAddress.split(":").map { it.toInt(16).toByte() }.toByteArray()
        val magicPacket = ByteArray(6 + 16 * macBytes.size)
        for (i in 0..5) {
            magicPacket[i] = 0xFF.toByte()
        }
        for (i in 6 until magicPacket.size step macBytes.size) {
            macBytes.copyInto(magicPacket, i)
        }
        return magicPacket
    }
}