package net.donething.pc_phone

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import net.donething.pc_phone.databinding.ActivityDialogBinding

/**
 * 对话框型的 Activity
 */
open class DialogActivity : AppCompatActivity() {
    companion object {
        // 设置对话框的标题、内容的键
        const val dialogTitleKey = "dialogTitle"
        const val dialogContentKey = "dialogContent"
    }

    lateinit var binding: ActivityDialogBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setFinishOnTouchOutside(true)

        // 设置布局文件
        binding = ActivityDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 设置窗口的位置、大小
        if (window != null) {
            // 将窗口位置设置为屏幕底部
            window.setGravity(Gravity.BOTTOM)

            // 设置布局的宽度、高度
            window.setBackgroundDrawableResource(android.R.color.transparent)
            window.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT
            )
        }

        // 绑定事件
        binding.bnActivityDialogCopy.setOnClickListener {
            if (intent?.getStringExtra(dialogContentKey) != null) {
                // 复制文本
                val content = intent.getStringExtra(dialogContentKey)

                val clipManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText(ClipDescription.MIMETYPE_TEXT_PLAIN, content)
                clipManager.setPrimaryClip(clip)

                Toast.makeText(this, "已复制内容", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "复制失败：内容为空", Toast.LENGTH_LONG).show()
            }

            finishAndRemoveTask()
        }
        binding.bnActivityDialogOk.setOnClickListener {
            finishAndRemoveTask()
        }

        // 设置对话框的标题、内容
        if (intent?.getStringExtra(dialogTitleKey) != null) {
            binding.tvActivityDialogTitleText.text = intent.getStringExtra(dialogTitleKey)
        }
        if (intent?.getStringExtra(dialogContentKey) != null) {
            binding.tvActivityDialogContentText.text = intent.getStringExtra(dialogContentKey)
        }
    }
}