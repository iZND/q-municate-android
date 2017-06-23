package com.example.q_municate_chat_service.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.example.q_municate_chat_service.entity.ContactItem;

import java.util.List;

import static com.example.q_municate_chat_service.entity.ContactItem.TABLE_NAME;


@Dao
public interface ContactListDao {

    @Query("SELECT * FROM "+TABLE_NAME)
    LiveData<List<ContactItem>> getAll();

    @Query("SELECT * FROM "+TABLE_NAME +" where userId = :userId")
    LiveData<ContactItem> getById(int userId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<ContactItem> repositories);

    @Delete
    void delete(ContactItem contactItem);
}
