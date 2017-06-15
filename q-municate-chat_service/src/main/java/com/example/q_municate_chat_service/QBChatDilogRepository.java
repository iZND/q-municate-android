package com.example.q_municate_chat_service;


import android.arch.lifecycle.LiveData;

import com.quickblox.chat.model.QBChatDialog;

import java.util.List;

public interface QBChatDilogRepository{

    void create(QBChatDialog event);

    LiveData<List<QBChatDialog>> loadAll();

    LiveData<QBChatDialog> loaByd(String id);

    void delete(QBChatDialog event);

}
