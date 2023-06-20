package net.donething.pc_phone.tasks

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.donething.pc_phone.DialogActivity
import net.donething.pc_phone.MyApp
import net.donething.pc_phone.R
import net.donething.pc_phone.utils.MyNo
import kotlin.random.Random


const val mac = "30:9C:23:D3:C0:89"
const val pcHost = "http://192.168.68.96:8800"

class TaskService : Service(), LifecycleOwner {
    companion object {
        // Intent 数据的键
        const val INTENT_DATA_KEY = "data"

        // 快捷方式的 Action，需与 shortcuts.xml 中的 shortcutID 一致
        var ACTION_WAKEUP_PC = MyApp.ctx.getString(R.string.shortcut_id_wakeup_pc)
        var ACTION_CLIP_LOAD = MyApp.ctx.getString(R.string.shortcut_id_clipboard_load)

        // 分享的 Action
        var ACTION_PC_SEND_TEXT = MyApp.ctx.getString(R.string.share_action_pc_send_text)
        var ACTION_PC_SEND_FILES = MyApp.ctx.getString(R.string.share_action_pc_send_files)
    }

    private val itag = this::class.java.simpleName

    // 唯一的通知 ID
    private val notificationId = "${System.currentTimeMillis()}_${Random.nextInt()}".hashCode()

    // 构建通知，开启前台服务
    private val builder = NotificationCompat.Builder(this, MyNo.ChannelIDBGTask)

    override val lifecycle = LifecycleRegistry(this)

    // 执行
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val action = intent.action

        Log.i(itag, "收到 Action '$action'")

        val task = when (action) {
            ACTION_WAKEUP_PC -> WakeUpPC

            ACTION_CLIP_LOAD -> ClipboardLoad

            ACTION_PC_SEND_TEXT -> {
                SendTextToPC.data = intent.getStringExtra(INTENT_DATA_KEY)
                SendTextToPC
            }

            ACTION_PC_SEND_FILES -> {
                SendFilesToPC.data = intent.getParcelableArrayListExtra(INTENT_DATA_KEY, Uri::class.java)
                SendFilesToPC
            }

            else -> UnknownTask
        }

        start(task)

        stopSelf()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onCreate() {
        super.onCreate()

        val notification = builder.setContentTitle(getString(R.string.no_bg_task_title))
            .setContentText(getString(R.string.no_bg_task_msg)).setSmallIcon(R.drawable.ic_launcher_foreground)
            .setWhen(System.currentTimeMillis()).build()
        // 启动前台服务
        startForeground(notificationId, notification)
    }

    // 在子线程执行任务，在主线程（UI线程）显示结果
    private fun <T> start(task: ITask<T>) {
        val ctx = this
        lifecycleScope.launch {
            // 执行耗时任务
            val result = withContext(Dispatchers.IO) {
                try {
                    task.doTask()
                } catch (e: Exception) {
                    e.printStackTrace()
                    val title = getString(R.string.shortcut_tip_do_task_err, task.label)

                    reNotify(title, e.toString())
                    "$title：$e"
                }
            }

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