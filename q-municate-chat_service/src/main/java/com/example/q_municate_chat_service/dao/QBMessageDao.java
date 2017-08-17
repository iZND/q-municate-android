package com.example.q_municate_chat_service.dao;


import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.example.q_municate_chat_service.entity.QBMessage;

import java.util.ArrayList;
import java.util.List;

import static com.example.q_municate_chat_service.entity.QBMessage.TABLE_NAME;

@Dao
public interface QBMessageDao {

    @Query("SELECT * FROM "+TABLE_NAME)
    LiveData<List<QBMessage>> getAll();

    @Query("SELECT * FROM "+TABLE_NAME +" where dialogId = :dialogId")
    LiveData<List<QBMessage>> getAllByDialog(String dialogId);

    @Query("SELECT * FROM "+TABLE_NAME +" where id = :msgId")
    LiveData<QBMessage> getById(int msgId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<QBMessage> messages);

    @Insert
    void insert(QBMessage message);

    @Delete
    void delete(QBMessage message);
}
