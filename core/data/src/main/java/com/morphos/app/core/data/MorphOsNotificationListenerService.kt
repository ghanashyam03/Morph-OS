package com.morphos.app.core.data

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class MorphOsNotificationListenerService : NotificationListenerService() {
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        // Stub
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        // Stub
    }
}
