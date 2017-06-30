package com.example.q_municate_chat_service.entity;

import com.quickblox.chat.model.QBChatMessage;

public class NotificationsParser {

    public static final String PROPERTY_NOTIFICATION_TYPE = "notification_type";

    public static NotificationType getNotificationType(QBChatMessage qbChatMessage){
        NotificationType notificatio = null;
        if (qbChatMessage.getProperty(PROPERTY_NOTIFICATION_TYPE) != null) {
            String chatNotificationTypeString = qbChatMessage.getProperty(PROPERTY_NOTIFICATION_TYPE).toString();

            if (chatNotificationTypeString != null) {
                notificatio = new GroupNotification(qbChatMessage);
            }
        }

        return notificatio;
    }
}
