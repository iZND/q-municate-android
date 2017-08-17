package com.example.q_municate_chat_service.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;


import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.core.model.QBEntityPaged;

import java.util.List;

import rx.Observable;

@Dao
public interface QBChatDialogDao {

    @Query("SELECT * FROM chat_dialogs")
    LiveData<List<QBChatDialog>> getAll();

    @Query("SELECT * FROM chat_dialogs where dialogId = :dialogId")
    LiveData<QBChatDialog> getById(String dialogId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<QBChatDialog> repositories);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(QBChatDialog dialog);

    @Update
    void update(QBChatDialog dialog);

    @Delete
    void delete(QBChatDialog dialogModel);

    @Query("DELETE FROM chat_dialogs")
    void clear();
}
