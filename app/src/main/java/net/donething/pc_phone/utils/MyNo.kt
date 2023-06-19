package net.donething.pc_phone.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NotificationManagerCompat
import net.donething.pc_phone.MyApp
import net.donething.pc_phone.R

object MyNo {
    // 执行后台任务的通知渠道 ID
    var ChannelIDBGTask = MyApp.ctx.getString(R.string.no_channel_id_bg_task)

    /**
     * 开启通知权限，设置通知频道
     */
    fun setNotificationPermission(ctx: Context) {
        // 是否需要创建通知渠道
        if (NotificationManagerCompat.from(ctx).areNotificationsEnabled()) {
            // 通知渠道
            val channelBGTask = NotificationChannel(
                ChannelIDBGTask, ctx.getString(R.string.no_channel_name_bg_task), NotificationManager.IMPORTANCE_MIN
            )

            // 创建通知渠道
            val manager = ctx.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channelBGTask)
            return
        }

        // 没有开启通知权限，提示开启
        AlertDialog.Builder(ctx).setTitle(ctx.getString(R.string.no_enable_dialog_title))
            .setMessage(ctx.getString(R.string.no_enable_dialog_msg))
            .setPositiveButton(ctx.getString(R.string.no_enable_dialog_bn_ok)) { _, _ ->
                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, ctx.packageName)

                ctx.startActivity(intent)
            }.setNegativeButton(ctx.getString(R.string.no_enable_dialog_bn_cancel)) { _, _ -> }.show()
    }
}