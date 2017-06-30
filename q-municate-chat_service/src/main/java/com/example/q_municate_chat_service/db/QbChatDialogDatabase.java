package com.example.q_municate_chat_service.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;

import com.example.q_municate_chat_service.dao.ContactListDao;
import com.example.q_municate_chat_service.dao.DateConverter;
import com.example.q_municate_chat_service.dao.QBChatDialogDao;
import com.example.q_municate_chat_service.dao.QBMessageDao;
import com.example.q_municate_chat_service.entity.ContactItem;
import com.example.q_municate_chat_service.entity.QBMessage;
import com.quickblox.chat.model.QBChatDialog;

@Database(entities = {QBChatDialog.class, ContactItem.class, QBMessage.class}, version = 1)
@TypeConverters({DateConverter.class})
public abstract class QbChatDialogDatabase extends RoomDatabase {
    public abstract QBChatDialogDao chatDialogDao();

    public abstract ContactListDao contatIlistDao();

    public abstract QBMessageDao chatMessageDao();
}
