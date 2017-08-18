package com.quickblox.q_municate.utils.chat;


import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;

import com.example.q_municate_chat_service.entity.GroupNotification;
import com.example.q_municate_chat_service.entity.QBMessage;
import com.quickblox.core.helper.CollectionsUtil;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.utils.ChatUtils;
import com.quickblox.q_municate_db.managers.DataManager;
import com.quickblox.users.model.QBUser;

public class ChatMessageUtils {

    public static final String PROPERTY_ROOM_NAME = "room_name";
    public static final String PROPERTY_ROOM_PHOTO = "room_photo";
    public static final String PROPERTY_ROOM_CURRENT_OCCUPANTS_IDS = "current_occupant_ids";
    public static final String PROPERTY_ROOM_ADDED_OCCUPANTS_IDS = "added_occupant_ids";
    public static final String PROPERTY_ROOM_DELETED_OCCUPANTS_IDS = "deleted_occupant_ids";
    public static final String PROPERTY_ROOM_UPDATED_AT = "room_updated_date";
    public static final String PROPERTY_ROOM_UPDATE_INFO = "dialog_update_info";

    public static final String PROPERTY_MODULE_IDENTIFIER = "moduleIdentifier";
    public static final String PROPERTY_NOTIFICATION_TYPE = "notification_type";
    public static final String PROPERTY_CHAT_TYPE = "type";
    public static final String PROPERTY_DATE_SENT = "date_sent";

    public static final boolean VALUE_SAVE_TO_HISTORY = true;
    public static final String VALUE_MODULE_IDENTIFIER = "SystemNotifications";
    public static final String VALUE_GROUP_CHAT_TYPE = "2";

    public static String getBodyForUpdateChatNotificationMessage(Context context, QBMessage qbChatMessage) {
        String notificationTypeString = (String) qbChatMessage.getProperty(PROPERTY_NOTIFICATION_TYPE);
        String updatedInfo = (String) qbChatMessage.getProperty(PROPERTY_ROOM_UPDATE_INFO);
        String addedOccupantsIdsString = (String) qbChatMessage.getProperty(PROPERTY_ROOM_ADDED_OCCUPANTS_IDS);
        String deletedOccupantsIdsString = (String) qbChatMessage.getProperty(PROPERTY_ROOM_DELETED_OCCUPANTS_IDS);
        String dialogName = (String) qbChatMessage.getProperty(PROPERTY_ROOM_NAME);

        GroupNotification groupNotification = (GroupNotification) qbChatMessage.getNotificationType();

        Resources resources = context.getResources();
        String resultMessage = resources.getString(com.quickblox.q_municate_core.R.string.cht_notification_message);
        QBUser qbUser = AppSession.getSession().getUser();
        boolean ownMessage = qbUser.getId().equals(qbChatMessage.getSenderId());

        if (groupNotification.getUpdateType() != null) {
            switch (groupNotification.getUpdateType()) {
                case CHAT_PHOTO:
                    resultMessage = resources.getString(com.quickblox.q_municate_core.R.string.cht_update_group_photo_message,
                            ownMessage ? qbUser.getFullName() : qbChatMessage.getSenderId());
                    break;
                case CHAT_NAME:
                    resultMessage = resources.getString(com.quickblox.q_municate_core.R.string.cht_update_group_name_message,
                            (ownMessage ? qbUser.getFullName() : qbChatMessage.getSenderId()), dialogName);
                    break;
                case CHAT_OCCUPANTS:
                    String fullNames;

                    if (!TextUtils.isEmpty(addedOccupantsIdsString)) {
                        resultMessage =
                                resources.getString(com.quickblox.q_municate_core.R.string.cht_update_group_added_message,
                                        ownMessage ? qbUser.getFullName(): qbChatMessage.getSenderId() , addedOccupantsIdsString);
                    }

                    if (!TextUtils.isEmpty(deletedOccupantsIdsString)) {
                        resultMessage =
                                resources.getString(com.quickblox.q_municate_core.R.string.cht_update_group_leave_message,
                                        ownMessage ? qbUser.getFullName() : deletedOccupantsIdsString);
                    }

                    break;
            }
        }
        if (!TextUtils.isEmpty(resultMessage)) {
            return resultMessage;
        }

        switch (groupNotification.getType()){
            case CREATE_DIALOG:
                resultMessage = resources.getString(com.quickblox.q_municate_core.R.string.cht_update_group_added_message,ownMessage ? qbUser.getFullName():
                        qbChatMessage.getSenderId(), addedOccupantsIdsString);
                break;

        }

        return resultMessage;

    }

    public static boolean isReadForMe(QBMessage message){
        boolean isRead = false;
        if (!CollectionsUtil.isEmpty(message.getReadIds())){
            isRead = message.getReadIds().contains
                    (AppSession.getSession().getUser().getId());
        }
        return isRead;
    }
}
