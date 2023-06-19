package net.donething.pc_phone.share

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import net.donething.pc_phone.R
import net.donething.pc_phone.shortcuts.WakeUpPC
import net.donething.pc_phone.utils.MyNo
import net.donething.pc_phone.widgets.MyWidget

const val mac = "30:9C:23:D3:C0:89"
const val pcHost = "http://192.168.68.96:8800"

class WidgetService : Service(), LifecycleOwner {
    private val itag = this::class.java.simpleName
    private val notificationID = 234521

    override val lifecycle = LifecycleRegistry(this)

    // 执行
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val action = intent.action

        Log.i(itag, "收到 Action '$action'")

        if (action == null) {
            stopSelf()
            return super.onStartCommand(intent, flags, startId)
        }

        when (action) {
            MyWidget.actionWakeupPC -> WakeUpPC.start(null)

            else -> Log.i(itag, "未知的 Action '$action'")
        }

        // 构建通知，开启前台服务
        val builder = NotificationCompat.Builder(this, MyNo.ChannelIDBGTask)
        val notification = builder.setContentTitle(getString(R.string.no_bg_task_title))
            .setContentText(getString(R.string.no_bg_task_msg)).setSmallIcon(R.drawable.ic_launcher_foreground)
            .setWhen(System.currentTimeMillis()).build()

        // 启动前台服务
        startForeground(notificationID, notification)

        stopSelf()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}