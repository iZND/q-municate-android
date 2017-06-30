package com.example.q_municate_chat_service.entity;


import com.quickblox.chat.model.QBChatMessage;

public class GroupNotification extends NotificationType{

    private final Type type;
    private int internalType;

    public GroupNotification(QBChatMessage qbChatMessage) {
        String chatNotificationTypeString = (String) qbChatMessage.getProperty(NotificationsParser.PROPERTY_NOTIFICATION_TYPE);
        internalType = Integer.parseInt(chatNotificationTypeString);

        type = Type.parseByCode(Integer.parseInt(chatNotificationTypeString));

    }

    public Type getType(){
        return type;
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
