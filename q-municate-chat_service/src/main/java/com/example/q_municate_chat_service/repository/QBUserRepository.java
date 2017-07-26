package com.example.q_municate_chat_service.repository;


import android.arch.lifecycle.LiveData;

import com.example.q_municate_chat_service.entity.user.QMUser;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.core.request.QBPagedRequestBuilder;

import java.util.Collection;
import java.util.List;

import rx.Completable;
import rx.Observable;

public class QBUserRepository {

    Completable create(QBChatDialog event);

    LiveData<List<QBChatDialog>> load(int pageNumber, int count);

    LiveData<QBChatDialog> loadById(int id);

    LiveData<QBChatDialog> loadByIds(String id);

    Completable update(QBChatDialog event);

    public Observable<List<QMUser>> getUsersByIDs(final Collection<Integer> usersIds, final QBPagedRequestBuilder requestBuilder) {
        return  getUsersByIDs(usersIds, requestBuilder, true);
    }
}
