package org.dev_alex.mojo_qa.mojo

import org.dev_alex.mojo_qa.mojo.models.Notification

object SortUtil {
    fun sortNotifications(notifications: List<Notification>): List<Notification> {
        return notifications.sortedByDescending { it.create_date }
    }
}