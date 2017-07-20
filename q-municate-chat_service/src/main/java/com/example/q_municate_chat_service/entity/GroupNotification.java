package com.example.q_municate_chat_service.entity;


import com.quickblox.chat.model.QBChatMessage;

public class GroupNotification extends NotificationType {

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

    private final Type type;
    private int internalType;
    private UpdateType updateType;

    public GroupNotification(QBChatMessage qbChatMessage) {
        String chatNotificationTypeString = (String) qbChatMessage.getProperty(NotificationsParser.PROPERTY_NOTIFICATION_TYPE);
        internalType = Integer.parseInt(chatNotificationTypeString);

        type = Type.parseByCode(Integer.parseInt(chatNotificationTypeString));

        body = parseBody(qbChatMessage);
    }

    private String parseBody(QBChatMessage qbChatMessage) {
        String notificationTypeString = (String) qbChatMessage.getProperty(PROPERTY_NOTIFICATION_TYPE);
        String updatedInfo = (String) qbChatMessage.getProperty(PROPERTY_ROOM_UPDATE_INFO);
        String addedOccupantsIdsString = (String) qbChatMessage.getProperty(PROPERTY_ROOM_ADDED_OCCUPANTS_IDS);
        String deletedOccupantsIdsString = (String) qbChatMessage.getProperty(PROPERTY_ROOM_DELETED_OCCUPANTS_IDS);
        String dialogName = (String) qbChatMessage.getProperty(PROPERTY_ROOM_NAME);

        NotificationType notificationType = null;

        if (updatedInfo != null) {
            updateType = UpdateType.parseByValue(Integer.parseInt(updatedInfo));
        }

        return notificationTypeString;
    }

    public Type getType() {
        return type;
    }

    public UpdateType getUpdateType() {
        return updateType;
    }

    public enum UpdateType {

        CHAT_PHOTO(1),
        CHAT_NAME(2),
        CHAT_OCCUPANTS(3);

        private int value;

        UpdateType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static UpdateType parseByValue(int value) {
            UpdateType[] prioritiesArray = UpdateType.values();
            UpdateType result = null;
            for (UpdateType type : prioritiesArray) {
                if (type.getValue() == value) {
                    result = type;
                    break;
                }
            }
            return result;
        }
    }

    public enum Type {

        FRIENDS_REQUEST(4), FRIENDS_ACCEPT(5), FRIENDS_REJECT(6), FRIENDS_REMOVE(7),
        CREATE_DIALOG(25), ADDED_DIALOG(21), NAME_DIALOG(22), PHOTO_DIALOG(23), OCCUPANTS_DIALOG(24);

        private int code;

        Type(int code) {
            this.code = code;
        }

        public static Type parseByCode(int code) {
            Type[] valuesArray = Type.values();
            Type result = null;
            for (Type value : valuesArray) {
                if (value.getCode() == code) {
                    result = value;
                    break;
                }
            }
            return result;
        }

        public int getCode() {
            return code;
        }
    }
}
