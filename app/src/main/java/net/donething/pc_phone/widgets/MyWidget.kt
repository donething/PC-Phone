package net.donething.pc_phone.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import net.donething.pc_phone.ShortcutActivity
import net.donething.pc_phone.R
import net.donething.pc_phone.share.WidgetService

class MyWidget : AppWidgetProvider() {
    companion object {
        // 唤醒 PC
        const val actionWakeupPC = "net.donething.pc_phone.WIDGET_WAKEUP_PC"

        // 剪贴板操作
        const val actionClipSend = "net.donething.pc_phone.WIDGET_CLIP_SEND"
        const val actionClipLoad = "net.donething.pc_phone.WIDGET_CLIP_LOAD"
    }

    private val itag = this::class.java.simpleName

    // 更新组件中设置具体执行事件
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            Log.i(itag, "更新 Widget，ID $appWidgetId（共${appWidgetIds.size}个组件）")
            val views = RemoteViews(context.packageName, R.layout.widget_layout)

            // 唤醒 PC
            // Android 8 以后，需要以前台服务（Foreground Service）开启目标 Service
            val wakeupPCIntent = Intent(context, WidgetService::class.java)
            // 指定需要执行的事件
            wakeupPCIntent.action = actionWakeupPC
            val wakeupPCPendingIntent = PendingIntent.getService(
                context, 0, wakeupPCIntent, PendingIntent.FLAG_IMMUTABLE
            )
            // 绑定视图和事件
            views.setOnClickPendingIntent(R.id.bnWakeupPC, wakeupPCPendingIntent)


            // 发送剪贴板到 PC
            val sendClipIntent = Intent(context, ShortcutActivity::class.java)
            sendClipIntent.action = actionClipSend
            sendClipIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            val sendClipPendingIntent = PendingIntent.getActivity(
                context, 0, sendClipIntent, PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.bnClipboardSend, sendClipPendingIntent)


            // 获取 PC 剪贴板
            val loadClipIntent = Intent(context, WidgetService::class.java)
            loadClipIntent.action = actionClipLoad
            val loadClipPendingIntent = PendingIntent.getService(
                context, 0, loadClipIntent, PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.bnClipboardLoad, loadClipPendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
