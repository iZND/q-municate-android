package com.example.q_municate_chat_service.entity;


import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import static com.example.q_municate_chat_service.entity.NotificationType.TABLE_NAME;


@Entity(tableName = TABLE_NAME)
public class NotificationType {

    public static final String TABLE_NAME = "notification_type";
    @PrimaryKey
    private long id;

    private String notificationType;

    private int internalType;

    private String info;

    enum Type{
        GORUP_NOTIFICATION,
        CALL_NOTIFICATION
    }

}
