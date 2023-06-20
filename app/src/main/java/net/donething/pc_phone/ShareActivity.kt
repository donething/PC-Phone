package net.donething.pc_phone

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import net.donething.pc_phone.tasks.TaskService

/**
 * 接受系统分享的菜单
 * @link Android - 分享内容 - 接收其他APP的内容：https://www.cnblogs.com/fengquanwang/p/3148689.html
 */
class ShareActivity : DialogActivity() {
    private val itag = this::class.simpleName

    @SuppressLint("InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 绑定事件
        binding.bnActivityDialogOk.setOnClickListener {
            finishAndRemoveTask()
        }

        binding.tvActivityDialogTitleText.text = "分享到…"

        // 替换对话框的默认视图为分享面板
        val sharePanel = LayoutInflater.from(this).inflate(R.layout.layout_share_panel, null)
        val tvTextContent = binding.tvActivityDialogContentText
        val parent = (tvTextContent.parent as ViewGroup)
        parent.removeView(tvTextContent)
        parent.addView(sharePanel)

        // 绑定分享按钮的点击事件
        val ibSendPC = sharePanel.findViewById<ImageButton>(R.id.ib_share_send_pc)
        ibSendPC.setOnClickListener(onShareImageButtonClick)
    }

    private val onShareImageButtonClick = View.OnClickListener {
        if (intent == null || intent.type == null) {
            val msg = "分享的 intent 或数据 type 为空：$intent"
            Log.i(itag, msg)
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
            return@OnClickListener
        }

        val serviceIntent = Intent(this, TaskService::class.java)

        // 获取分享的内容
        val action: String
        if (intent.action == Intent.ACTION_SEND && intent.type!!.startsWith("text/")) {
            action = TaskService.ACTION_PC_SEND_TEXT
            val text = intent.getStringExtra(Intent.EXTRA_TEXT)
            serviceIntent.putExtra(TaskService.INTENT_DATA_KEY, text)
            Log.i(itag, "发送文本：'$text'")
        } else if (intent.action == Intent.ACTION_SEND) {
            action = TaskService.ACTION_PC_SEND_FILES
            val file = intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
            serviceIntent.putExtra(
                TaskService.INTENT_DATA_KEY, arrayListOf(file)
            )
            Log.i(itag, "发送单个文件：'${file?.path}'")
        } else {
            action = TaskService.ACTION_PC_SEND_FILES
            val files = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM, Uri::class.java)
            serviceIntent.putExtra(TaskService.INTENT_DATA_KEY, files)
            Log.i(itag, "发送多个文件：${files?.size}个")
        }

        // 启动执行任务的服务
        serviceIntent.action = action
        startForegroundService(serviceIntent)
        finishAndRemoveTask()
    }
}