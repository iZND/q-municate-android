package com.example.q_municate_chat_service.dao;


import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;


import com.example.q_municate_chat_service.entity.user.QMUser;

import java.util.Collection;
import java.util.List;

import rx.Completable;
import rx.Observable;

import static com.example.q_municate_chat_service.entity.user.QMUser.TABLE_NAME;

@Dao
public interface QMUserDao {

    @Query("SELECT * FROM "+TABLE_NAME)
    LiveData<List<QMUser>> getAll();

    @Query("SELECT * FROM "+TABLE_NAME +" where id = :userId")
    LiveData<QMUser> getById(int userId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<QMUser> users);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long create(QMUser user);

    @Delete
    int delete(QMUser user);

    @Query("SELECT * FROM "+TABLE_NAME +" where id in (:userIds)")
    LiveData<List<QMUser>> getUsersByIDs(List<Integer> userIds);

    @Query("SELECT * FROM "+TABLE_NAME +" where id in (:userIds)")
    List<QMUser> getByIDs(List<Integer> userIds);

}
