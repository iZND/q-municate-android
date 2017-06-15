package com.example.q_municate_chat_service.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;

import com.example.q_municate_chat_service.dao.DateConverter;
import com.example.q_municate_chat_service.dao.QBChatDialogDao;
import com.quickblox.chat.model.QBChatDialog;

@Database(entities = {QBChatDialog.class}, version = 1)
@TypeConverters({DateConverter.class})
public abstract class QbChatDialogDatabase extends RoomDatabase {
    public abstract QBChatDialogDao chatDialogDao();
}
