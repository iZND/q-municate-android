package com.example.q_municate_chat_service.repository;


import android.arch.lifecycle.LiveData;

import com.example.q_municate_chat_service.entity.user.QMUser;

import java.util.Collection;
import java.util.List;

import rx.Completable;
import rx.Observable;

public interface QMUserRepository {

    Completable create(QMUser event);

    LiveData<List<QMUser>> load(int pageNumber, int count);

    LiveData<QMUser> loadById(int id);

    LiveData<List<QMUser>> loadUsersByIds(List<Integer> usersIds);

    Observable<List<QMUser>> loadByIds(List<Integer> usersIds, boolean forceload);

    Completable update(QMUser event);

    Completable save(QMUser user);

}
