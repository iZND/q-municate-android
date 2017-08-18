package com.example.q_municate_chat_service.entity;


import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;

import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.core.helper.CollectionsUtil;

import java.util.Map;

import static com.example.q_municate_chat_service.entity.QBMessage.TABLE_NAME;


@Entity(tableName = TABLE_NAME, primaryKeys="id")
public class QBMessage extends QBChatMessage {

    public static final String TABLE_NAME = "chat_messages";

    @Ignore
    private NotificationType notificationType;

    boolean isReadForOwner;

    public QBMessage(QBChatMessage chatMessage){
        setBody(chatMessage.getBody());
        setDateSent(chatMessage.getDateSent());
        setDialogId(chatMessage.getDialogId());
        setDeliveredIds(chatMessage.getDeliveredIds());
        setReadIds(chatMessage.getReadIds());
        setMarkable(chatMessage.isMarkable());
        setDelayed(chatMessage.isDelayed());
        setRecipientId(chatMessage.getRecipientId());
        setSenderId(chatMessage.getSenderId());
        setSaveToHistory(chatMessage.isSaveToHistory());
        setProperties(chatMessage.getProperties());
        setNotificationType(NotificationsParser.getNotificationType(chatMessage));
    }

    public QBMessage(){}

    public boolean isSystem(){
        return notificationType != null;
    }

    public NotificationType getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(NotificationType notificationType) {
        this.notificationType = notificationType;
    }

    public boolean isReadForOwner() {
        return isReadForOwner;
    }

    public void setReadForOwner(boolean readForOwner) {
        isReadForOwner = readForOwner;
    }

}
