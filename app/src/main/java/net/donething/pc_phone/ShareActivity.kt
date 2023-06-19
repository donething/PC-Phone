package net.donething.pc_phone

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import net.donething.pc_phone.utils.Http
import net.donething.pc_phone.share.pcHost

/**
 * 接受系统分享的菜单
 * @link Android - 分享内容 - 接收其他APP的内容：https://www.cnblogs.com/fengquanwang/p/3148689.html
 */
class ShareActivity : DialogActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 绑定事件
        binding.bnActivityDialogOk.setOnClickListener {
            finishAndRemoveTask()
        }

        binding.tvActivityDialogTitle.text = "分享的内容"

        // 获取分享的内容
        if (intent.type == null) {
            binding.tvActivityDialogContent.text = "分享内容的类型为空"
            return
        }

        if (intent?.action == Intent.ACTION_SEND) {
            if (intent.type!!.startsWith("text/")) {
                handleText(intent)
            } else {
                handleFile(intent)
            }
        } else if (intent?.action == Intent.ACTION_SEND_MULTIPLE) {
            handleMultipleFiles(intent)
        }
    }

    private fun handleText(intent: Intent) {
        // 处理文本数据
        val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
        if (sharedText != null) {
            // 处理分享的文本内容
            binding.tvActivityDialogContent.text = sharedText
        }
    }

    private fun handleFile(intent: Intent) {
        val fileUri: Uri? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
        } else {
            intent.getParcelableExtra(Intent.EXTRA_STREAM)
        }

        if (fileUri != null) {
            // 处理分享的文件
            binding.tvActivityDialogContent.text = "文件路径：${fileUri.path}"
            val obj = Http.postFiles<String>("$pcHost/api/file/send", listOf(fileUri), this)
        }
    }

    private fun handleMultipleFiles(intent: Intent) {
        val fileUris: ArrayList<Uri>? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM, Uri::class.java)
        } else {
            intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM)
        }

        if (fileUris != null) {
            // 处理分享的多个文件
            binding.tvActivityDialogContent.text = "文件路径：$fileUris"
            val obj = Http.postFiles<String>("$pcHost/api/file/send", fileUris, this)
            Toast.makeText(this, obj.msg, Toast.LENGTH_LONG).show()
        }
    }
}