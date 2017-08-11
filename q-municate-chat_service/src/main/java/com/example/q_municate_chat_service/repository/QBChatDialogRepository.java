package com.example.q_municate_chat_service.repository;


import android.arch.lifecycle.LiveData;

import com.example.q_municate_chat_service.entity.PagedResult;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.core.QBEntityCallback;

import java.util.List;

import rx.Completable;
import rx.Observable;

public interface QBChatDialogRepository {

    Completable create(QBChatDialog event);

    void load(int count, int page,
                                   boolean forceLoad, QBEntityCallback<PagedResult<QBChatDialog>> callback);

    LiveData<List<QBChatDialog>> loadAll(boolean forceLoad);

    LiveData<QBChatDialog> loadById(String id, boolean forceLoad);

    Completable delete(QBChatDialog event);

    Completable update(QBChatDialog event);

    void clear();
}
