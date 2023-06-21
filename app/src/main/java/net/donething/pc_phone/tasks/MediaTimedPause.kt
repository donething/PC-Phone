package net.donething.pc_phone.tasks

import android.content.Context
import android.media.AudioManager
import android.util.Log
import android.view.KeyEvent
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import net.donething.pc_phone.MyApp
import net.donething.pc_phone.R
import org.jetbrains.annotations.Nullable

object MediaTimedPause : ITask<Nullable>() {
    private val itag = this::class.simpleName

    override val label: String = MyApp.ctx.getString(R.string.shortcut_label_media_timed_pause_short)

    override fun doTask(): String {
        val mAudioManager = MyApp.ctx.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        Log.i(itag, "是否正在播放音频：${mAudioManager.isMusicActive}")
        if (mAudioManager.isMusicActive) {
            val event = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PAUSE)
            mAudioManager.dispatchMediaKeyEvent(event)
        }

        return "已发送暂停播放的事件"
    }
}