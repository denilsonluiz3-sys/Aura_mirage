package com.auramirage.app

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class NotificationHelper(private val context: Context) {
    companion object {
        const val CHANNEL = "reading"
        const val ID = 10
        const val ACTION_PASTE = "com.auramirage.app.action.PASTE"
        const val ACTION_ATTACH = "com.auramirage.app.action.ATTACH"
        const val ACTION_MORE = "com.auramirage.app.action.MORE"
    }

    init {
        if (Build.VERSION.SDK_INT >= 26) {
            context.getSystemService(NotificationManager::class.java)
                .createNotificationChannel(NotificationChannel(CHANNEL, context.getString(R.string.notification_channel_name), NotificationManager.IMPORTANCE_LOW).apply {
                    description = context.getString(R.string.notification_channel_description)
                })
        }
    }

    private fun actionIntent(action: String, requestCode: Int): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).setAction(action).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        return PendingIntent.getActivity(context, requestCode, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
    }

    fun showReady() {
        val open = actionIntent(Intent.ACTION_MAIN, 1)
        val notification = NotificationCompat.Builder(context, CHANNEL)
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setContentTitle("Files and text for listening")
            .setContentText("Pronto para colar ou anexar conteúdo")
            .setContentIntent(open)
            .setOngoing(true)
            .addAction(android.R.drawable.ic_menu_edit, "Colar", actionIntent(ACTION_PASTE, 2))
            .addAction(android.R.drawable.ic_menu_add, "Anexar", actionIntent(ACTION_ATTACH, 3))
            .addAction(android.R.drawable.ic_menu_more, "Mais", actionIntent(ACTION_MORE, 4))
            .build()
        context.getSystemService(NotificationManager::class.java).notify(ID, notification)
    }

    fun cancel() = context.getSystemService(NotificationManager::class.java).cancel(ID)
}
