package com.example.q_municate_chat_service.dao;


import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;


import com.example.q_municate_chat_service.entity.user.QMUser;

import java.util.Collection;
import java.util.List;

import static com.example.q_municate_chat_service.entity.user.QMUser.TABLE_NAME;

public interface QMUserDao {

    @Query("SELECT * FROM "+TABLE_NAME)
    LiveData<List<QMUser>> getAll();

    @Query("SELECT * FROM "+TABLE_NAME +" where id = :userId")
    LiveData<QMUser> getById(int userId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<QMUser> users);

    @Delete
    void delete(QMUser contactItem);

    void deleteUserByExternalId(String externalId);

    List<QMUser> getUsersByIDs(Collection<Integer> idsList);

    QMUser getUserByColumn(String column, String value);

    List<QMUser> getUsersByFilter(Collection<?> filterValue, String filter);
}
