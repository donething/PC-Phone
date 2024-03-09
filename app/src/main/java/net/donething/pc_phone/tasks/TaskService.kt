package net.donething.pc_phone.tasks

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.Uri
import android.os.IBinder
import android.os.SystemClock
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.donething.pc_phone.DialogActivity
import net.donething.pc_phone.MyApp
import net.donething.pc_phone.R
import net.donething.pc_phone.utils.MyNo

class TaskService : Service(), LifecycleOwner {
    companion object {
        // Intent 数据的键
        const val INTENT_DATA_KEY = "data"

        // 快捷方式的 Action，需与 shortcuts.xml 中的 shortcutID 一致
        var ACTION_WAKEUP_PC = MyApp.ctx.getString(R.string.shortcut_id_wakeup_pc)
        var ACTION_SHUTDOWN_PC = MyApp.ctx.getString(R.string.shortcut_id_shutdown_pc)
        var ACTION_CLIP_CLEAR = MyApp.ctx.getString(R.string.shortcut_id_clipboard_clear)
        var ACTION_CLIP_LOAD = MyApp.ctx.getString(R.string.shortcut_id_clipboard_load)
        var ACTION_CLIP_SEND = MyApp.ctx.getString(R.string.shortcut_id_clipboard_send)
        var ACTION_MEDIA_TIMED_PAUSE = MyApp.ctx.getString(R.string.shortcut_id_media_timed_pause)

        // 分享的 Action
        var ACTION_PC_SEND_TEXT = MyApp.ctx.getString(R.string.share_action_pc_send_text)
        var ACTION_PC_SEND_FILES = MyApp.ctx.getString(R.string.share_action_pc_send_files)
    }

    private val itag = this::class.java.simpleName

    // 唯一的通知 ID
    private val notificationId = SystemClock.uptimeMillis().toInt()

    // 构建通知，开启前台服务
    private val builder = NotificationCompat.Builder(this, MyNo.ChannelIDBGTask)

    override val lifecycle = LifecycleRegistry(this)

    // 执行
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val action = intent.action

        Log.i(itag, "收到 Action '$action'")

        // 根据 Intent 中的参数，解析需要执行的任务
        val task = when (action) {
            ACTION_WAKEUP_PC -> WakeUpPC()

            ACTION_SHUTDOWN_PC -> ShutdownPC()

            ACTION_CLIP_CLEAR -> ClipboardClear()

            ACTION_CLIP_LOAD -> ClipboardLoad()

            ACTION_CLIP_SEND -> ClipboardSend(intent.getStringExtra(INTENT_DATA_KEY))

            // 延时一小时后暂停
            ACTION_MEDIA_TIMED_PAUSE -> MediaTimedPause(60 * 60 * 1000L)

            ACTION_PC_SEND_TEXT -> SendTextToPC(intent.getStringExtra(INTENT_DATA_KEY))

            ACTION_PC_SEND_FILES -> SendFilesToPC(intent.getParcelableArrayListExtra(INTENT_DATA_KEY, Uri::class.java))

            // 未知的任务
            else -> UnknownTask
        }

        val notification = builder.setContentTitle(task.label)
            .setContentText(getString(R.string.no_bg_task_content_default))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setWhen(System.currentTimeMillis())
            .build()

        // 启动前台服务
        startForeground(notificationId, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)

        // start()中调用 lifecycleScope.launch 中的代码是异步执行，不会等任务执行完后返回
        // 所以不要在start()后调用stopSelf()，会直接停止服务，虽然任务依然会继续，带前面的通知会取消发出
        start(task)

        return START_NOT_STICKY
    }

    // 在子线程执行任务，在主线程（UI线程）显示结果
    private fun <T> start(task: ITask<T>) {
        val ctx = this

        lifecycleScope.launch {
            // 执行耗时任务
            val result = withContext(Dispatchers.IO) {
                // 按任务配置延时执行
                delay(task.delay ?: 0)

                try {
                    task.doTask()
                } catch (e: Exception) {
                    e.printStackTrace()
                    val title = getString(R.string.shortcut_tip_do_task_err, task.label)

                    reNotify(title, e.toString())
                    "$title：$e"
                }
            }

            // 执行玩任务后，停止服务
            // 不能放在调用 reNotify() 后，会导致发送的通知取消发出
            // 如果任务极快完成（如doTask中判断条件不合直接返回），导致stopSelf()马上被调用，会导致通知发不出来
            ctx.stopSelf()

            val title = "已执行'${task.label}'"

            Log.i(itag, "$title：${result}")

            Toast.makeText(ctx, "${task.label}：$result", Toast.LENGTH_LONG).show()
            reNotify(title, result)
        }
    }

    @SuppressLint("MissingPermission")
    private fun reNotify(title: String, content: String) {
        builder.setContentTitle(title).setContentText(content)

        // 创建 Intent
        val intent = Intent(this, DialogActivity::class.java)
        intent.putExtra(DialogActivity.dialogTitleKey, title)
        intent.putExtra(DialogActivity.dialogContentKey, content)
        // 创建 PendingIntent，点击通知后打开对话框来显示通知详情
        val pendingIntent = PendingIntent.getActivity(
            this, notificationId, intent, PendingIntent.FLAG_IMMUTABLE
        )
        builder.setContentIntent(pendingIntent)

        val notification = builder.build()
        NotificationManagerCompat.from(this).notify(notificationId, notification)
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}