package com.example.q_municate_chat_service.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;


import com.quickblox.chat.model.QBChatDialog;

import java.util.List;

import rx.Observable;

@Dao
public interface QBChatDialogDao {

    @Query("SELECT * FROM chat_dialogs")
    LiveData<List<QBChatDialog>> getAll();

    @Query("SELECT * FROM chat_dialogs where dialogId = :dialogId")
    QBChatDialog getById(String dialogId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<QBChatDialog> repositories);

    @Delete
    void delete(QBChatDialog dialogModel);


}
