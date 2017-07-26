package com.example.q_municate_chat_service.repository;


import android.arch.lifecycle.LiveData;

import com.quickblox.chat.model.QBChatDialog;

import java.util.List;

import rx.Completable;

public interface QBChatDialogRepository {

    Completable create(QBChatDialog event);

    LiveData<List<QBChatDialog>> load(int pageNumber, int count);

    LiveData<QBChatDialog> loadById(String id);

    Completable delete(QBChatDialog event);

    Completable update(QBChatDialog event);
}
