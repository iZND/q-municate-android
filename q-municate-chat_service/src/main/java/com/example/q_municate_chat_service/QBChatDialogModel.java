package com.example.q_municate_chat_service;


import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import com.quickblox.chat.model.QBChatDialog;

@Entity(tableName = "chat_dialog", primaryKeys = "id")
public class QBChatDialogModel extends QBChatDialog{

    @Ignore
    private int dbId;

    public int getDbId() {
        return dbId;
    }

    public void setDbId(int dbId) {
        this.dbId = dbId;
    }
}
