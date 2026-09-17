package com.auramirage.app
import android.app.*
import android.content.*
import android.os.Build
import androidx.core.app.NotificationCompat
class NotificationHelper(private val c:Context){companion object{const val CHANNEL="reading";const val ID=10};init{if(Build.VERSION.SDK_INT>=26)c.getSystemService(NotificationManager::class.java).createNotificationChannel(NotificationChannel(CHANNEL,c.getString(R.string.notification_channel_name),NotificationManager.IMPORTANCE_LOW))};fun showReady(){val p=PendingIntent.getActivity(c,1,Intent(c,MainActivity::class.java),PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT);val n=NotificationCompat.Builder(c,CHANNEL).setSmallIcon(android.R.drawable.ic_menu_info_details).setContentTitle("Files and text for listening").setContentText("Pronto para colar ou anexar conteúdo").setContentIntent(p).setOngoing(true).addAction(android.R.drawable.ic_menu_edit,"Colar",p).addAction(android.R.drawable.ic_menu_add,"Anexar",p).addAction(android.R.drawable.ic_menu_more,"Mais",p).build();c.getSystemService(NotificationManager::class.java).notify(ID,n)};fun cancel(){c.getSystemService(NotificationManager::class.java).cancel(ID)}}
